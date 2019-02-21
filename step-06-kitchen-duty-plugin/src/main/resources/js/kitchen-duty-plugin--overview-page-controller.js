AJS.toInit(function(){
    AJS.log('KDP: CycleTime KPI Controller initializing ...');
    var baseUrl = AJS.params.baseURL;
    var restUrl = baseUrl + '/rest/jd-api/1.0';
    window.JDrestUrl = restUrl;

    // Init Base SOY template
    var plotlyTemplate = JIRA.Templates.JDCT.plotly();
    var sidebarTemplate = JIRA.Templates.JDCT.sidebar();
    AJS.$('#jd-cycletime-container').html(plotlyTemplate);
    //AJS.$('#kdp-overview-page-container').html(sidebarTemplate);

    //Global Variables
    var total = 0;
    var graph = {};

    //TODO arrumar esse loop aqui pra deixar numa jql só
    AJS.$(AJS.$('#jd-project-select').select2('data')).each(function () {
        var projName = this.text;
        AJS.$.ajax({
            url: window.JDrestUrl + '/issues/search?query=' + projName,
            dataType: 'json',
            success: function (rawData) {
                var dataXY = [];
                var issues = [];
                AJS.$(rawData).each(function () {
                    var issue = {
                        key: AJS.$(this).attr('key'),
                        resolutionDate: AJS.$(this).attr('resolutionDate'),
                        transitionsTime: AJS.$(this).attr('transitionsTime')
                    };
                    issues.push(issue);

                    if ((issue.transitionsTime !== undefined && issue.transitionsTime !== null && issue.transitionsTime.length > 0) &&
                        (issue.resolutionDate !== undefined && issue.resolutionDate !== null)) {
                        var xy = {};
                        xy.y = 0;
                        xy.text = 'Issue: ' + issue.key + '<br><br>';

                        issue.transitionsTime.forEach(function (t) {
                            xy.y += t.time / 86400000;
                            xy.text += t.name + ': ' + convertMiliToTime(t.time) + '<br><br>';
                            xy.x = new Date(t.resolution)
                        });

                        xy.text += 'Cycle Time: ' + convertMiliToTime(xy.y * 86400000) + '<br><br>';
                        xy.text += 'Resolution: ' + xy.x.toLocaleString();
                        dataXY.push(xy);
                    }
                });

                if (dataXY.length > 0) {
                    dataXY.sort(function (a, b) {
                        if (b.x === null || a.x > b.x) {
                            return 1;
                        }
                        if (a.x === null || a.x < b.x) {
                            return -1;
                        }
                        return 0;
                    });

                    var dataX = [];
                    var dataY = [];
                    var dataText = [];
                    dataXY.forEach(function (data) {
                        dataX.push(data.x);
                        dataY.push(data.y);
                        dataText.push(data.text);
                    });
                    this.total = 0;


                    var rAVG = [];
                    dataY.forEach(function (d, index) {
                        rAVG.push(math.mean(dataY.slice((index - (Math.round(dataY.length * 0.2)) < 0 ? 0 : index - (Math.round(dataY.length * 0.2))),
                            (index + 1) + (Math.round(dataY.length * 0.2)))));
                    });

                    var lowerLim = new Date(new Date(dataX[0]).getTime() - 1 * 86400000);
                    var upperLim = new Date(new Date(dataX[dataX.length - 1]).getTime() + 1 * 86400000);

                    rAVG.push(rAVG[rAVG.length - 1]);
                    dataX.push(upperLim);
                    dataY.push(dataY[dataY.length - 1]);

                    this.graph = {
                        data: [
                            {
                                type: 'scatter',
                                x: dataX.slice().concat(upperLim),
                                y: rAVG.slice().concat(rAVG[rAVG.length - 1]),
                                mode: 'lines',
                                name: 'Rolling average',
                                showlegend: true,
                                hoverinfo: 'y',
                                line: {
                                    shape: 'linear',
                                    color: 'rgb(32,80,129)',
                                    width: 3,
                                }
                            },
                            {
                                type: 'scatter',
                                x: dataX,
                                y: dataY.slice(0, dataY.length - 1),
                                text: dataText,
                                mode: 'markers',
                                name: 'Issue',
                                showlegend: true,
                                hoverinfo: 'text',
                                marker: {
                                    color: 'rgb(35,142,35)',
                                    line: {width: 3},
                                    opacity: 0.8,
                                    size: 10,
                                    symbol: 'circle-open'
                                }
                            },
                            {
                                type: 'scatter',
                                x: [lowerLim, upperLim],
                                y: [math.mean(dataY), math.mean(dataY)],
                                mode: 'lines',
                                name: 'Average',
                                showlegend: true,
                                line: {
                                    color: 'red',
                                    width: 3
                                }
                            },
                            {
                                x: dataX.concat(dataX.slice().reverse()),
                                y: dataY.slice().map(function (d, index) {
                                    return rAVG[index] + math.std(dataY.slice(
                                        (index - (Math.round(dataY.length * 0.2)) < 0) ? 0 : (index - (Math.round(dataY.length * 0.2))),
                                        ((index + 1) + (Math.round(dataY.length * 0.2)))));
                                }).concat(dataY.slice().map(function (d, index) {
                                    return rAVG[index] - math.std(dataY.slice(
                                        (index - (Math.round(dataY.length * 0.2)) < 0) ? 0 : (index - (Math.round(dataY.length * 0.2))),
                                        ((index + 1) + (Math.round(dataY.length * 0.2)))));
                                }).reverse()),
                                fill: 'toself',
                                fillcolor: 'rgba(160,160,210,0.2)',
                                line: {
                                    shape: 'linear',
                                    color: 'transparent'
                                },
                                name: 'Standard Deviation',
                                showlegend: true,
                                hoverinfo: 'skip',
                                type: 'scatter'
                            }
                        ],
                        layout: {
                            autosize: true,
                            title: 'Control Chart',
                            xaxis: {
                                range: [lowerLim, upperLim],
                                type: 'date',
                                zeroline: false
                            },
                            yaxis: {
                                range: [0, Math.max.apply(null, dataY) + 0.5],
                                zeroline: false
                            }
                        },
                        style: {width: '100%', height: '100%'}
                    };

                    this.plotReady = true;
                    this.plotInitialized = true;

                } else {
                    //Mensagem de grafico vazio
                }
            }
        });

    Plotly.plot( 'cycletime-plotly', [{
    }], {
    } );

    function convertMiliToTime(mili){
        var remain = mili;
        var str = '';

        var weeks = Math.floor(remain / (1000 * 60 * 60 * 24 * 7));
        remain = remain % (1000 * 60 * 60 * 24 * 7);

        var days = Math.floor(remain / (1000 * 60 * 60 * 24));
        remain = remain % (1000 * 60 * 60 * 24);

        var hours = Math.floor(remain / (1000 * 60 * 60));
        remain = remain % (1000 * 60 * 60);

        var minutes = Math.floor(remain / (1000 * 60));
        remain = remain % (1000 * 60);

        if (weeks) {
            str += weeks + 'w ';
        }
        if (days) {
            str += days + 'd ';
        }
        if (hours) {
            str += hours + 'h ';
        }
        if (minutes) {
            str += minutes + 'm ';
        }

        return str;
    }

    /*AJS.$('#jd-cycletime').fullCalendar({
        defaultView: 'month',
        weekNumbers: true,
        height: 500,
        fixedWeekCount: false,
        events: function(start, end, timezone, callback) {
            // Full Calender always starts month with days of previous month.
            // We add 10 days to get month we want.
            var year = moment(start).add('days', 10).format('YYYY');
            var month = moment(start).add('days', 10).format('M');
            AJS.$.ajax({
                url: window.KDPrestUrl + '/overview_page/year/' + year + '/month/' + month,
                dataType: 'json',
                success: function(rawEvents) {
                    var events = [];
                    AJS.$(rawEvents).each(function() {
                        var users = AJS.$(this).attr('users');
                        events.push({
                            title: users.join(', '),
                            start: AJS.$(this).attr('start'),
                            end: AJS.$(this).attr('end'),
                            color: users.length > 0 ? '#36B37E' : '#FFAB00',
                        });
                    });
                    callback(events);
                }
            });
        }
    });*/
});

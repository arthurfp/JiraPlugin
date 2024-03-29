AJS.toInit(function() {
    AJS.log('JD: CycleTime KPI initializing ...');
    const baseUrl = AJS.params.baseURL;
    const restUrl = baseUrl + '/rest/jd-api/1.0';
    window.JDrestUrl = restUrl;

    // Init Base SOY template
    const plotlyTemplate = JIRA.Templates.JDCT.plotlyCycleChart();
    const selectProjectsTemplate = JIRA.Templates.JDCT.selectProjects();
    const selectSprintsTemplate = JIRA.Templates.JDCT.selectSprints();
    const selectPlansTemplate = JIRA.Templates.JDCT.selectPlans();
    const selectTeamsTemplate = JIRA.Templates.JDCT.selectTeams();
    AJS.$('#jd-cycletime-container').html(plotlyTemplate);
    AJS.$('#search-plot-features').html(selectProjectsTemplate);
    AJS.$('#search-plot-features').append(selectSprintsTemplate);
    AJS.$('#search-plot-features').append(selectPlansTemplate);
    AJS.$('#search-plot-features').append(selectTeamsTemplate);

    //Global Variables
    let total = 0;
    let graph = {};
    let projectJql = '';
    let sprintJql = '';
    let teamJql = '';
    let issuetypeFilter = '';
    let statusFilter = '';

    let plotReady = false;
    let plotInitialized = false;

    const config = {
        scrollZoom: false,
        displaylogo: false,
        modeBarButtonsToRemove: ['sendDataToCloud'],
        responsive: true,
        displayModeBar: true
    };

    let dataProj = [];
    AJS.$.ajax({
        url: window.JDrestUrl + '/projects/getAll',
        type: 'get',
        dataType: 'json',
        async: false,
        success: function(data) {
            data.forEach(d => {
                dataProj.push(
                    {
                        id: d.key,
                        text: d.name + ' (' + d.key + ')'
                    }
                );
            })

        }
    });

    const auiProjectSelectOptions = {
        data: dataProj,
        minimumInputLength: 1,
        tags: 'true',
    };

    const auiSprintSelectOptions = {
        data: dataProj,
        minimumInputLength: 1,
        tags: 'true',
    };

    const auiPlanSelectOptions = {
        data: dataProj,
        minimumInputLength: 1,
        tags: 'true',
    };

    const auiTeamSelectOptions = {
        data: dataProj,
        minimumInputLength: 1,
        tags: 'true',
    };

    AJS.$('#jd-project-select').auiSelect2(auiProjectSelectOptions);
    AJS.$('#jd-team-select').auiSelect2(auiTeamSelectOptions);
    AJS.$('#jd-sprint-select').auiSelect2(auiSprintSelectOptions);
    AJS.$('#jd-plan-select').auiSelect2(auiPlanSelectOptions);

    AJS.$('#jd-project-select').get(0).onchange = function(e) { searchProject(e); };

    function searchProject(e) {
        const data = AJS.$('#jd-project-select').get(0).value.split(',');
        data.forEach(function (d, index) {
            const projKey = d;
            if(index < 1) {
                projectJql = 'project =\"' + projKey + '\"';
            }
            else{
                projectJql = projectJql + ' OR project=\"' + projKey + '\"';
            }
        }
        );
        search();
    }

    function search() {
        plotInitialized = true;
        plotReady = false;
        const searchJql = projectJql + sprintJql + teamJql + issuetypeFilter + statusFilter;
        drawPlotly(searchJql);
    }

    //TODO arrumar esse loop aqui pra deixar numa jql só
    //AJS.$(AJS.$('#jd-project-select').select2('data')).each(function () {
    function drawPlotly(jql) {
        AJS.$.ajax({
            //TODO tirar esse AND
            url: window.JDrestUrl + '/issues/search?query=' + jql + 'AND (status changed from "In Progress")',
            dataType: 'json',
            success: rawData => {
                const dataXY = [];
                rawData.forEach(rd => {
                    const issue = {
                        key: rd.key,
                        resolutionDate: rd.resolutionDate,
                        transitionsTime: rd.transitionsTime
                    };

                    if ((issue.transitionsTime !== undefined && issue.transitionsTime !== null && Object.keys(issue.transitionsTime).length > 0) &&
                        (issue.resolutionDate !== undefined && issue.resolutionDate !== null)) {
                        const xy = {};
                        xy.y = 0;
                        xy.text = 'Issue: ' + issue.key + '<br><br>';

                        //TODO arrumar o cycle time direito
                        let cycleTime = '';

                        Object.entries(issue.transitionsTime).forEach(t => {
                            if (t[0] == 'In Progress'){
                                xy.y += t[1].duration / 86400000;
                                xy.x = new Date(t[1].resolvedDate);
                                cycleTime = convertMiliToTime(t[1].duration);
                            } else {
                                xy.text += t[0] + ': ' + convertMiliToTime(t[1].duration) + '<br><br>';
                            }
                        });

                        xy.text += 'Cycle Time (In Progress): ' + cycleTime + '<br><br>';
                        xy.text += 'Total: ' + convertMiliToTime(xy.y * 86400000) + '<br><br>';
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

                    const dataX = [];
                    const dataY = [];
                    const dataText = [];
                    dataXY.forEach(function(data) {
                        dataX.push(data.x);
                        dataY.push(data.y);
                        dataText.push(data.text);
                    });
                    total = 0;


                    const rAVG = [];
                    dataY.forEach(function (d, index) {
                        rAVG.push(math.mean(dataY.slice((index - (Math.round(dataY.length * 0.2)) < 0 ? 0 : index - (Math.round(dataY.length * 0.2))),
                            (index + 1) + (Math.round(dataY.length * 0.2)))));
                    });

                    const lowerLim = new Date(new Date(dataX[0]).getTime() - 1 * 86400000); // "Days * time" to have a margin in graph data
                    const upperLim = new Date(new Date(dataX[dataX.length - 1]).getTime() + 1 * 86400000); // "Days * time" to have a margin in graph data

                    rAVG.push(rAVG[rAVG.length - 1]);
                    dataX.push(upperLim);
                    dataY.push(dataY[dataY.length - 1]);

                    graph = {
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
                                }).concat(dataY.slice().map( function(d, index) {
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
                            },
                            margin: {
                                t: 60, //top margin
                                l: 60, //left margin
                                r: 60, //right margin
                                b: 60 //bottom margin
                            }
                        }
                    };

                    plotReady = true;
                    plotInitialized = true;

                } else {
                    //Mensagem de grafico vazio
                }

                AJS.log(graph.data);
                Plotly.plot( 'cycletime-plotly', graph.data, graph.layout, config );
                graph = {};
            }
        });
    }

    AJS.$('cycletime-plotly').on('plotly_click', function(data) {
        if (data.hasOwnProperty('points') && data.points[0].data.name === 'Issue') {
            const str = data.points[0].text;
            let url = str.substring(str.indexOf('Issue: ') + 7, str.indexOf('<br><br>'));
            url = baseUrl + '/browse/' + url;
            window.open(url, '_blank');
        }
    });

    function convertMiliToTime(mili){
        let remain = mili;
        let str = '';

        const weeks = Math.floor(remain / (1000 * 60 * 60 * 24 * 7));
        remain = remain % (1000 * 60 * 60 * 24 * 7);

        const days = Math.floor(remain / (1000 * 60 * 60 * 24));
        remain = remain % (1000 * 60 * 60 * 24);

        const hours = Math.floor(remain / (1000 * 60 * 60));
        remain = remain % (1000 * 60 * 60);

        const minutes = Math.floor(remain / (1000 * 60));
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

});

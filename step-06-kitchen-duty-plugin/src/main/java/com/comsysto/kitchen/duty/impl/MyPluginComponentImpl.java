package com.comsysto.kitchen.duty.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.greenhopper.service.sprint.SprintIssueService;
import com.atlassian.greenhopper.service.sprint.SprintQueryService;
import com.atlassian.greenhopper.service.sprint.SprintService;
import com.atlassian.jira.bc.issue.search.SearchService;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.comsysto.kitchen.duty.api.MyPluginComponent;

import javax.inject.Inject;
import javax.inject.Named;

@ExportAsService ({MyPluginComponent.class})
@Named ("myPluginComponent")
public class MyPluginComponentImpl implements MyPluginComponent
{

    @ComponentImport
    protected com.atlassian.sal.api.user.UserManager userManager;

    @ComponentImport
    private final ApplicationProperties applicationProperties;

    @ComponentImport
    private ActiveObjects activeObjects;

    @ComponentImport
    private SearchService searchService;

    @ComponentImport
    private ProjectService projectSearchService;

    @ComponentImport
    private SprintIssueService sprintIssueServicee;

    @ComponentImport
    private SprintQueryService sprintQueryService;

    @ComponentImport
    private SprintService sprintService;

    @ComponentImport
    private JqlQueryParser jqlQueryParserSearchService;

    @ComponentImport
    private PageBuilderService pageBuilderService;

    @Inject
    public MyPluginComponentImpl(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public String getName()
    {
        if(null != applicationProperties)
        {
            return "myComponent:" + applicationProperties.getDisplayName();
        }

        return "myComponent";
    }
}

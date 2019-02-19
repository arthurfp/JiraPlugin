package com.comsysto.kitchen.duty.rest;

import com.atlassian.greenhopper.service.ServiceOutcome;
import com.atlassian.greenhopper.service.sprint.Sprint;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.greenhopper.service.sprint.SprintQueryService;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Named
@Path("/sprints")
public class SprintSearchResource {

    private SprintQueryService sprintQueryService;
    private JqlQueryParser jqlQueryParser;
    private final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

    @Inject
    public SprintSearchResource(SprintQueryService sprintQueryService, JqlQueryParser jqlQueryParser) {
        this.sprintQueryService = sprintQueryService;
        this.jqlQueryParser = jqlQueryParser;
    }

    public SprintSearchResource() {
    }

    @GET
    @Path("/health")
    @Produces({MediaType.APPLICATION_JSON})
    @AnonymousAllowed
    public Response health() {
        return Response.ok("ok").build();
    }

    /**
     * Call from select2 JS plugin
     * Response needs to look like this: [{ 'id': 1, 'text': 'Demo' }, { 'id': 2, 'text': 'Demo 2'}]
     */
    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    public Response searchSprintbyProject(@QueryParam("query") final String JQLQuery,
                                 @Context HttpServletRequest request) {
        List<SprintSearchResourceModel> sprints = findSprints(JQLQuery);
        return Response.ok(sprints).build();
    }

    public List<SprintSearchResourceModel> findSprints(String term) {

        String JQLQuery = "project="+term;
        Query query = null;

        try {
            query = jqlQueryParser.parseQuery(JQLQuery);
        } catch (JqlParseException e) {
            e.printStackTrace();
        }

        List<SprintSearchResourceModel> result = new ArrayList<>();

        if(query != null) {
            ServiceOutcome<List<Sprint>> sprintResult = sprintQueryService.getSprints(user, query);
            if (sprintResult.isValid()) {
                List<Sprint> sprints = sprintResult.getValue();
                if (!sprints.isEmpty()) {
                    for (Sprint sprint : sprints) {
                        result.add(new SprintSearchResourceModel(sprint));
                    }
                }
            }
        }
        return result;
    }
}

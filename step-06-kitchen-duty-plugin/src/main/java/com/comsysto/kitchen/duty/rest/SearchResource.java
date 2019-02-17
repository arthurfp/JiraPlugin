package com.comsysto.kitchen.duty.rest;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

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
@Path("/issues")
public class SearchResource {

    private SearchService searchService;
    private final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    private final ChangeHistoryManager changeManager = ComponentAccessor.getChangeHistoryManager();

    @Autowired
    public SearchResource(@ComponentImport SearchService searchService) {
        this.searchService = searchService;
    }

    public SearchResource() {
    }

    @GET
    @Path("/healthIssue")
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
    @Path("/searchIssue")
    @Produces({MediaType.APPLICATION_JSON})
    public Response searchIssues(@QueryParam("query") final String JQLQuery,
                                 @Context HttpServletRequest request) {
        List<SearchResourceModel> issues = findIssues(JQLQuery);
        return Response.ok(issues).build();
    }

    public List<SearchResourceModel> findIssues(String term) {

        String JQLQuery = "project="+term;
        List<SearchResourceModel> result = new ArrayList<SearchResourceModel>();

        SearchService.ParseResult parseResult = searchService.parseQuery(user, JQLQuery);
        if (parseResult.isValid()) {
            SearchResults results;
            try {
                results = searchService.search(user, parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
            } catch (SearchException e) {
                return null;
            }
            List<Issue> issues = results.getIssues();
            if (!issues.isEmpty()) {
                for (Issue issue : issues) {
                    result.add(new SearchResourceModel(issue, changeManager.getChangeItemsForField(issue, "status")));
                }
            }
        }
        return result;
    }
}

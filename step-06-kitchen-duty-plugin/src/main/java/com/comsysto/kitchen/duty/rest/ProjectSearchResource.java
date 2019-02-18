package com.comsysto.kitchen.duty.rest;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
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

/**
 * A resource of message.
 */
@Named
@Path("/search-jira")
public class ProjectSearchResource {

    private ProjectService projectService;

    @Inject
    public ProjectSearchResource(ProjectService projectService){
        this.projectService = projectService;
    }

    public ProjectSearchResource(){

    }

    @GET
    @Path("/health")
    @Produces({MediaType.APPLICATION_JSON})
    @AnonymousAllowed
    public Response health() {
        return Response.ok("ok").build();
    }

    @GET
    @Path("/project")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getProject(@QueryParam("query") final String searchQuery,
                               @Context HttpServletRequest request ) {
        ProjectSearchResourceModel project = findProject(searchQuery);
        return Response.ok(project).build();
    }

    @GET
    @Path("/projects")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getProjects(@Context HttpServletRequest request ) {
        List<ProjectSearchResourceModel> projects = findProjects(request);
        return Response.ok(projects).build();
    }

    private ProjectSearchResourceModel findProject(String query){
        ProjectSearchResourceModel projectSearchResourceModel = new ProjectSearchResourceModel();
        Project project = null;
        if((project = projectService.getProjectByKey(query).getProject()) != null) {
            projectSearchResourceModel = new ProjectSearchResourceModel(project);
        }
        return projectSearchResourceModel;
    }

    private List<ProjectSearchResourceModel> findProjects(HttpServletRequest request){
        JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        com.atlassian.jira.user.ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();

        List<ProjectSearchResourceModel> projectSearchResourceModels = new ArrayList<ProjectSearchResourceModel>();
        List<Project> projects = null;
        if((projects = projectService.getAllProjects(user).get()).isEmpty()) {
            for(Project proj : projects) {
                projectSearchResourceModels.add(new ProjectSearchResourceModel(proj));
            }
        }
        return projectSearchResourceModels;
    }

}

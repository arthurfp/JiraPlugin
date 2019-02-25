package com.comsysto.kitchen.duty.rest;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
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
@Path("/projects")
public class ProjectSearchResource {

    private ProjectService projectService;

    private final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

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
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getProject(@QueryParam("query") final String searchQuery,
                               @Context HttpServletRequest request ) {
        ProjectSearchResourceModel project = findProject(searchQuery);
        return Response.ok(project).build();
    }

    @GET
    @Path("/getAll")
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

        List<ProjectSearchResourceModel> projectSearchResourceModels = new ArrayList<>();
        List<Project> projects = null;
        projects = projectService.getAllProjects(user).get();
        if(!projects.isEmpty()){
            for(Project proj : projects) {
                projectSearchResourceModels.add(new ProjectSearchResourceModel(proj));
            }
        }
        return projectSearchResourceModels;
    }

}

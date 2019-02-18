package com.comsysto.kitchen.duty.rest;

import com.atlassian.jira.project.Project;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectSearchResourceModel {

    @XmlElement
    private String key;

    @XmlElement
    private String name;

    public ProjectSearchResourceModel() {
    }

    public ProjectSearchResourceModel(Project project) {
        this.key = project.getKey();
        this.name = project.getName();
    }
}

package com.comsysto.kitchen.duty.rest;

import com.atlassian.greenhopper.service.sprint.Sprint;
import com.atlassian.jira.project.Project;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class SprintSearchResourceModel {

    @XmlElement
    private Long id;

    @XmlElement
    private String name;

    public SprintSearchResourceModel() {
    }

    public SprintSearchResourceModel(Sprint sprint) {
        this.id = sprint.getId();
        this.name = sprint.getName();
    }
}

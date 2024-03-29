package com.comsysto.kitchen.duty.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.user.ApplicationUser;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.*;
import java.util.*;

@XmlRootElement(name = "issues")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResourceModel {

    @XmlElement
    private String key;

    @XmlElement
    private String assignee;

    @XmlElement
    private String creator;

    @XmlElement
    private String reporter;

    @XmlElement
    private Date created;

    @XmlElement
    private String issuetype;

    @XmlElement
    private Date resolutionDate;

    @XmlElement
    private Map<String, TransitionsType> transitionsTime;

    public SearchResourceModel() {
    }

    public SearchResourceModel(Issue issue, List<ChangeItemBean> items) {
        this.key = issue.getKey();
        this.assignee = (issue.getAssigneeUser() != null) ? issue.getAssigneeUser().getUsername() : "";
        this.creator = (issue.getCreator() != null) ? issue.getCreator().getUsername() : "";
        this.reporter = (issue.getReporter() != null) ? issue.getReporter().getUsername() : "";
        this.created = issue.getCreated();
        this.issuetype = issue.getIssueType().getName();
        this.resolutionDate = issue.getResolutionDate();

        this.transitionsTime = new HashMap<String, TransitionsType>();
        Map<String, Long> transitions = new HashMap<String, Long>();
        Map<String, Date> lastDate = new HashMap<String, Date>();
        Map<String, Date> resolvedDate = new HashMap<String, Date>();

        for(ChangeItemBean item : items){
            if(!transitions.containsKey(item.getFromString())) {
                resolvedDate.put(item.getFromString(), item.getCreated());
                lastDate.put(item.getToString(), item.getCreated());
                Long diff = item.getCreated().getTime() - issue.getCreated().getTime();
                transitions.put(item.getFromString(), new Date(diff).getTime());
            } else {
                resolvedDate.put(item.getFromString(), item.getCreated());
                lastDate.put(item.getToString(), item.getCreated());
                Long diff = item.getCreated().getTime() - lastDate.get(item.getFromString()).getTime();
                Long total = transitions.get(item.getFromString()) + diff;
                transitions.put(item.getFromString(), total);
            }
        }
        for (Map.Entry<String, Long> entry : transitions.entrySet()) {
            this.transitionsTime.put(entry.getKey(), new TransitionsType(transitions.get(entry.getKey()), resolvedDate.get(entry.getKey())));
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getIssuetype() {
        return issuetype;
    }

    public void setIssuetype(String issuetype) {
        this.issuetype = issuetype;
    }

    public Date getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(Date resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public Map<String, TransitionsType> getTransitionsTime() {
        return transitionsTime;
    }

    public void setTransitionsTime(Map<String, TransitionsType> transitionsTime) {
        this.transitionsTime = transitionsTime;
    }
}

class TransitionsType {

    @JsonProperty("duration")
    private Long duration;

    @JsonProperty("resolvedDate")
    private Date resolvedDate;

    public TransitionsType(Long duration, Date resolvedDate) {
        this.duration = duration;
        this.resolvedDate = resolvedDate;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public void setResolvedDate(Date resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public Long getDuration() {
        return duration;
    }

    public Date getResolvedDate() {
        return resolvedDate;
    }
}

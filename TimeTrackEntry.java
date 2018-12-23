package eu.domroese.opentimetracking;

import java.util.Date;

public class TimeTrackEntry {

    private String uniqueId;

    private String project;

    private String story;

    private String task;

    private Date start;

    private Date end;


    public TimeTrackEntry(String project, String story, String task, Date start) {
        this.project = project;
        this.story = story;
        this.task = task;
        this.start = start;
    }
    public TimeTrackEntry(String uniqueid, String project, String story, String task, Date start, Date end) {
        this.uniqueId = uniqueid;
        this.project = project;
        this.story = story;
        this.task = task;
        this.start = start;
        this.end = end;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}

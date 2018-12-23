package eu.domroese.opentimetracking;

public class TimeTrackSummary {

    private String project;

    private String story;

    private String task;

    private String date;

    private Float seconds;

    public TimeTrackSummary(String project, String story, String task, String date, Float seconds) {
        this.project = project;
        this.story = story;
        this.task = task;
        this.date = date;
        this.seconds = seconds;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Float getSeconds() {
        return seconds;
    }

    public void setSeconds(Float seconds) {
        this.seconds = seconds;
    }
}

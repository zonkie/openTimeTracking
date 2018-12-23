package eu.domroese.opentimetracking;

import java.util.Date;

public class Project {

    private String uniqueId;

    private String Project;

    private String color;

    public Project(String project) {
        Project = project;
    }

    public Project(String uniqueId, String project) {
        this.uniqueId = uniqueId;
        Project = project;
    }

    public Project(String uniqueId, String project, String color) {
        this.uniqueId = uniqueId;
        Project = project;
        this.color = color;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getProject() {
        return Project;
    }

    public void setProject(String project) {
        Project = project;
    }

    public String getColor() { return color; }

    public void setColor(String color) { this.color = color; }
}

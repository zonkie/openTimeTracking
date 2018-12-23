package eu.domroese.opentimetracking;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;


public class Controller {

    private Stage stage;
    private Scene scene;

    private AnchorPane summaryAnchorPane = null;
    private AnchorPane mainAnchorPane = null;
    private AnchorPane sprintSummaryAnchorPane = null;
    private Label labelActiveProject = null;
    private Label labelSummary = null;
    private Label labelDailySumamry = null;

    @FXML
    private Menu logMenu;

    private TimeTrackingDatabase db;
    private ProjectDatabase projectDb;
    private ConfigHandler conf;

    private List<Project> allProjects = new ArrayList<>();

    public void init(Stage stage) {
        this.stage = stage;
        this.conf = ConfigHandler.getInstance();
        MenuBar menubar = (MenuBar) this.stage.getScene().lookup("#menuBar");
        ObservableList<Menu> menuEntries = menubar.getMenus();

        try {
            for (Menu menu : menuEntries) {
                System.out.println(menu.getId());
                if ("logMenu".equals(menu.getId())) {
                    System.out.println("Found menu");
                    this.logMenu = menu;
                    System.out.println("huargh!");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

        this.initDbAndControls();
    }

    /**
     * Handlers start
     */
    public void showSummary() {
        Label summaryLabel = this.getSummaryLabel();
        Label activeProjectLabel = this.getActiveProjectLabel();

        String summary = "";
        try {

            String activeProject = this.db.getActiveProject();
            List<TimeTrackSummary> summaries = this.db.fetchSumByProject();
            for (TimeTrackSummary sum : summaries) {
                summary = summary + OutputFormatter.formatSummary(sum);
            }
            summary = summary + System.getProperty("line.separator") + "Active: " + activeProject;
            summaryLabel.setText(summary);

            activeProjectLabel.setText("Active: " + activeProject);

        } catch (Exception e) {
            System.out.println("Error in ShowSummary() " + e.getMessage());
        }
    }

    public void showLastEntry() {
        Label activeProjectLabel = this.getActiveProjectLabel();

        try {
            List<TimeTrackSummary> summaries = this.db.getLastCompleteEntry();
            for (TimeTrackSummary sum : summaries) {
                activeProjectLabel.setText(
                        activeProjectLabel.getText() + System.getProperty("line.separator")
                                + "Last entry: " + sum.getProject()
                                + ((sum.getStory() != "") ? " " + sum.getStory() : "")
                                + ((sum.getTask() != "") ? " " + sum.getTask() : "")
                                + ": "
                                + Math.round(sum.getSeconds() / 60) + " min"
                );

            }

        } catch (Exception e) {
            System.out.println("Error in ShowSummary() " + e.getMessage());
        }

    }

    public void showDailySummary() {
        Label summaryLabel = this.getDailySummaryLabel();

        String summary = "";
        try {
            String activeProject = this.db.getActiveProject();
            List<DailySummary> summaries = this.db.fetchSumByDay();
            for (DailySummary sum : summaries) {
                summary = summary + OutputFormatter.formatDailySummary(sum);
            }
            summaryLabel.setText(summary);
        } catch (Exception e) {
            System.out.println("Error in showDailySummary() " + e.getMessage());
        }

    }

    public void displaySprintSummary() {
        try {
            DatePicker pickStart = (DatePicker) this.stage.getScene().lookup("#pickStart");
            if (pickStart == null) {
                throw new Exception("Element #pickStart not found!");
            }

            DatePicker pickEnd = (DatePicker) this.stage.getScene().lookup("#pickEnd");
            if (pickStart == null) {
                throw new Exception("Element #pickEnd not found!");
            }

            Label labelSprintSummary = (Label) this.stage.getScene().lookup("#labelSprintSummary");
            if (pickStart == null) {
                throw new Exception("Element #labelSprintSummary not found!");
            }

            PieChart summaryPie = (PieChart) this.stage.getScene().lookup("#summaryPieChart");
            if (summaryPie == null) {
                throw new Exception("Element #summaryPieChart not found!");
            }
            summaryPie.getData().clear();

            LocalDate startDate = pickStart.getValue();
            LocalDate endDate = pickEnd.getValue();

            // fetch by date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            List<TimeTrackSummary> sprintSummary = this.db.getSprintSummary(startDate.format(formatter), endDate.format(formatter));

            int totalSeconds = 0;
            for (TimeTrackSummary projectSum : sprintSummary) {
                totalSeconds += projectSum.getSeconds();
            }
            System.out.println("total minutes: " + totalSeconds);
            float percentSeconds = totalSeconds / 100;
            System.out.println("percent minutes: " + percentSeconds);

            String sprintSummaryText = "";
            for (TimeTrackSummary projectSum : sprintSummary) {
                int projectSeconds = Math.round(projectSum.getSeconds());

                Integer projectPercentage = Math.round(projectSeconds / percentSeconds);

                /** Was: Halfway exact percentages **/
                //BigDecimal bd = new BigDecimal(Float.toString(projectPercentage));
                //bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
                //Float projectPercentageRounded = bd.floatValue();

                /** is now: Rounded percentages **/
                Float projectPercentageRounded = Float.valueOf(projectPercentage);

                PieChart.Data pieChart = new PieChart.Data(projectSum.getProject() + " " + projectPercentageRounded + "%", projectPercentageRounded);
                summaryPie.getData().add(pieChart);

                //@TODO: Add Colors to PieChart

                sprintSummaryText = sprintSummaryText
                        + String.format("%1$-" + 10 + "s", projectSum.getProject() + ":") + "\t"
                        + projectSeconds / 60 / 60 + "h \t"
                        + projectPercentageRounded + "% of total\t"
                        + System.getProperty("line.separator")
                ;
            }

            System.out.println(sprintSummaryText);
            labelSprintSummary.setText(sprintSummaryText);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occured while showing the complete sprint summary: " + e.getMessage());
        }

    }

    public void handleReload(ActionEvent event) {
        this.stage = Main.getPrimaryStage();
        this.scene = this.stage.getScene();

        this.init(this.stage);
    }

    public void editEntries() {
        this.stage = Main.getPrimaryStage();
        this.scene = this.stage.getScene();
        this.fillEditEntriesTable();
    }

    public void setSprintStart(ActionEvent event) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date today = new Date();
            this.conf.setConfigValue("sprintStart", formatter.format(today));
        } catch (Exception e) {
            System.out.println("Could not set sprint start: " + e.getMessage());
        }
    }
    /* Handlers end */

    /* Scene Init START */
    private void initDbAndControls() {
        try {
            this.db = TimeTrackingDatabase.getInstance();
        } catch (Exception e) {
            System.out.println("No TimeTracking database instance found");
        }

        try {
            this.projectDb = ProjectDatabase.getInstance();
        } catch (Exception e) {
            System.out.println("No Project database instance found");
        }

        initClassVariables();
        initButtons();
        initSprintSummaryControls();
        initEditSummaryTableView();
    }

    private void initClassVariables() {
        this.stage = Main.getPrimaryStage();
        this.scene = this.stage.getScene();
    }

    private void initButtons() {
        List<Project> projectsA = new ArrayList<>();
        try {
            this.allProjects = this.projectDb.getProjects();
            this.allProjects.add(0, new Project("1", "Pause"));
            this.allProjects.add(1, new Project("2", "Feierabend"));
        } catch (Exception e) {
            System.out.println("Unable to fetch Projects");
        }

        ArrayList<String> projects = new ArrayList<>();
        for (Project proj : this.allProjects) {
            projects.add(proj.getProject());
        }

        int count = 1;
        AnchorPane mainAnchorPane = this.getMainAnchorPane();

        List<Node> elements = mainAnchorPane.getChildren();

        outerloop:
        for (String projectName : projects) {

            // Check if element exists
            for (Node element : elements) {
                if (projectName.equals(element.getId())) {
                    System.out.println(projectName + ":" + element.getId() + "?" + projectName.equals(element.getId()));
                    Button button = (Button) Main.getPrimaryStage().getScene().lookup("#" + projectName);
                    String color = this.getProjectColor(projectName);
                    if (button != null) {
                        if (color != null) {
                            button.setStyle("-fx-background-color: #" + color);
                        } else {
                            button.setStyle("-fx-background-color: #ffffff");
                        }
                    }
                    continue outerloop;
                }
            }
            this.addButton(projectName, count);
            count++;
        }
    }

    private void addButton(String projectName, Integer count) {
        AnchorPane summaryAnchorPane = this.getSummaryAnchorPane();

        String color = this.getProjectColor(projectName);

        Button projectButton = new Button();
        projectButton.setId(projectName);
        int XMargin = 15;
        projectButton.setLayoutX(XMargin);
        int YMargin = 5;
        int buttonHeight = 50;
        projectButton.setLayoutY((count * (buttonHeight + YMargin)) + YMargin);
        projectButton.setText(projectName);
        projectButton.setPrefHeight(buttonHeight);
        int buttonWidth = 100;
        projectButton.setPrefWidth(buttonWidth);
        projectButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        projectButton.autosize();

        if (color != null) {
            projectButton.setStyle("-fx-background-color: #" + color);
        }

        projectButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String projectName = ((Button) event.getSource()).getText();
                        try {
                            if (projectName == "Feierabend") {
                                db.closeOpenTimeTrackEntries();
                            } else {
                                String story = "";
                                String task = "";
                                try {
                                    if (projectName != "Feierabend" && projectName != "Pause") {

                                        TextField storyField = (TextField) stage.getScene().lookup("#" + projectName + "Story");
                                        TextField taskField = (TextField) stage.getScene().lookup("#" + projectName + "Task");
                                        story = storyField.getText();
                                        task = taskField.getText();
                                        storyField.setText("");
                                        taskField.setText("");
                                    }
                                } catch (Exception e) {
                                    System.out.println("An exception occured: " + e.getMessage());
                                }
                                db.createNewEntry(projectName, story, task);

                            }
                            try {
                                showSummary();
                                showDailySummary();
                                showLastEntry();
                                displaySprintSummary();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                            updateButtons();
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    }
                }
        );


        summaryAnchorPane.setPrefHeight(300);

        mainAnchorPane.setPrefHeight(200);
        mainAnchorPane.getChildren().add(projectButton);

        if (projectName != "Feierabend" && projectName != "Pause") {
            TextField storyText = new TextField();
            storyText.setId(projectName + "Story");
            int inputWidth = 85;
            storyText.setMinWidth(inputWidth);
            storyText.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            storyText.setLayoutX(XMargin + buttonWidth + XMargin);
            storyText.setLayoutY((count * (buttonHeight + YMargin)) + YMargin);

            TextField taskText = new TextField();
            taskText.setId(projectName + "Task");
            taskText.setMinWidth(inputWidth);
            taskText.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            taskText.setLayoutX(XMargin + buttonWidth + XMargin);
            int inputHeight = 25;
            taskText.setLayoutY((count * (buttonHeight + YMargin)) + YMargin + inputHeight);
            mainAnchorPane.getChildren().add(storyText);
            mainAnchorPane.getChildren().add(taskText);
        }
        Label activeProjectLabel = this.getActiveProjectLabel();
        double windowHeight = (this.allProjects.size()) * (buttonHeight + YMargin) + activeProjectLabel.getHeight() + YMargin;
        mainAnchorPane.setMinHeight(windowHeight);
        mainAnchorPane.setPrefHeight(windowHeight);
        mainAnchorPane.setMaxHeight(windowHeight);
        this.stage.setMinHeight(windowHeight);
        this.stage.setMaxHeight(windowHeight);
        this.stage.show();
        //mainAnchorPane.autosize();
        this.showSummary();
        this.showDailySummary();
        this.showLastEntry();
        this.updateButtons();

    }

    private String getProjectColor(String projectName) {

        for (Project current : this.allProjects) {
            if (projectName.equals(current.getProject())) {
                if (current.getColor() != null) {
                    return current.getColor();
                }
            }
        }

        return null;
    }


    private void initSprintSummaryControls() {
        Properties config = this.conf.loadConfig();
        String sprintStart = config.getProperty("sprintStart");
        LocalDate date = null;
        try {
            Date startDate = null;
            startDate = new SimpleDateFormat("yyyy-MM-dd").parse(sprintStart);
            date = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (Exception e) {

        }

        DatePicker pickStart = new DatePicker();

        if (date != null) {
            pickStart = new DatePicker(date);
        }

        pickStart.setId("pickStart");
        pickStart.setPromptText("Start");
        pickStart.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        displaySprintSummary();
                    }
                }
        );

        pickStart.setLayoutX(15);
        pickStart.setLayoutY(15);


        DatePicker pickEnd = new DatePicker();
        if (date != null) {
            pickEnd = new DatePicker(date.plusDays(13));
        }
        pickEnd.setId("pickEnd");
        pickEnd.setPromptText("Ende");
        pickEnd.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            displaySprintSummary();
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
        );

        pickEnd.setLayoutX(15);
        pickEnd.setLayoutY(45);

        Label labelSprintSummary = new Label();
        labelSprintSummary.setId("labelSprintSummary");
        labelSprintSummary.setLayoutY(70);
        labelSprintSummary.setPadding(new Insets(15, 15, 15, 15));

        PieChart summaryPie = new PieChart();
        summaryPie.setId("summaryPieChart");
        summaryPie.setLayoutX(0);
        summaryPie.setLayoutY(215);
        summaryPie.setMinWidth(200);
        summaryPie.setMaxWidth(400);
        summaryPie.autosize();

        AnchorPane sprintSummaryAnchorPane = this.getSprintSummaryAnchorPane();
        sprintSummaryAnchorPane.setPrefHeight(200);
        sprintSummaryAnchorPane.getChildren().add(pickStart);
        sprintSummaryAnchorPane.getChildren().add(pickEnd);
        sprintSummaryAnchorPane.getChildren().add(labelSprintSummary);

        sprintSummaryAnchorPane.getChildren().add(summaryPie);
        displaySprintSummary();
    }

    private void initEditSummaryTableView() {
        TableView entries = (TableView) this.scene.lookup("#editEntriesTableView");

        TableColumn project = getTableColumn("project", "project", "project", "project");
        TableColumn story = getTableColumn("story", "story", "story", "story");
        TableColumn task = getTableColumn("task", "task", "task", "task");
        TableColumn start = getTableColumn("start", "start", "start", "start");
        TableColumn end = getTableColumn("end", "end", "end", "end");

        entries.getColumns().addAll(project, story, task, start, end);
        entries.setEditable(true);

        this.fillEditEntriesTable();
    }

    private void fillEditEntriesTable() {
        TableView entries = (TableView) this.scene.lookup("#editEntriesTableView");
        entries.getItems().remove(0, (entries.getItems().size() - 1));
        try {
            TimeTrackingDatabase ttdb = TimeTrackingDatabase.getInstance();
            List<TimeTrackEntry> todaysEntries = ttdb.fetchAllForDay();

            for (TimeTrackEntry todaysEntry : todaysEntries) {
                entries.getItems().add(todaysEntry);
            }
        } catch (Exception e) {

        }
    }

    private TableColumn getTableColumn(String columnName, String columnTitle, String columnId, String propertyValueFactoryIdentifier) {
        TableColumn column = new TableColumn(columnName);
        column.setText(columnTitle);
        column.setId(columnId);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyValueFactoryIdentifier));

        return column;
    }

    private void updateButtons() {
        try {
            String activeProject = this.db.getActiveProject();
            AnchorPane mainAnchorPane = this.getMainAnchorPane();
            ObservableList<Node> children = mainAnchorPane.getChildren();

            for (Node child : children) {
                if (child.getClass() == Button.class) {
                    Button button = (Button) child;

                    if (activeProject.toLowerCase().contains(button.getText().toLowerCase())) {
                        button.setStyle("-fx-background-color: #ddddff");
                    } else {
                        String color = getProjectColor(button.getText());
                        if (color != null) {
                            button.setStyle("-fx-background-color: #" + color);
                        } else {
                            button.setStyle("-fx-background-color: #ffffff");
                        }
                    }
                }
            }
        } catch (Exception e) {

        }

    }

    /**
     * Scene Init END
     */



    /* Elements getters */
    private AnchorPane getSummaryAnchorPane() {
        if (this.summaryAnchorPane == null) {
            this.summaryAnchorPane = (AnchorPane) this.scene.lookup("#summaryAnchorPane");
        }
        return this.summaryAnchorPane;
    }

    private AnchorPane getMainAnchorPane() {
        if (this.mainAnchorPane == null) {
            this.mainAnchorPane = (AnchorPane) this.scene.lookup("#mainAnchorPane");
        }
        return this.mainAnchorPane;
    }

    private Label getActiveProjectLabel() {
        if (this.labelActiveProject == null) {
            this.labelActiveProject = (Label) this.scene.lookup("#labelActiveProject");
        }
        return this.labelActiveProject;
    }

    private Label getDailySummaryLabel() {
        if (this.labelDailySumamry == null) {
            this.labelDailySumamry = (Label) this.stage.getScene().lookup("#labelDailySummary");
        }
        return this.labelDailySumamry;
    }

    private Label getSummaryLabel() {
        if (this.labelSummary == null) {
            this.labelSummary = (Label) this.stage.getScene().lookup("#labelSummary");
        }
        return this.labelSummary;
    }

    private AnchorPane getSprintSummaryAnchorPane() {
        if (this.sprintSummaryAnchorPane == null) {
            this.sprintSummaryAnchorPane = (AnchorPane) this.scene.lookup("#sprintSummaryAnchorPane");
        }
        return this.sprintSummaryAnchorPane;
    }
}
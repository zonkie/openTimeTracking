package eu.domroese.opentimetracking;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class TimeTrackingDatabase {

    private static TimeTrackingDatabase instance = null;


    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    private List<TimeTrackEntry> entries = new ArrayList<>();

    private Properties config = null;
    private Connection connection = null;

    private TimeTrackingDatabase() throws Exception {
        ConfigHandler conf = ConfigHandler.getInstance();
        this.config = conf.loadConfig();
        open();

    }

    public static TimeTrackingDatabase getInstance() throws Exception {
        if (instance == null) {
            instance = new TimeTrackingDatabase();
        }

        return instance;
    }

    public void createDatabaseTable() {
        try {
            if (this.connection == null) {
                open();
            }
            preparedStatement.executeQuery(
                    "CREATE TABLE `.timeTrackEntries` ("
                            + "`uniqueid` varchar(255) NOT NULL DEFAULT '',"
                            + "`project` varchar(255) DEFAULT NULL,"
                            + "`story` varchar(255) DEFAULT NULL,"
                            + "`task` varchar(255) DEFAULT NULL,"
                            + "`start` datetime DEFAULT NULL,"
                            + "`end` datetime DEFAULT NULL,"
                            + "PRIMARY KEY (`uniqueid`)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8"
            );
        } catch (Exception e) {
            Logger.log("Table 'timeTrackentries' was not created: " + e.getMessage());
        }
    }

    public void createNewEntry(
            String project,
            String story,
            String task
    ) throws Exception {
        try {
            if (this.connection == null) {
                open();
            }
            this.closeOpenTimeTrackEntries();
            /**
             * CREATE TABLE `.timeTrackEntries` (
             *   `uniqueid` varchar(255) NOT NULL DEFAULT '',
             *   `project` varchar(255) DEFAULT NULL,
             *   `story` varchar(255) DEFAULT NULL,
             *   `task` varchar(255) DEFAULT NULL,
             *   `start` datetime DEFAULT NULL,
             *   `end` datetime DEFAULT NULL,
             *   PRIMARY KEY (`uniqueid`)
             * ) ENGINE=InnoDB DEFAULT CHARSET=utf8
             */
            preparedStatement = this.connection.prepareStatement("insert into timeTrackEntries (uniqueId, project, story, task, start) values (uuid(), ?, ?, ?, now())");
            preparedStatement.setString(1, project);
            preparedStatement.setString(2, story);
            preparedStatement.setString(3, task);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            Logger.log("An exception occured while inserting the timeTrackEntry. " + e.getMessage());
            close();
            throw e;
        }
    }

    public void closeOpenTimeTrackEntries() throws Exception {
        try {
            if (this.connection == null) {
                open();
            }
            preparedStatement = this.connection.prepareStatement("UPDATE timeTrackEntries SET end=now() WHERE end IS NULL");
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            Logger.log("An exception occured while closing all open TimeTrackEntries. " + e.getMessage());
            close();
            throw e;
        }
    }

    /**
     * By Project navigation
     **/
    public List<TimeTrackSummary> fetchSumByProject() throws Exception {

        try {
            if (this.connection == null) {
                open();
            }
            // Statements allow to issue SQL queries to the database
            statement = this.connection.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement.executeQuery("select project, left(start, 10) as date, sum(time_to_sec(timediff(end, start))) as seconds FROM timeTrackEntries WHERE start >= current_date()-1 GROUP BY project, left(start, 10) ORDER BY date, project;");

            return this.createListFromResultSet(resultSet);
        } catch (Exception e) {
            Logger.log("An exception occured while reading the summary by project. " + e.getMessage());
            close();
            throw e;
        }
    }

    public Integer fetchSecondsForProjectAndDate(String project, String date) throws Exception {

        try {
            if (this.connection == null) {
                open();
            }
            // Statements allow to issue SQL queries to the database
            statement = this.connection.createStatement();
            // Result set get the result of the SQL query
            preparedStatement = this.connection.prepareStatement("select sum(time_to_sec(timediff(end, start))) as seconds FROM timeTrackEntries WHERE project = '" + project + "' AND left(start, 10) = '" + date + "'");
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getInt("seconds");
            }

        } catch (Exception e) {
            Logger.log("An exception occured while reading the seconds per Project and Day. " + e.getMessage());
        }

        return 0;
    }

    public String getActiveProject() throws Exception {
        try {
            if (this.connection == null) {
                open();
            }
            // Statements allow to issue SQL queries to the database
            statement = this.connection.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement.executeQuery("select project, story, task FROM timeTrackEntries WHERE end IS NULL");
            while (resultSet.next()) {

                String storyLabel = resultSet.getString("story");
                String taskLabel = resultSet.getString("task");
                String task = "";
                String story = "";

                if (!storyLabel.equals("")) {
                    story = "(" + storyLabel + ")";
                }
                if (!taskLabel.equals("")) {
                    task = "(" + taskLabel + ")";
                }

                return resultSet.getString("project")
                        + story
                        + task
                        ;
            }
        } catch (Exception e) {
            Logger.log("An exception occured while reading active project! " + e.getMessage());
        }
        return "";

    }

    public List<TimeTrackSummary> getLastCompleteEntry() throws Exception {
        try {
            if (this.connection == null) {
                open();
            }
            // Statements allow to issue SQL queries to the database
            statement = this.connection.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement.executeQuery("select project, story, task, sum(time_to_sec(timediff(end, start))) as seconds, left(start,10) as date FROM timeTrackEntries WHERE end IS NOT NULL GROUP BY project, story, task, start ORDER BY start DESC LIMIT 1;");
            return createListFromResultSet(resultSet);
        } catch (Exception e) {
            Logger.log("An exception occured while reading last complete Entry! " + e.getMessage());
        }
        return new ArrayList<>();

    }


    public List<TimeTrackSummary> getSprintSummary(String startDate, String endDate) {
        try {
            if (this.connection == null) {
                open();
            }
            statement = this.connection.createStatement();
            preparedStatement = this.connection.prepareStatement("select project, sum(time_to_sec(timediff(end, start))) as seconds, '' as date from timeTrackEntries WHERE project != 'Pause' and start >=? AND end <=? GROUP BY project");
            preparedStatement.setString(1, startDate + " 00:00:00");
            preparedStatement.setString(2, endDate + " 23:59:59");

            resultSet = preparedStatement.executeQuery();
            return createListFromResultSet(resultSet);
        } catch (Exception e) {
            Logger.log("An exception occured while reading the sprint summary! " + e.getMessage());
        }
        return new ArrayList<>();

    }
    /** By Project Navigation END **/

    /**
     * By Day navigation
     **/
    public List<DailySummary> fetchSumByDay() throws Exception {

        try {
            if (this.connection == null) {
                open();
            }
            // Statements allow to issue SQL queries to the database
            statement = this.connection.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement.executeQuery("select left(start, 10) as date, sum(time_to_sec(timediff(end, start))) as seconds FROM timeTrackEntries WHERE start >= current_date()-7 GROUP BY left(start, 10) ORDER BY date;");

            return this.createDailySummaryFromResultSet(resultSet);
        } catch (Exception e) {
            Logger.log("An exception occured while reading the Sum by Day. " + e.getMessage());
            close();
            throw e;
        }
    }

    public List<TimeTrackEntry> fetchAllForDay() throws Exception {

        try {
            if (this.connection == null) {
                open();
            }

            // Statements allow to issue SQL queries to the database
            statement = this.connection.createStatement();
            // Result set get the result of the SQL query
            resultSet = statement.executeQuery("select uniqueid, project, story, task, start, end FROM timeTrackEntries WHERE start >= current_date() ORDER BY start ASC;");

            return this.createEntryListFromResultSet(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log("An exception occured while reading the List for today. " + e.getMessage());
            close();
            throw e;
        }
    }
    /* By Day Navigation END **/

    /**
     * List Results
     **/
    private List<TimeTrackSummary> createListFromResultSet(ResultSet resultSet) throws Exception {
        List<TimeTrackSummary> entries = new ArrayList<>();
        //if(resultSet.)
        while (resultSet.next()) {
            String project = resultSet.getString("project");

            String date = resultSet.getString("date");
            Float seconds = resultSet.getFloat("seconds");
            TimeTrackSummary sum = new TimeTrackSummary(project, "", "", date, seconds);
            entries.add(sum);
        }
        return entries;

    }

    private List<TimeTrackEntry> createEntryListFromResultSet(ResultSet resultSet) throws Exception {
        List<TimeTrackEntry> entries = new ArrayList<>();
        //if(resultSet.)
        while (resultSet.next()) {
            String uniqueid = resultSet.getString("uniqueid");
            String project = resultSet.getString("project");
            String story = resultSet.getString("story");
            String task = resultSet.getString("task");
            Date startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse( resultSet.getString("start") );
            Date endDate = new Date();

            try {
                endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse( resultSet.getString("end") );
            } catch (Exception e){
                endDate = new Date();
            }

System.out.println("uniqueid");
System.out.println(uniqueid);
System.out.println("project");
System.out.println(project);
System.out.println("story");
System.out.println(story);
System.out.println("task");
System.out.println(task);
System.out.println("startDate");
System.out.println(startDate);
System.out.println("endDate");
System.out.println(endDate);

            TimeTrackEntry sum = new TimeTrackEntry(uniqueid, project, story, task, startDate, endDate);
            entries.add(sum);
        }
        return entries;
    }

    private List<DailySummary> createDailySummaryFromResultSet(ResultSet resultSet) throws Exception {
        List<DailySummary> entries = new ArrayList<>();
        while (resultSet.next()) {
            String date = resultSet.getString("date");
            Float seconds = resultSet.getFloat("seconds");
            Float secondsPause = (float) this.fetchSecondsForProjectAndDate("Pause", date);
            Float secondsWithoutPause = seconds - secondsPause;
            DailySummary sum = new DailySummary(date, seconds, secondsWithoutPause, (float) 7.7);
            entries.add(sum);
        }
        return entries;
    }

    /**
     * List Results END
     **/

    private void open() throws Exception {
        if (this.connection != null) {
            return;
        }
        try {
            Logger.log("Connect to Database");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.config.getProperty("mysqlHost") + "/openTimeTracking?user=" + this.config.getProperty("mysqlUser") + "&password=" + this.config.getProperty("mysqlPassword") + "&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
            if (this.connection != null) {
                Logger.log("Connection established");
            }
        } catch (Exception e) {
            Logger.log(e.getMessage());
            close();
            throw e;
        }
    }

    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (this.connection != null) {
                this.connection.close();
            }
        } catch (Exception e) {

        }
    }

}

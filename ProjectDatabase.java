package eu.domroese.opentimetracking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ProjectDatabase {

    private static ProjectDatabase instance = null;


    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    private List<Project> allProjects = new ArrayList<>();

    private Properties config = null;
    private Connection connection = null;

    private ProjectDatabase() throws Exception {
        ConfigHandler conf = ConfigHandler.getInstance();
        this.config = conf.loadConfig();

        open();
    }


    public static ProjectDatabase getInstance() throws Exception {
        if (instance == null) {
            instance = new ProjectDatabase();
        }

        return instance;
    }

    /**
     * CREATE TABLE `projects` (
     *   `uniqueId` varchar(45) NOT NULL,
     *   `project` varchar(45) DEFAULT NULL,
     *   PRIMARY KEY (`uniqueId`),
     *   UNIQUE KEY `project` (`project`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
     *
     * ALTER TABLE `openTimeTracking`.`projects`
     * ADD COLUMN `color` VARCHAR(7) NULL AFTER `project`;
     *
     *
     * @return
     * @throws Exception
     */
    public List<Project> getProjects() throws Exception {
        try {
            if (this.connection == null) {
                open();
            }
            statement = this.connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM projects;");

            List<Project> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(new Project(resultSet.getString("uniqueId"), resultSet.getString("project"), resultSet.getString("color")));
            }
            return result;

        } catch (Exception e) {
            System.out.println("An exception occured while inserting the timeTrackEntry. " + e.getMessage());
            close();
            throw e;
        }
    }

    private void open() throws SQLException {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.config.getProperty("mysqlHost") + "/openTimeTracking?user=" + this.config.getProperty("mysqlUser") + "&password=" + this.config.getProperty("mysqlPassword") + "&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
            if (this.connection != null) {
                System.out.println("Connection established");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
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

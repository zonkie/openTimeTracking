package eu.domroese.opentimetracking;

import java.io.*;
import java.util.Properties;

public class ConfigHandler {

    private static ConfigHandler instance = null;

    private ConfigHandler(){}

    public static ConfigHandler getInstance(){
        if( instance == null){
            instance = new ConfigHandler();
        }

        return instance;
    }

    public Properties loadConfig() {
        File configFile = new File("config.properties");

        try {
            FileReader reader = new FileReader(configFile);
            Properties props = new Properties();
            props.load(reader);
            reader.close();

            return props;
        } catch (FileNotFoundException ex) {
            // file does not exist
            this.createConfigDummy();
        } catch (IOException ex) {
            // I/O error
        }
        return null;
    }

    public void setConfigValue(String propName, String value){
        try {
            File configFile = new File("config.properties");

            FileReader reader = new FileReader(configFile);
            Properties props = new Properties();
            props.load(reader);
            reader.close();

            props.setProperty(propName, value);

            FileWriter writer = new FileWriter(configFile);
            props.store(writer, "Database settings");
            writer.close();

        } catch (FileNotFoundException ex) {
            // file does not exist
        } catch (IOException ex) {
            // I/O error
        }
    }

    private void createConfigDummy() {
        File configFile = new File("config.properties");

        try {
            Properties props = new Properties();
            props.setProperty("mysqlHost", "mysql.server.local");
            props.setProperty("mysqlUser", "someuser");
            props.setProperty("mysqlPassword", "someVerySecurePassword");
            props.setProperty("mysqlPort", "3306");
            FileWriter writer = new FileWriter(configFile);
            props.store(writer, "Database settings");
            writer.close();
        } catch (FileNotFoundException ex) {
            // file does not exist
        } catch (IOException ex) {
            // I/O error
        }
    }
}

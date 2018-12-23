package eu.domroese.opentimetracking;

public class DailySummary {

    private String date;
    private Float seconds;
    private Float secondsWithoutBreaks;
    private Float hoursExpected;

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

    public Float getSecondsWithoutBreaks() {
        return secondsWithoutBreaks;
    }

    public void setSecondsWithoutBreaks(Float secondsWithoutBreaks) {
        this.secondsWithoutBreaks = secondsWithoutBreaks;
    }

    public Float getHoursExpected() {
        return hoursExpected;
    }

    public void setHoursExpected(Float hoursExpected) {
        this.hoursExpected = hoursExpected;
    }

    public DailySummary(String date, Float seconds, Float secondsWithoutBreaks, Float hoursExpected) {
        this.date = date;
        this.seconds = seconds;
        this.secondsWithoutBreaks = secondsWithoutBreaks;
        this.hoursExpected = hoursExpected;
    }
}

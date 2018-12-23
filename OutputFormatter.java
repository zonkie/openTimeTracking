package eu.domroese.opentimetracking;

public class OutputFormatter {

    public static String formatSummary(TimeTrackSummary sum) {

        int minutes = Math.round(sum.getSeconds() / 60);
        return sum.getDate() + " - " + sum.getProject() + ": " + String.valueOf(minutes) + " min" + System.getProperty("line.separator");
    }

    public static String formatDailySummary(DailySummary sum) {

        float hoursTotal = (sum.getSeconds() / 60) / 60;
        float hoursTotalNoPause = (sum.getSecondsWithoutBreaks() / 60) / 60;
        float hoursExpected = sum.getHoursExpected();
        float hoursOver = sum.getHoursExpected() - hoursTotalNoPause;
        java.text.DecimalFormat number_format = new java.text.DecimalFormat("#.#");

        String result = sum.getDate() + ": "
                + number_format.format(hoursTotal) + " hours, "
                + number_format.format(hoursTotalNoPause) + " worked, ";
        if (hoursOver > 0) {
            result = result + number_format.format(hoursOver) + " hours left" + System.getProperty("line.separator");

        } else {
            result = result + number_format.format(hoursOver) + " worked more than needed" + System.getProperty("line.separator");
        }
        return result;

    }

}

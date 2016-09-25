package name.nanek.changimon.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * API response returned from SITA.
 * Created by Lance on 9/24/16.
 */

public class FlightRecordResponse {

    // E.g. true
    public boolean success;

    // E.g. SIN
    public String airportCode;

    // E.g. BA
    public String airlineCode;

    // E.g. 0012
    public String flightNumber;

    // E.g. 2016-09-25
    public String flightDate;

    // E.g. D
    public String adi;

    public List<FlightRecord> flightRecord;

    public String getAdiDisplayString() {
        if (null == adi) {
            return "";
        }
        if ("D".equals(adi)) {
            return "Departing";
        }
        if ("A".equals(adi)) {
            return "Arriving";
        }
        return "Unknown";
    }

    public String getDisplayString() {

        String flightInfoDisplay = "";

        // E.g. BA0012
        flightInfoDisplay += airlineCode;
        flightInfoDisplay += " " + flightNumber;
        flightInfoDisplay += "\n";

        // E.g. Departing 2016-09-25
        flightInfoDisplay += getAdiDisplayString();
        flightInfoDisplay += " " + flightDate;
        flightInfoDisplay += "\n";

        if (null != flightRecord && !flightRecord.isEmpty()) {
            final FlightRecord lastRecord = flightRecord.get(flightRecord.size() - 1);

            flightInfoDisplay += "Status " + lastRecord.statusText + "\n";
            flightInfoDisplay += "Terminal " + lastRecord.terminal;
            flightInfoDisplay += " " + getTime(lastRecord.scheduled);
        }
        return flightInfoDisplay;
    }

    private String getTime(Date date) {
        if ( null == date ) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String shortTimeStr = sdf.format(date);
        return shortTimeStr;
    }

    @Override
    public String toString() {
        return "FlightRecordResponse{" +
                "success=" + success +
                ", airportCode='" + airportCode + '\'' +
                ", airlineCode='" + airlineCode + '\'' +
                ", flightNumber='" + flightNumber + '\'' +
                ", flightDate=" + flightDate +
                ", adi='" + adi + '\'' +
                ", flightRecord=" + flightRecord +
                '}';
    }
}

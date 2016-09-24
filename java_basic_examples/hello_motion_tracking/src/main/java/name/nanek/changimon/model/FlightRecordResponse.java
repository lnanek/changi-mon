package name.nanek.changimon.model;

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

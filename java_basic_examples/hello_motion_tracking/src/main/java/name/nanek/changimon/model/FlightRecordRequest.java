package name.nanek.changimon.model;

/**
 * A request for a flight record.
 *
 * Created by Lance on 9/24/16.
 */
public class FlightRecordRequest {

    private static final String URL =
        "https://flifo-qa.api.aero/flifo/v3/flight/sin/";

    // E.g. BA
    public String airline;

    // E.g. 0012
    public String flightNumber;

    // E.g. https://flifo-qa.api.aero/flifo/v3/flight/sin/ba/0012/d
    public String generateUrl() {
        return URL + airline.toLowerCase() + "/" + flightNumber.toLowerCase() + "/d";
    }

}

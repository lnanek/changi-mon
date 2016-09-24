package name.nanek.changimon.model;

/**
 * Carrier part of FlightRecordResponse.
 *
 * Created by Lance on 9/24/16.
 */
public class CarrierRecord {

    // E.g. BA
    public String airlineCode;

    // E.g. British Airways
    public String airline;

    // E.g. 12
    public String flightNumber;

    @Override
    public String toString() {
        return "CarrierRecord{" +
                "airlineCode='" + airlineCode + '\'' +
                ", airline='" + airline + '\'' +
                ", flightNumber='" + flightNumber + '\'' +
                '}';
    }
}

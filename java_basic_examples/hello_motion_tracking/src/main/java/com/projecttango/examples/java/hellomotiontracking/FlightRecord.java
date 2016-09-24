package com.projecttango.examples.java.hellomotiontracking;

/**
 * Created by Lance on 9/24/16.
 */

public class FlightRecord {

    public CarrierRecord operatingCarrier;

    // E.g. LHR
    public String airportCode;

    // E.g. 388
    public String aircraft;

    // E.g. SC
    public String status;

    // E.g. Scheduled
    public String statusText;

    // E.g. 2016-09-25T23:20:00+0800
    public String scheduled;

    // E.g. London
    public String city;

    // E.g. 810
    public String duration;

    // E.g. 1
    public String terminal;

    @Override
    public String toString() {
        return "FlightRecord{" +
                "operatingCarrier=" + operatingCarrier +
                ", airportCode='" + airportCode + '\'' +
                ", aircraft='" + aircraft + '\'' +
                ", status='" + status + '\'' +
                ", statusText='" + statusText + '\'' +
                ", scheduled='" + scheduled + '\'' +
                ", city='" + city + '\'' +
                ", duration='" + duration + '\'' +
                ", terminal='" + terminal + '\'' +
                '}';
    }
}

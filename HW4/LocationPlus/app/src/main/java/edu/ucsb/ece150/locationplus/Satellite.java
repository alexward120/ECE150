package edu.ucsb.ece150.locationplus;

import android.location.GnssStatus;
import android.os.Build;

/*
 * This class is provided as a way for you to store information about a single satellite. It can
 * be helpful if you would like to maintain the list of satellites using an ArrayList (i.e.
 * ArrayList<Satellite>). As in Homework 3, you can then use an Adapter to update the list easily.
 *
 * You are not required to implement this if you want to handle satellite information in using
 * another method.
 */
public class Satellite {

    // TODO [DONE] Define private member variables
    int id;
    float azimuthAngle;
    float elevationAngle;
    float carrierFrequency;
    float carrierNoiseDensity;
    String constellationName;
    int SVID;

    public Satellite(GnssStatus status, int id) {
        this.id = id;
        this.azimuthAngle = status.getAzimuthDegrees(id);
        this.elevationAngle = status.getElevationDegrees(id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.carrierFrequency = status.getCarrierFrequencyHz(id);
        }
        this.carrierNoiseDensity = status.getCn0DbHz(id);
        this.SVID = status.getSvid(id);
        int type = status.getConstellationType(id);
        switch (type) {
            case GnssStatus.CONSTELLATION_BEIDOU:
                this.constellationName = "BEIDOU";
                break;
            case GnssStatus.CONSTELLATION_GALILEO:
                this.constellationName = "GALILEO";
                break;
            case GnssStatus.CONSTELLATION_GLONASS:
                this.constellationName = "GLONASS";
                break;
            case GnssStatus.CONSTELLATION_GPS:
                this.constellationName = "GPS";
                break;
            case GnssStatus.CONSTELLATION_QZSS:
                this.constellationName = "QZSS";
                break;
            case GnssStatus.CONSTELLATION_SBAS:
                this.constellationName = "SBAS";
                break;
            default:
                this.constellationName = "UNKNOWN";
                break;
        }

    }

    // [TODO [DONE] Implement the toString() method. When the Adapter tries to assign names to items
    // in the ListView, it calls the toString() method of the objects in the ArrayList
    @Override
    public String toString() {
        return "Satellite " + id;
    }

    public String showInfo() {
        return "Azimuth: " + azimuthAngle + " °\n" +
                "Elevation: " + elevationAngle + " °\n\n" +
                "Carrier Frequency: " + carrierFrequency + " Hz\n" +
                "C/N0: " + carrierNoiseDensity + " db Hz\n\n" +
                "Constellation Name: " + constellationName + "\n" +
                "SVID: " + SVID + "\n";
    }
}

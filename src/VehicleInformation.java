/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sample6_mapkit;

import java.awt.Color;
import org.jxmapviewer.viewer.GeoPosition;

/**
 *
 * @author goebel
 */
public class VehicleInformation {

    private double lat;
    private double lon;
    private String name;
    private String id;
    private long timens;
    private double speed;
    private double heading;
    private long lastEmergencyRectimeNs = 0;
    private long lastEmergencyMessageId = 0;
    //private GeoPosition pos;

    public VehicleInformation(long timens, String name, double lat, double lon, double speed, double heading) {
        this.lat = lat;
        this.lon = lon;
        //this.pos = new GeoPosition(lat,lon);
        this.name = name;
        this.timens = timens;
        this.speed = speed;
        this.heading = heading;
        id = name.split("_")[1];
    }

    public VehicleInformation(String[] splits) {
        timens = Long.parseLong(splits[1]);
        name = splits[2];
        lat = Double.parseDouble(splits[3]);
        lon = Double.parseDouble(splits[4]);
        //pos = new GeoPosition(lat,lon);
        speed = Double.parseDouble(splits[5]);
        heading = Double.parseDouble(splits[6]);
        id = name.split("_")[1];
    }

    public void update(String[] splits) {
        timens = Long.parseLong(splits[1]);
        lat = Double.parseDouble(splits[3]);
        lon = Double.parseDouble(splits[4]);
        //pos = new GeoPosition(lat,lon);
        speed = Double.parseDouble(splits[5]);
        heading = Double.parseDouble(splits[6]);
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getName() {
        return name;
    }

    public long getTimens() {
        return timens;
    }

    public double getSpeed() {
        return speed;
    }

    public double getHeading() {
        return heading;
    }

    public GeoPosition getGeoPosition() {
        return new GeoPosition(lat, lon);
    }

    public String toString() {
        return name + " at " + timens / 1000000000.0 + "s at lat/lon: " + lat + "/" + lon;
    }

    public Color getColor(long curtimens) {
        if (this.name.equals("veh_4")) {
            return Color.BLUE;
        } else if (curtimens - lastEmergencyRectimeNs < 500000000) { //less than 500ms after receiving emergency message
            return Color.RED;
        } else {
            return Color.GREEN;
        }
    }

    public String getId() {
        return id;
    }

    public void emergencyMessageReceived(long tstampns, long id) {
        lastEmergencyMessageId = id;
        lastEmergencyRectimeNs = tstampns;
    }

}

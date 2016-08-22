package com.cognizant.collab.collabtrak;

/**
 * Created by Joel Fraga on 8/15/16.
 */
public class BeaconPing {

    private String beaconUUID;
    private PingType type;

    public BeaconPing(){

    }

    public BeaconPing(String beaconUUID, PingType type) {
        this.beaconUUID = beaconUUID;
        this.type = type;
    }

    public String getBeaconUUID() {
        return beaconUUID;
    }

    public void setBeaconUUID(String beaconUUID) {
        this.beaconUUID = beaconUUID;
    }

    public PingType getType() {
        return type;
    }

    public void setType(PingType type) {
        this.type = type;
    }
}


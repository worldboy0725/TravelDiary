package com.example.map1app;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ShowAddress {
    private Geocoder gc;
    private List<Address> lstAddress;

    public Address getAddrList(Context context, double lat, double lng) {
        Geocoder gc = new Geocoder(context, Locale.TRADITIONAL_CHINESE);
        List<Address> lstAddress = null;
        try {
            lstAddress = gc.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address addrList = lstAddress.get(0);
        return addrList;
    }
}
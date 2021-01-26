package com.cdap.safetyapp.Interface;

import com.cdap.safetyapp.MyLatLng;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import com.cdap.safetyapp.MyLatLng;

public interface IOnLocationListener {
    void onLoadLocationSuccess(List<MyLatLng> latLngs);
    void onLoadLocationFailed(String message);

}

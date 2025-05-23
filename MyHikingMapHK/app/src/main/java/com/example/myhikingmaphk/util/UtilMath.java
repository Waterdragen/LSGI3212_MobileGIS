package com.example.myhikingmaphk.util;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class UtilMath {
    private static final double MIN_LAT = -90.0;
    private static final double MAX_LAT = 90.0;
    private static final double MIN_LON = -180.0;
    private static final double MAX_LON = 180.0;
    private static final double CIRCUMFERENCE_OF_EARTH = 40000.0;
    public static LatLng getCenterPointOfPoints(List<LatLng> points) {
        double sumLat = 0;
        double sumLng = 0;
        for (LatLng point : points) {
            sumLat += point.latitude;
            sumLng += point.longitude;
        }

        int numPoints = points.size();
        double centerLat = sumLat / numPoints;
        double centerLng = sumLng / numPoints;

        return new LatLng(centerLat, centerLng);
    }

    public static float getZoomLevel(List<LatLng> points) {
        double minLon = MAX_LON;
        double maxLon = MIN_LON;
        for (LatLng point : points) {
            minLon = Math.min(minLon, point.longitude);
            maxLon = Math.max(maxLon, point.longitude);
        }
        // deg = 110.574 km ~= 100 km
        double extentKm = (maxLon - minLon) * 100;
        // zl = log_2(c / km)
        double zoomLevel = Math.log(CIRCUMFERENCE_OF_EARTH / extentKm) / Math.log(2.0);
        // We zoom out
        zoomLevel -= 0.5;
        // 1 <= zl <= 20
        return (float) Math.max(1.0f, Math.min(20.0f, zoomLevel));
    }
}

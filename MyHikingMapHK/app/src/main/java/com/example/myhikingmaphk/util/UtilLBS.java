package com.example.myhikingmaphk.util;

import static android.content.Context.LOCATION_SERVICE;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

public class UtilLBS {
    public static class Checker {
        Context context;
        Checker(Context context) {
            this.context = context;
        }

        public static boolean validate(Context context) {
            Checker checker = new Checker(context);
            return checker.hasPermission() && checker.hasProvider();
        }

        private boolean hasPermission() {
            int accessFineLocation = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION);
            int accessCoarseLocation = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION);
            int permissionGranted = PackageManager.PERMISSION_GRANTED;
            return accessFineLocation == permissionGranted || accessCoarseLocation == permissionGranted;
        }

        private boolean hasProvider() {
            LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            if (locationManager == null) {
                return false;
            }
            return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        }
    }

    public static void askLocationPermissions(Context context) {
        Dialog dialog = UtilDialog.makeYesOrNoDialog(context, "Set location permissions?",
            "This app requires location permission to function properly.\nWould you like to enable it now?",
                () -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                },
                null
        );
        dialog.show();
    }
}

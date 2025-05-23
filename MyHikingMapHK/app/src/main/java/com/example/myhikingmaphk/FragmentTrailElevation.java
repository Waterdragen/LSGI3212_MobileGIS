package com.example.myhikingmaphk;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myhikingmaphk.util.AreaGraph;
import com.example.myhikingmaphk.util.TrailsCSV.Field;
import com.example.myhikingmaphk.util.UtilApi;
import com.example.myhikingmaphk.util.UtilDialog;
import com.google.android.gms.maps.model.LatLng;

import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FragmentTrailElevation extends Fragment {

    private final String[] trail;
    private static final String HIKING_URL_START = "https://services3.arcgis.com/6j1KwZfY2fZrfNMR/arcgis/rest/services/Hiking_Trails_in_Country_Parks_of_Hong_Kong/FeatureServer/0/query?where=Trail_ID%20%3D%20'";
    private static final String HIKING_URL_END = "'&outFields=&outSR=4326&f=geojson";
    private static final String URL = "https://elevation.trailwatch.hk/";
    private static final Map<String, String> HEADERS = initHeaders();
    private Map<String, String> data = initData();
    private final CompletableFuture<UtilApi.Response> resp;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public FragmentTrailElevation(String[] trail) {
        // Required empty public constructor
        this.trail = trail;
        String url = HIKING_URL_START +
                trail[Field.TrailId.ordinal()].toUpperCase() +
                HIKING_URL_END;
        UtilApi.Response resp_ = UtilApi.Response.get(url);
        resp = resp_.thenAsyncResponse((String result) -> {
            if (result == null) {
                showDialog("Error", "(Unable to get polyline from esri)");
                return null;
            }
            StringBuilder pointsBuf = new StringBuilder();
            try {
                JSONObject json = new JSONObject(result);
                JSONObject jsonGeometry = json
                        .getJSONArray("features")
                        .getJSONObject(0)
                        .getJSONObject("geometry");
                String lineType = jsonGeometry.getString("type");
                JSONArray jsonPoints = jsonGeometry.getJSONArray("coordinates");
                if (lineType.equals("MultiLineString")) {
                    jsonPoints = jsonPoints.getJSONArray(0);
                }
                int len = jsonPoints.length();
                for (int i = 0; i < len; i++) {
                    JSONArray jsonPoint = jsonPoints.getJSONArray(i);
                    float lon = (float) jsonPoint.getDouble(0);
                    float lat = (float) jsonPoint.getDouble(1);
                    pointsBuf.append(lat);
                    pointsBuf.append(',');
                    pointsBuf.append(lon);
                    pointsBuf.append(',');
                }
                String points = pointsBuf.substring(0, pointsBuf.length() - 1);
                data.put("latLngCollection", points);
                return UtilApi.Response.post(URL, HEADERS, data);
            } catch (JSONException e) {
                showDialog("Error", "(Unable to parse json)");
                return null;
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trail_elevation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        AreaGraph graph = view.findViewById(R.id.graphTrailElevation);
        resp.thenAccept((elevResp) -> elevResp.join((result) -> {
            if (result == null) {
                showDialog("Error", "(Unable to get elevation)");
                return;
            }
            List<Pair<Double, Double>> elevations = new ArrayList<>();
            try {
                JSONObject json = new JSONObject(result);
                JSONArray arr = json.getJSONArray("elevationProfile");
                int len = arr.length();
                for (int i = 0; i < len; i++) {
                    JSONObject elevation = arr.getJSONObject(i);
                    double distance = elevation.getDouble("distance");
                    double height = elevation.getDouble("height");
                    elevations.add(Pair.create(distance, height));
                }
            } catch (JSONException e) {
                showDialog("Error", "(Unable to get elevation)");
                return;
            }
            graph.setElevations(elevations);
        })).join();

        TextView startPt = view.findViewById(R.id.textTrailElevationStart);
        TextView endPt = view.findViewById(R.id.textTrailElevationEnd);
        startPt.setText(trail[Field.StartPt.ordinal()]);
        endPt.setText(trail[Field.EndPt.ordinal()]);
    }

    private static Map<String, String> initHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json, text/javascript");
        headers.put("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        return headers;
    }

    private static Map<String, String> initData() {
        Map<String, String> data = new HashMap<>();
        data.put("key", "Fmjtd%257Cluubnua22u%252C2s%253Do5-907luf");
        data.put("shapeFormat", "raw");
        data.put("latLngCollection", "");
        data.put("resolution", "0");
        return data;
    }

    public void showDialog(String title, String content) {
        Context context = this.getContext();
        uiHandler.post(() -> {
            Dialog dialog = UtilDialog.makeDialog(context, title, content);
            dialog.show();
        });
    }
}
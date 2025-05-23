package com.example.myhikingmaphk;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myhikingmaphk.util.TrailsCSV.Field;
import com.example.myhikingmaphk.util.UtilApi;
import com.example.myhikingmaphk.util.UtilDialog;
import com.example.myhikingmaphk.util.UtilLBS;
import com.example.myhikingmaphk.util.UtilMath;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FragmentTrailMap extends Fragment implements OnMapReadyCallback {
    private static final String HIKING_URL_START = "https://services3.arcgis.com/6j1KwZfY2fZrfNMR/arcgis/rest/services/Hiking_Trails_in_Country_Parks_of_Hong_Kong/FeatureServer/0/query?where=Trail_ID%20%3D%20'";
    private static final String HIKING_URL_END = "'&outFields=&outSR=4326&f=geojson";
    private static final int TRAIL_COLOR = 0xFFEF4040;
    private static final int PATH_COLOR = 0xFF4984B8;
    private final String[] trail;
    private final UtilApi.Response resp;
    private Polyline pathLine = null;
    private GoogleMap map = null;
    private MapView mapView;
    private Context context;
    private AtomicReference<String> info = new AtomicReference<>(null);
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private AutoCompleteTextView inputOrigin;
    private Spinner dropDownDest;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationClient;

    FragmentTrailMap(String[] trail) {
        this.trail = trail;
        String url = HIKING_URL_START +
                trail[Field.TrailId.ordinal()].toUpperCase() +
                HIKING_URL_END;
        this.resp = UtilApi.Response.get(url);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trail_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        mapView = view.findViewById(R.id.mapTrail);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        context = view.getContext();

        if (!Places.isInitialized()) {
            Places.initialize(context, getString(R.string.constGoogleMapApiKey));
        }
        placesClient = Places.createClient(context);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        inputOrigin = view.findViewById(R.id.inputTrailMapOrigin);
        setupAutoComplete(inputOrigin);

        dropDownDest = view.findViewById(R.id.dropdownTrailMapDest);
        String[] destOptions = {trail[Field.StartPt.ordinal()], trail[Field.EndPt.ordinal()]};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, destOptions);
        dropDownDest.setAdapter(adapter);

        ImageButton buttonSearch = view.findViewById(R.id.buttonTrailMapSearch);
        ImageButton buttonInfo = view.findViewById(R.id.buttonTrailMapInfo);
        ImageButton buttonLocation = view.findViewById(R.id.buttonTrailMapLocation);

        buttonSearch.setOnClickListener(_view -> {
            Thread thread = new Thread(() -> {
                removePathLine();
                info.set(null);
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                String origin = inputOrigin.getText().toString();
                if (origin.isEmpty()) {
                    showDialog("Error", "(Missing starting location)");
                    return;
                }

                String dest = dropDownDest.getSelectedItem().toString() + "(Hong Kong)";
                String url = UtilApi.dirUrl(origin, dest, getString(R.string.constGoogleMapApiKey));

                AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setQuery(dest)
                        .build();

                placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
                });
                UtilApi.Response resp = UtilApi.Response.get(url);
                resp.then(result -> {
                    try {
                        JSONObject json = new JSONObject(result);
                        String encodedPoints = json
                                .getJSONArray("routes")
                                .getJSONObject(0)
                                .getJSONObject("overview_polyline")
                                .get("points")
                                .toString();
                        JSONArray steps = json
                                .getJSONArray("routes")
                                .getJSONObject(0)
                                .getJSONArray("legs")
                                .getJSONObject(0)
                                .getJSONArray("steps");
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < steps.length(); i++) {
                            JSONObject step = steps.getJSONObject(i);
                            sb.append(step.get("html_instructions"));
                            sb.append('\n');
                        }
                        info.set(sb.toString());
                        Log.d("Debug", info.get());

                        drawPolylines(encodedPoints, PATH_COLOR, true);
                    } catch (JSONException e) {
                        showDialog("Error", "(Unable to parse json)");
                    }
                });
            });
            thread.start();
        });

        buttonInfo.setOnClickListener(_view -> {
                    String info = this.info.get();
                    showDialog("Path result",
                            info == null ? "(Unable to get path)" : info);
                }
        );

        buttonLocation.setOnClickListener(_view -> {
            if (!UtilLBS.Checker.validate(context)) {
                UtilLBS.askLocationPermissions(context);
                return;
            }
            Thread thread = new Thread(() -> {
                try {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(location -> {
                                String lat = String.valueOf(location.getLatitude());
                                String lon = String.valueOf(location.getLongitude());
                                String apiKey = getString(R.string.constGoogleMapApiKey);
                                String url = UtilApi.getRevGeoCodeApiUrl(lat, lon, apiKey);

                                UtilApi.Response resp = UtilApi.Response.get(url);
                                try {
                                    resp.then(result -> {
                                        try {
                                            JSONObject json = new JSONObject(result);
                                            String address = json.getJSONArray("results")
                                                    .getJSONObject(0)
                                                    .get("formatted_address")
                                                    .toString();
                                            uiHandler.post(() -> inputOrigin.setText(address));
                                        } catch (JSONException e) {
                                            showDialog("Error", "(Unable to parse json)");
                                        }
                                    });
                                } catch (RuntimeException e) {
                                    showDialog("Error", "(Unable to get current location)");
                                }
                            })
                            .addOnFailureListener(e -> UtilLBS.askLocationPermissions(context));
                } catch (SecurityException e) {
                    showDialog("Error", "(Unable to get current location)");
                }
            });
            thread.start();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;

        resp.then(result -> {
            if (result == null) {
                showDialog("Error", "(Unable to get polyline from esri)");
                return;
            }
            List<LatLng> points;
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
                points = new ArrayList<>();
                int len = jsonPoints.length();
                for (int i = 0; i < len; i++) {
                    JSONArray jsonPoint = jsonPoints.getJSONArray(i);
                    double lon = jsonPoint.getDouble(0);
                    double lat = jsonPoint.getDouble(1);
                    points.add(new LatLng(lat, lon));
                }
            } catch (JSONException e) {
                showDialog("Error", "(Unable to parse json)");
                return;
            }
            drawPolylines(points, TRAIL_COLOR, false);
        });
    }

    private void drawPolylines(String encodedPoints, int color, boolean isPath) {
        drawPolylines(PolyUtil.decode(encodedPoints), color, isPath);
    }

    private void drawPolylines(List<LatLng> points, int color, boolean isPath) {
        // show the decoded points
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(points);
        polylineOptions.color(color);
        polylineOptions.jointType(JointType.ROUND);
        polylineOptions.width(15f);

        this.getActivity().runOnUiThread(() -> {
            if (isPath) {
                pathLine = map.addPolyline(polylineOptions);
            } else {
                map.addPolyline(polylineOptions);
            }
            LatLng centerOfPolyline = UtilMath.getCenterPointOfPoints(points);
            float zoomLevel = UtilMath.getZoomLevel(points);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(centerOfPolyline, zoomLevel));

        });
    }

    private void removePathLine() {
        this.getActivity().runOnUiThread(() -> {
            if (pathLine != null) {
                pathLine.remove();
                pathLine = null;
            }
        });
    }

    public void showDialog(String title, String content) {
        Context context = this.getContext();
        uiHandler.post(() -> {
            Dialog dialog = UtilDialog.makeDialog(context, title, content);
            dialog.show();
        });
    }

    private void setupAutoComplete(AutoCompleteTextView textView) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line);
        assert textView != null;
        textView.setAdapter(adapter);
        textView.setThreshold(1);
        textView.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 1) {
                    fetchPlaceSuggestions(textView, s.toString());
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // When a suggestion is clicked, update the input field
        textView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPlace = (String) parent.getItemAtPosition(position);
            textView.setText(selectedPlace);
        });
    }

    private void fetchPlaceSuggestions(AutoCompleteTextView textView, String place) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(place)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) textView.getAdapter();
            adapter.clear();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                adapter.add(prediction.getFullText(null).toString());
            }
            adapter.notifyDataSetChanged();
            if (textView.isPopupShowing()) {
                adapter.getFilter().filter(textView.getText());
            } else {
                textView.showDropDown();
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public void onSaveInstanceState(@Nullable Bundle outState) {
        if (outState == null) {
            return;
        }
        super.onSaveInstanceState(outState);
        assert mapView != null;
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        if (mapView == null) {
            return;
        }
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        if (mapView == null) {
            return;
        }
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mapView == null) {
            return;
        }
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        if (mapView == null) {
            return;
        }
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
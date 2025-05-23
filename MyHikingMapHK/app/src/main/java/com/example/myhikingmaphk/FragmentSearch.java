package com.example.myhikingmaphk;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.myhikingmaphk.util.TrailsListRecycler;
import com.google.android.material.slider.RangeSlider;

import com.example.myhikingmaphk.util.TrailsCSV;
import com.example.myhikingmaphk.util.TrailsCSV.Filterer;

import java.util.Objects;

public class FragmentSearch extends Fragment {
    private Context context;
    private TextView textDifficultyRange;
    private TextView textLengthRange;
    private RecyclerView recyclerView;
    private int minDifficulty = 1;
    private int maxDifficulty = 5;
    private float minLength = 0.0f;
    private float maxLength = 20.0f;
    private String region = "Any";
    private String trailType = "Any";
    private Filterer filterer = new Filterer();
    private static final String ANY = "Any";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        RangeSlider sliderDifficulty = view.findViewById(R.id.sliderSearchDifficulty);
        textDifficultyRange = view.findViewById(R.id.textSearchDifficultyRange);
        RangeSlider sliderLength = view.findViewById(R.id.sliderSearchLength);
        textLengthRange = view.findViewById(R.id.textSearchLengthRange);
        Spinner dropdownRegion = view.findViewById(R.id.dropdownSearchRegion);
        Spinner dropdownType = view.findViewById(R.id.dropdownSearchType);
        recyclerView = view.findViewById(R.id.recyclerSearch);
        context = view.getContext();

        sliderDifficulty.setLabelFormatter(value -> TrailsCSV.DIFFICULTY_NAMES[Math.round(value)]);

        sliderDifficulty.addOnChangeListener((slider, value, fromUser) -> {
            minDifficulty = Math.round(slider.getValues().get(0));
            maxDifficulty = Math.round(slider.getValues().get(1));
            updateDifficulty();
        });

        sliderLength.addOnChangeListener((slider, value, fromUser) -> {
            minLength = slider.getValues().get(0);
            maxLength = slider.getValues().get(1);
            updateLength();
        });

        dropdownRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newRegion = parent.getItemAtPosition(position).toString();
                if (region.equals(newRegion)) {
                    return;
                }
                region = newRegion;
                updateRegion();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        dropdownType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newTrailType = parent.getItemAtPosition(position).toString();
                if (trailType.equals(newTrailType)) {
                    return;
                }
                trailType = newTrailType;
                updateTrailType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        updateDifficulty();
        updateLength();
        updateRegion();
        updateTrailType();
    }

    private void updateRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        TrailsListRecycler recycler = new TrailsListRecycler(context, filterer.filter());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recycler);
    }

    private void updateDifficulty() {
        if (maxDifficulty - minDifficulty == 4) {
            textDifficultyRange.setText(ANY);
            filterer.difficulty(null);
            updateRecycler();
            return;
        }
        String minDifficultyStr = TrailsCSV.DIFFICULTY_NAMES[minDifficulty];
        if (minDifficulty == maxDifficulty) {
            textDifficultyRange.setText(minDifficultyStr);
            filterer.difficulty(String.valueOf(minDifficulty));
            updateRecycler();
            return;
        }
        String maxDifficultyStr = TrailsCSV.DIFFICULTY_NAMES[maxDifficulty];
        textDifficultyRange.setText(String.format("%s to %s", minDifficultyStr, maxDifficultyStr));
        filterer.difficultyRange(Pair.create(minDifficulty, maxDifficulty));
        updateRecycler();
    }

    private void updateLength() {
        if (maxLength - minLength == 20.0f) {
            textLengthRange.setText(ANY);
            filterer.lengthRange(null);
        } else {
            textLengthRange.setText(String.format("%.1f to %.1f km", minLength, maxLength));
            filterer.lengthRange(Pair.create(minLength, maxLength));
        }
        updateRecycler();
    }

    private void updateRegion() {
        filterer.region(region.equals(ANY) ? null : region);
        updateRecycler();
    }

    private void updateTrailType() {
        filterer.type(trailType.equals(ANY) ? null : trailType);
        updateRecycler();
    }
}
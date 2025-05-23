package com.example.myhikingmaphk;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myhikingmaphk.util.TrailsCSV;
import com.example.myhikingmaphk.util.TrailsCSV.Field;

public class FragmentTrailInfo extends Fragment {
    private final String[] trail;

    FragmentTrailInfo(String[] trail) {
        this.trail = trail;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trail_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        TextView textDifficulty = view.findViewById(R.id.textTrailInfoDifficulty);
        TextView textEnd = view.findViewById(R.id.textTrailInfoEnd);
        TextView textLength = view.findViewById(R.id.textTrailInfoLength);
        TextView textName = view.findViewById(R.id.textTrailInfoName);
        TextView textRegion = view.findViewById(R.id.textTrailInfoRegion);
        TextView textStart = view.findViewById(R.id.textTrailInfoStart);
        TextView textType = view.findViewById(R.id.textTrailInfoType);

        textDifficulty.setText(TrailsCSV.DIFFICULTY_NAMES[Integer.parseUnsignedInt(getField(Field.Difficulty))]);
        textEnd.setText(getField(Field.EndPt));
        textLength.setText(getField(Field.Length) + " km");
        textName.setText(getField(Field.TrailName));
        textRegion.setText(getField(Field.Region));
        textStart.setText(getField(Field.StartPt));
        textType.setText(getField(Field.Type));
    }

    private String getField(Field field) {
        return trail[field.ordinal()];
    }
}
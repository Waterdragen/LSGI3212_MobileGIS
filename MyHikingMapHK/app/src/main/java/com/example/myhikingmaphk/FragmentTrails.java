package com.example.myhikingmaphk;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myhikingmaphk.util.TrailsListRecycler;
import com.example.myhikingmaphk.util.TrailsCSV;

public class FragmentTrails extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trails, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.recyclerTrails);
        TrailsListRecycler recycler = new TrailsListRecycler(context, TrailsCSV.TRAILS);
        recyclerView.setAdapter(recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
    }
}
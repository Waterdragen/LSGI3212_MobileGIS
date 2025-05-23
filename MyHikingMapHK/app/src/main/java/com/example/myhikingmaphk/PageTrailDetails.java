package com.example.myhikingmaphk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myhikingmaphk.databinding.ActivityPageTrailDetailsBinding;
import com.example.myhikingmaphk.util.UI;

import java.util.HashMap;

public class PageTrailDetails extends UI.BasePage {
    ActivityPageTrailDetailsBinding binding;
    int currentNavId = R.id.navTrailInfo;
    HashMap<Integer, Pair<Integer, Fragment>> bottomNavTable;
    String[] trail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDefault();

        binding = ActivityPageTrailDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        trail = intent.getStringArrayExtra("trailData");
        assert trail != null;
        bottomNavTable = initBottomNavTable();

        ImageButton buttonClose = findViewById(R.id.buttonTrailDetailsClose);
        buttonClose.setOnClickListener(v -> finish());

        initFragment(R.id.navTrailInfo);
        binding.bottomNavTrailDetails.setOnItemSelectedListener(item -> {
            int newNavId = item.getItemId();
            if (newNavId == currentNavId) {
                return false;
            }
            if (!bottomNavTable.containsKey(newNavId)) {
                return false;
            }
            slideFragment(newNavId);
            currentNavId = newNavId;
            return true;
        });
    }

    private void initFragment(int navId) {
        Pair<Integer, Fragment> tuple = bottomNavTable.get(navId);
        assert tuple != null;
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frameLayout, tuple.second)
                .commit();
    }

    private void slideFragment(int newNavId) {
        Pair<Integer, Fragment> currentTuple = bottomNavTable.get(currentNavId);
        Pair<Integer, Fragment> newTuple = bottomNavTable.get(newNavId);
        boolean shouldSlideRight = currentTuple.first < newTuple.first;
        Fragment currentFragment = currentTuple.second;
        Fragment newFragment = newTuple.second;
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (shouldSlideRight) {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        transaction.hide(currentFragment);
        if (newFragment.isAdded()) {
            transaction.show(newFragment);
        } else {
            transaction.add(R.id.frameLayout, newFragment);
        }
        transaction.commit();
    }

    private HashMap<Integer, Pair<Integer, Fragment>> initBottomNavTable() {
        HashMap<Integer, Pair<Integer, Fragment>> map = new HashMap<>();
        map.put(R.id.navTrailInfo, Pair.create(0, new FragmentTrailInfo(trail)));
        map.put(R.id.navTrailMap, Pair.create(1, new FragmentTrailMap(trail)));
        map.put(R.id.navTrailElevation, Pair.create(2, new FragmentTrailElevation(trail)));
        return map;
    }
}
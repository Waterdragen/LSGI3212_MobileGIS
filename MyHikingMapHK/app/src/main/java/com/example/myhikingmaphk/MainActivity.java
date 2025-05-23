package com.example.myhikingmaphk;

import android.os.Bundle;
import android.util.Pair;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myhikingmaphk.databinding.ActivityMainBinding;

import com.example.myhikingmaphk.util.UI;
import com.example.myhikingmaphk.util.TrailsCSV;

import java.util.HashMap;

public class MainActivity extends UI.BasePage {

    ActivityMainBinding binding;
    int currentNavId = R.id.navHome;
    static final HashMap<Integer, Pair<Integer, Fragment>> bottomNavTable = initBottomNavTable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDefault();
        TrailsCSV.init(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFragment(R.id.navHome);
        binding.bottomNav.setOnItemSelectedListener(item -> {
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

    private static HashMap<Integer, Pair<Integer, Fragment>> initBottomNavTable() {
        HashMap<Integer, Pair<Integer, Fragment>> map = new HashMap<>();
        map.put(R.id.navHome, Pair.create(0, new FragmentHome()));
        map.put(R.id.navTrails, Pair.create(1, new FragmentTrails()));
        map.put(R.id.navSearch, Pair.create(2, new FragmentSearch()));
        return map;
    }
}
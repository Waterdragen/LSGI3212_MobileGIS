package com.example.myhikingmaphk.util;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UI {
    public static class BasePage extends AppCompatActivity {
        protected BottomNavigationView bottomNavigationView;
        public void initDefault() {
            EdgeToEdge.enable(this);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }

        public void setSelectedNavigationItem(int itemId) {
            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(itemId);
            }
        }
    }

    public static void linkPage(View view, Class<?> dest) {
        view.setOnClickListener((_view) -> {
            Context src = view.getContext();
            Intent intent = new Intent(src, dest);
            src.startActivity(intent);
        });
    }

    public static void linkPageAndClear(View view, Class<?> dest) {
        view.setOnClickListener((_view) -> {
            Context src = view.getContext();
            Intent intent = new Intent(src, dest);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            src.startActivity(intent);
        });
    }
}

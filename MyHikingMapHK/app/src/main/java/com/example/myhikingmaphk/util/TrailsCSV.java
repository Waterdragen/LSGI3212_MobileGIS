package com.example.myhikingmaphk.util;

import android.content.Context;
import android.util.Pair;

import com.example.myhikingmaphk.R;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TrailsCSV {
    public static List<String[]> TRAILS = new ArrayList<>();
    public static String[] DIFFICULTY_NAMES = {"--", "Easy", "Moderate", "Demanding", "Difficult", "Very Difficult"};
    private static boolean isInitTrails = false;

    public static void init(Context context) {
        if (isInitTrails) {
            return;
        }
        try (InputStream inputStream = context.getResources().openRawResource(R.raw.trails);
             CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream))) {
            // Ignore header
            csvReader.readNext();
            TRAILS = csvReader.readAll();
            isInitTrails = true;
        } catch (IOException | CsvException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }
    }

    public enum Field {
        Name,
        SectionId,
        TrailId,
        Length,
        Difficulty,
        Region,
        Type,
        TrailName,
        ShapeLength,
        StartPt,
        EndPt
    }

    public static class Filterer {
        private String difficulty;
        private Pair<Integer, Integer> difficultyRange;
        private String type;
        private String region;
        private Pair<Float, Float> lengthRange;

        public Filterer difficulty(String difficulty) {
            this.difficulty = difficulty;
            difficultyRange = null;
            return this;
        }
        public Filterer difficultyRange(Pair<Integer, Integer> difficultyRange) {
            this.difficultyRange = difficultyRange;
            difficulty = null;
            return this;
        }
        public Filterer type(String type) {
            this.type = type;
            return this;
        }
        public Filterer region(String region) {
            this.region = region;
            return this;
        }
        public Filterer lengthRange(Pair<Float, Float> lengthRange) {
            this.lengthRange = lengthRange;
            return this;
        }

        public List<String[]> filter() {
            List<String[]> filteredTrails = new ArrayList<>();

            for (String[] trail : TRAILS) {
                if (difficulty != null && !trail[Field.Difficulty.ordinal()].equals(difficulty)) {
                    continue;
                }

                if (difficultyRange != null) {
                    int difficulty = Integer.parseUnsignedInt(trail[Field.Difficulty.ordinal()]);
                    if (outOfRange(difficulty, difficultyRange)) {
                        continue;
                    }
                }

                if (type != null && !trail[Field.Type.ordinal()].equals(type)) {
                    continue;
                }

                if (region != null && !trail[Field.Region.ordinal()].equals(region)) {
                    continue;
                }

                if (lengthRange != null) {
                    float length = Float.parseFloat(trail[Field.Length.ordinal()]);
                    if (outOfRange(length, lengthRange)) {
                        continue;
                    }
                }
                filteredTrails.add(trail);
            }

            return filteredTrails;
        }

        private static <T extends Comparable<T>> boolean outOfRange(T value, Pair<T, T> range) {
            return (value.compareTo(range.first) < 0) || (range.second.compareTo(value) < 0);
        }
    }
}

package com.example.myhikingmaphk.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AreaGraph extends View {
    private List<Pair<Double, Double>> elevations;
    private Paint areaFillPaint;
    private Paint areaStrokePaint;
    private Paint gridPaint;
    private Paint textPaint;
    private float padding = 120f;
    private int movingAverageWindow;

    public AreaGraph(Context context) {
        this(context, null);
    }

    public AreaGraph(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AreaGraph(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        areaFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        areaFillPaint.setColor(Color.argb(128, 81, 152, 114)); // Semi-transparent blue
        areaFillPaint.setStyle(Paint.Style.FILL);

        areaStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        areaStrokePaint.setColor(Color.rgb(81, 152, 114));
        areaStrokePaint.setStrokeWidth(10f);
        areaStrokePaint.setStyle(Paint.Style.STROKE);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
    }

    public void setElevations(List<Pair<Double, Double>> elevations) {
        this.elevations = elevations.stream()
                .filter(p -> p.second != 0.0)
                .collect(Collectors.toList());
        invalidate(); // Trigger a redraw
        Log.d("Debug", this.elevations.toString());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (elevations == null || elevations.isEmpty()) {
            drawNoDataMessage(canvas);
            return;
        }

        float width = getWidth() - (2 * padding);
        float height = getHeight() - (2 * padding);

        float maxDistance = Collections.max(elevations, Comparator.comparing((item) -> item.first)).first.floatValue();
        float maxHeight = Collections.max(elevations, Comparator.comparing((item) -> item.second)).second.floatValue();

        if (maxHeight == 0.0) {
            drawNoDataMessage(canvas);
            return;
        }

        List<Pair<Double, Double>> smoothedElevations = calculateMovingAverage(elevations);
        movingAverageWindow = smoothedElevations.size() / 20;

        Path path = new Path();
        path.moveTo(padding, height + padding);

        float count = 0;
        float len = smoothedElevations.size();
        for (Pair<Double, Double> elevation : smoothedElevations) {
//            float x = padding + (count / len * width);
            float x = padding + (float) (elevation.first / maxDistance * width);
            float y = height + padding - (float) (elevation.second / maxHeight * height);
            path.lineTo(x, y);
            count += 1;
        }

        path.lineTo(width + padding, height + padding);
        path.close();

        postInvalidateOnAnimation();
        canvas.drawPath(path, areaFillPaint);
        canvas.drawPath(path, areaStrokePaint);

        // Draw y-axis grid lines
        for (int i = 0; i <= 1000; i += 100) {
            float gridY = height + padding - (i / maxHeight * height);
            canvas.drawLine(padding, gridY, width + padding, gridY, gridPaint);
        }

        // Draw y-axis labels
        for (int i = 0; i <= 1000; i += 100) {
            float gridY = height + padding - (i / maxHeight * height);
            String label = String.valueOf(i);
            float textWidth = textPaint.measureText(label);
            canvas.drawText(label, padding - textWidth - 8, gridY + 4, textPaint);
        }
    }

    private List<Pair<Double, Double>> calculateMovingAverage(List<Pair<Double, Double>> elevations) {
        List<Pair<Double, Double>> smoothedElevations = new ArrayList<>();
        for (int i = 0; i < elevations.size(); i++) {
            double distanceSum = 0.0;
            double heightSum = 0.0;
            int count = 0;
            for (int j = Math.max(0, i - movingAverageWindow / 2);
                 j < Math.min(elevations.size(), i + movingAverageWindow / 2 + 1); j++) {
                distanceSum += elevations.get(j).first;
                heightSum += elevations.get(j).second;
                count++;
            }
            double averageDistance = distanceSum / count;
            double averageHeight = heightSum / count;
            smoothedElevations.add(Pair.create(averageDistance, averageHeight));
        }
        return smoothedElevations;
    }

    private void drawNoDataMessage(Canvas canvas) {
        String message = "No valid elevation data";
        float textSize = 24f;
        float textWidth = areaStrokePaint.measureText(message);
        float textHeight = areaStrokePaint.descent() - areaStrokePaint.ascent();
        float x = getWidth() / 2 - textWidth / 2;
        float y = getHeight() / 2 - textHeight / 2;
        canvas.drawText(message, x, y, areaStrokePaint);
    }
}

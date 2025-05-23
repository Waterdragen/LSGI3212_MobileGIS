package com.example.myhikingmaphk.util;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.myhikingmaphk.PageTrailDetails;
import com.example.myhikingmaphk.R;
import com.example.myhikingmaphk.util.TrailsCSV.Field;

public class TrailsListRecycler extends RecyclerView.Adapter<TrailsListRecycler.ViewHolder> {
    Context context;
    List<String[]> trails;

    public TrailsListRecycler(Context context, List<String[]> trails) {
        this.context = context;
        this.trails = trails;
    }

    @NonNull
    @Override
    // inflate the layout
    public TrailsListRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.trails_item, parent, false);
        return new ViewHolder(view, trails);
    }

    @Override
    // assigning values to the views
    public void onBindViewHolder(@NonNull TrailsListRecycler.ViewHolder holder, int position) {
        holder.trailNameText.setText(trails.get(position)[Field.TrailName.ordinal()]);
        String difficulty = trails.get(position)[Field.Difficulty.ordinal()];
        String difficultyName = TrailsCSV.DIFFICULTY_NAMES[Integer.parseUnsignedInt(difficulty)];
        holder.difficultyText.setText(difficultyName);
        holder.lengthText.setText(trails.get(position)[Field.Length.ordinal()] + " km");
        holder.regionText.setText(trails.get(position)[Field.Region.ordinal()]);
    }

    @Override
    public int getItemCount() {
        return trails.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // get views from layout file

        // Forward compatibility: we might update the fire icon
        ImageView difficultyImg;
        TextView trailNameText, regionText, lengthText, difficultyText;
        List<String[]> trails;
        public ViewHolder(@NonNull View itemView, List<String[]> trails) {
            super(itemView);

            difficultyImg = itemView.findViewById(R.id.iconTrailItemDifficulty);
            trailNameText = itemView.findViewById(R.id.textTrailItemTrailName);
            regionText = itemView.findViewById(R.id.textTrailItemRegion);
            lengthText = itemView.findViewById(R.id.textTrailItemLength);
            difficultyText = itemView.findViewById(R.id.textTrailItemDifficulty);
            this.trails = trails;

            CardView cardView = itemView.findViewById(R.id.cardTrailItem);
            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Context context = v.getContext();
                    String[] trailData = this.trails.get(position);
                    Intent intent = new Intent(context, PageTrailDetails.class);
                    intent.putExtra("trailData", trailData);
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Context context = v.getContext();
                String[] trailData = trails.get(position);
                Intent intent = new Intent(context, PageTrailDetails.class);
                intent.putExtra("trailData", trailData);
                context.startActivity(intent);
            }
        }
    }
}

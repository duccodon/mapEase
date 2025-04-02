package com.example.mapease.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapease.R;
import com.example.mapease.Remote.Step;

import java.util.ArrayList;
import java.util.List;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {
    private List<Step> stepList;

    public StepAdapter(List<Step> stepList) {
        this.stepList = (stepList != null) ? stepList : new ArrayList<>();
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.step_item, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        Step step = stepList.get(position);
        holder.tvInstruction.setText(step.getInstructions());
        holder.tvDistance.setText(String.valueOf(step.getDistance()) + " m");
        // Set maneuver icon based on type
        holder.imgManeuver.setImageResource(step.getManeuverIconResId());
    }

    @Override
    public int getItemCount() {
        return stepList.size();
    }

    public static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView tvInstruction, tvDistance;
        ImageView imgManeuver;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInstruction = itemView.findViewById(R.id.tv_instruction);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            imgManeuver = itemView.findViewById(R.id.img_maneuver);
        }
    }
    // Method to update the step list dynamically
}

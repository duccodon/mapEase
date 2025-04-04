package com.example.mapease;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapease.Remote.Step;
import com.example.mapease.adapter.StepAdapter;

import java.util.ArrayList;
import java.util.List;

public class RouteFragment extends Fragment {
    private static final String ARG_DURATION = "duration";
    private static final String ARG_DISTANCE = "distance";
    private static final String ARG_STEPS = "steps";

    private String duration;
    private String distance;
    private List<Step> stepList;

    public RouteFragment() {
        // Required empty public constructor
    }

    public static RouteFragment newInstance(String duration, String distance, List<Step> steps) {
        RouteFragment fragment = new RouteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DURATION, duration);
        args.putString(ARG_DISTANCE, distance);
        args.putParcelableArrayList(ARG_STEPS, new ArrayList<>(steps));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            duration = getArguments().getString(ARG_DURATION);
            distance = getArguments().getString(ARG_DISTANCE);
            stepList = getArguments().getParcelableArrayList(ARG_STEPS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route, container, false);


        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_steps);
        TextView tvDurationDistance = view.findViewById(R.id.tv_duration_distance);

        tvDurationDistance.setText(duration + " (" + distance + ")");

        StepAdapter stepAdapter = new StepAdapter(stepList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(stepAdapter);

        return view;
    }
}

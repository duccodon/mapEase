package com.example.mapease.Remote;

import com.example.mapease.R;

public class Step {
    private int maneuverIconResId;
    private String instructions;
    private int distance;

    public Step(String maneuver, String instructions, int distance) {
        this.instructions = instructions;
        this.distance = distance;
        this.maneuverIconResId = getManeuverIconResId(maneuver);
    }

    // Function to handle switching based on the maneuver
    private int getManeuverIconResId(String maneuver) {
        switch (maneuver) {
            case "DEPART":
                return R.drawable.icon_start;

            case "TURN_RIGHT":
                return R.drawable.icon_turn_right;
            case "TURN_SLIGHT_RIGHT":
                return R.drawable.icon_turn_slight_right;
            case "TURN_SHARP_RIGHT":
                return R.drawable.icon_turn_sharp_right;
            case "UTURN_RIGHT":
                return R.drawable.icon_uturn_right;

            case "TURN_LEFT":
                return R.drawable.icon_turn_left;
            case "TURN_SLIGHT_LEFT":
                return R.drawable.icon_turn_slight_left;
            case "TURN_SHARP_LEFT":
                return R.drawable.icon_turn_sharp_left;
            case "UTURN_LEFT":
                return R.drawable.icon_uturn_left;
            case "STRAIGHT":
                return R.drawable.icon_straight;

            case "RAMP_LEFT":
                return R.drawable.icon_ramp_left;
            case "RAMP_RIGHT":
                return R.drawable.icon_ramp_right;
            case "MERGE":
                return R.drawable.icon_merge;

            case "ROUNDABOUT_LEFT":
                return R.drawable.icon_roundabout_left;
            case "ROUNDABOUT_RIGHT":
                return R.drawable.icon_roundabout_right;

            default:
                return R.drawable.icon_default_direction;  // Default icon if no match
        }
    }

    public int getManeuverIconResId() {
        return maneuverIconResId;
    }

    public String getInstructions() {
        return instructions;
    }

    public int getDistance() {
        return distance;
    }
    @Override
    public String toString() {
        return "Step{" +
                "instructions='" + instructions + '\'' +
                ", distance='" + distance + '\'' +
                '}';
    }
}


package com.airesapps.util;

public class Threshold {
    public static final double DEFAULT_THRESHOLD = 0.65;
    public static final double LOW_THRESHOLD = 0.50;
    public static final double HIGH_THRESHOLD = 0.75;

    public static double currentThreshold = DEFAULT_THRESHOLD;

    public static double getCurrentThreshold() {
        return currentThreshold;
    }

    public static String getThresholdString() {
        return (int) (currentThreshold * 100) + "%";
    }
}

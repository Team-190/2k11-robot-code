package edu.wpi.team190.util;

/**
 *
 * @author Paul
 */
public class Util {
    public static double constrainToRange(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }
}

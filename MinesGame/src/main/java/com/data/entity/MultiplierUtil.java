package com.data.entity;

public class MultiplierUtil {

    // Example multiplier table
    private static final double[][] MULTIPLIERS = {
        {1.01, 1.08, 1.12, 1.18, 1.24, 1.31, 1.37, 1.46, 1.55, 1.65, 1.77, 1.9, 2.06, 2.25, 2.47, 2.75, 3.09, 3.54, 4.12, 4.95, 6.19, 8.25, 12.37, 24.75}, // Row for 1 mine
        {1.08, 1.17, 1.29, 1.41, 1.56, 1.74, 1.93, 2.18, 2.47, 2.83, 3.26, 3.81, 4.5, 5.46, 6.68, 8.25, 10.14, 14.14, 19.8, 29.7, 49.5, 99.0, 297.0},        // Row for 2 mines
        {24.75}                                                                                                            // Row for 3 mines
    };

    public static double getMultiplier(int mines, int diamonds) {
        // Validate input: mines must be at least 1
        if (mines < 1) {
            throw new IllegalArgumentException("Number of mines must be at least 1.");
        }

        // Cap the number of mines to the maximum defined rows
        int maxMines = MULTIPLIERS.length;
        if (mines > maxMines) {
            mines = maxMines; // Cap mines to the highest available row
        }

        // Validate input: diamonds must be at least 1
        if (diamonds < 1) {
            throw new IllegalArgumentException("Number of revealed diamonds must be at least 1.");
        }

        // Get the maximum diamonds allowed for the given mine count
        int maxRevealed = MULTIPLIERS[mines - 1].length;
        if (diamonds > maxRevealed) {
            diamonds = maxRevealed; // Cap diamonds to the maximum available value
        }

        // Return the multiplier value from the table
        return MULTIPLIERS[mines - 1][diamonds - 1];
    }
}

package com.zez.app.games.snakesladders;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure coordinate calculation utility for 10x10 serpentine Snakes & Ladders numbering.
 * Square 1 sits at bottom-left; Square 100 sits at top-left.
 */
public final class SnakesLaddersCoordinates {

    private SnakesLaddersCoordinates() {}

    public static final class GridCoord {
        public final int square;
        public final int rowFromTop;
        public final int rowFromBottom;
        public final int col;

        public GridCoord(int square, int rowFromTop, int rowFromBottom, int col) {
            this.square = square;
            this.rowFromTop = rowFromTop;
            this.rowFromBottom = rowFromBottom;
            this.col = col;
        }
    }

    public static GridCoord squareToGrid(int square) {
        return squareToGrid(square, 10, 10);
    }

    public static GridCoord squareToGrid(int square, int rows, int columns) {
        int boardSize = rows * columns;
        if (square < 1 || square > boardSize) {
            throw new IllegalArgumentException("Square out of range 1.." + boardSize + ": " + square);
        }
        int zeroBased = square - 1;
        int rowFromBottom = zeroBased / columns;
        int offset = zeroBased % columns;
        int col = (rowFromBottom % 2 == 0) ? offset : (columns - 1 - offset);
        int rowFromTop = rows - 1 - rowFromBottom;
        return new GridCoord(square, rowFromTop, rowFromBottom, col);
    }

    public static int gridToSquare(int rowFromTop, int col) {
        return gridToSquare(rowFromTop, col, 10, 10);
    }

    public static int gridToSquare(int rowFromTop, int col, int rows, int columns) {
        if (rowFromTop < 0 || rowFromTop >= rows || col < 0 || col >= columns) {
            throw new IllegalArgumentException("Grid coords out of range");
        }
        int rowFromBottom = rows - 1 - rowFromTop;
        int offset = (rowFromBottom % 2 == 0) ? col : (columns - 1 - col);
        return rowFromBottom * columns + offset + 1;
    }

    public static float normalizedCenterX(int square, int rows, int columns) {
        if (square <= 0) {
            return 0.05f;
        }
        GridCoord c = squareToGrid(square, rows, columns);
        return (c.col + 0.5f) / columns;
    }

    public static float normalizedCenterY(int square, int rows, int columns) {
        if (square <= 0) {
            return 0.96f;
        }
        GridCoord c = squareToGrid(square, rows, columns);
        return (c.rowFromTop + 0.5f) / rows;
    }

    /**
     * Builds exact intermediate numbered steps between previousPosition and normalDestination.
     */
    public static List<Integer> buildStepPath(int fromSquare, int toSquare) {
        List<Integer> steps = new ArrayList<>();
        if (toSquare <= fromSquare) {
            return steps;
        }
        for (int s = fromSquare + 1; s <= toSquare; s++) {
            steps.add(s);
        }
        return steps;
    }
}

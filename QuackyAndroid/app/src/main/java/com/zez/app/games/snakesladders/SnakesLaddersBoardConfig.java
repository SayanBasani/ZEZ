package com.zez.app.games.snakesladders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Isolated board configuration supporting future swappable board definitions.
 */
public class SnakesLaddersBoardConfig {

    public static final class SnakeDef {
        public final int head;
        public final int tail;

        public SnakeDef(int head, int tail) {
            this.head = head;
            this.tail = tail;
        }
    }

    public static final class LadderDef {
        public final int start;
        public final int end;

        public LadderDef(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public final String version;
    public final int boardSize;
    public final int rows;
    public final int columns;
    public final List<SnakeDef> snakes;
    public final List<LadderDef> ladders;

    public SnakesLaddersBoardConfig(String version, int boardSize, int rows, int columns,
                                    List<SnakeDef> snakes, List<LadderDef> ladders) {
        this.version = version;
        this.boardSize = boardSize;
        this.rows = rows;
        this.columns = columns;
        this.snakes = Collections.unmodifiableList(new ArrayList<>(snakes));
        this.ladders = Collections.unmodifiableList(new ArrayList<>(ladders));
    }

    public static SnakesLaddersBoardConfig defaultConfig() {
        List<SnakeDef> snakes = new ArrayList<>();
        snakes.add(new SnakeDef(22, 2));
        snakes.add(new SnakeDef(34, 7));
        snakes.add(new SnakeDef(59, 40));
        snakes.add(new SnakeDef(75, 51));
        snakes.add(new SnakeDef(88, 71));
        snakes.add(new SnakeDef(98, 24));

        List<LadderDef> ladders = new ArrayList<>();
        ladders.add(new LadderDef(5, 26));
        ladders.add(new LadderDef(11, 49));
        ladders.add(new LadderDef(56, 64));
        ladders.add(new LadderDef(60, 79));
        ladders.add(new LadderDef(74, 93));
        ladders.add(new LadderDef(80, 82));

        return new SnakesLaddersBoardConfig("sl-board-v1", 100, 10, 10, snakes, ladders);
    }

    public boolean isSnakeHead(int square) {
        for (SnakeDef s : snakes) {
            if (s.head == square) return true;
        }
        return false;
    }

    public boolean isLadderStart(int square) {
        for (LadderDef l : ladders) {
            if (l.start == square) return true;
        }
        return false;
    }
}

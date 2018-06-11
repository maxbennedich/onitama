package onitama.model;

import onitama.ai.Searcher;

public class Move {
    public final Card card;
    public final int px, py, nx, ny;

    public final int score;
    public final int scoreSearchDepth;

    public final String stats;

    public Move(Card card, int px, int py, int nx, int ny) {
        this(card, px, py, nx, ny, Searcher.NO_SCORE, 0, null);
    }

    public Move(Card card, int px, int py, int nx, int ny, int score, int scoreSearchDepth, String stats) {
        this.card = card;
        this.px = px;
        this.py = py;
        this.nx = nx;
        this.ny = ny;
        this.score = score;
        this.scoreSearchDepth = scoreSearchDepth;
        this.stats = stats;
    }

    @Override public String toString() {
        return String.format("%s %c%c-%c%c", card.name, 'a'+px, '5'-py, 'a'+nx, '5'-ny);
    }

    public String toFixedWidthString(boolean capture) {
        return String.format("%s %c%c%c%c%c", card.getFixedWidthName(), 'a'+px, '5'-py, capture ? 'x' : '-', 'a'+nx, '5'-ny);
    }

    public int getUniqueId() {
        return card.id + (px << 4) + (py << 7) + (nx << 10) + (ny << 13);
    }

    @Override public int hashCode() {
        return getUniqueId();
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        return getUniqueId() == ((Move)obj).getUniqueId();
    }
}

package onitama.model;

import onitama.ai.Searcher;

public class Move {
    public final Card card;
    public final int px, py, nx, ny;

    public final int score;
    public final int scoreSearchDepth;

    public final long nodesEvaluated;
    public final String stats;

    public Move(Card card, int px, int py, int nx, int ny) {
        this(card, px, py, nx, ny, Searcher.NO_SCORE, 0, 0, null);
    }

    public Move(Card card, int px, int py, int nx, int ny, int score, int scoreSearchDepth, long nodesEvaluated, String stats) {
        this.card = card;
        this.px = px;
        this.py = py;
        this.nx = nx;
        this.ny = ny;
        this.score = score;
        this.scoreSearchDepth = scoreSearchDepth;
        this.nodesEvaluated = nodesEvaluated;
        this.stats = stats;
    }

    @Override public String toString() {
        return String.format("%s %s-%s", card.name, GameDefinition.getPosition(px, py), GameDefinition.getPosition(nx, ny));
    }

    public String toFixedWidthString(boolean capture) {
        return String.format("%s %s%c%s", card.getFixedWidthName(), GameDefinition.getPosition(px, py), capture ? 'x' : '-', GameDefinition.getPosition(nx, ny));
    }

    public int getUniqueId() {
        return px + (py << 3) + (nx << 6) + (ny << 9) + (card.id << 12);
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

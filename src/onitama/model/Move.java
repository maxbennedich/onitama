package onitama.model;

public class Move {
    public final Card card;
    final int px, py, nx, ny;

    public Move(Card card, int px, int py, int nx, int ny) {
        this.card = card;
        this.px = px;
        this.py = py;
        this.nx = nx;
        this.ny = ny;
    }

    @Override public String toString() {
        return String.format("%s %c%c-%c%c", card.name, 'a'+px, '5'-py, 'a'+nx, '5'-ny);
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

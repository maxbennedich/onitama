package onitama.model;

public class Move {
    public Card card;
    public int px, py, nx, ny;

    public Move(Card card, int px, int py, int nx, int ny) {
        this.card = card;
        this.px = px;
        this.py = py;
        this.nx = nx;
        this.ny = ny;
    }
}

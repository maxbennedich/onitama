package onitama.model;

public class Pair<P, Q> {
    public P p;
    public Q q;

    public Pair(P p, Q q) {
        this.p = p;
        this.q = q;
    }

    @Override public String toString() {
        return "Pair [p=" + p + ", q=" + q + "]";
    }
}
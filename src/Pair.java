public class Pair {
    public int x;
    public int y;

    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Pair[] getNeighbors() {
        return new Pair[] {
            new Pair(x - 1, y),
            new Pair(x, y - 1),
            new Pair(x + 1, y),
            new Pair(x, y + 1),
        };
    }
}

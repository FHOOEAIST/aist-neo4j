package science.aist.neo4j.it.nodes;

/**
 * @author Andreas Pointner
 * @since 1.0
 */
public class Point {
    private int x;
    private int y;

    public Point() {

    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * gets value of field {@link Point#x}
     *
     * @return value of field x
     * @see Point#x
     */
    public int getX() {
        return x;
    }

    /**
     * sets value of field {@link Point#x}
     *
     * @param x value of field x
     * @see Point#x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * gets value of field {@link Point#y}
     *
     * @return value of field y
     * @see Point#y
     */
    public int getY() {
        return y;
    }

    /**
     * sets value of field {@link Point#y}
     *
     * @param y value of field y
     * @see Point#y
     */
    public void setY(int y) {
        this.y = y;
    }
}

package science.aist.neo4j.it.nodes;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * <p>Test class</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
@NodeEntity
public class LineNode {
    @Id
    private Long id;

    private Point startPoint;

    private Point endPoint;

    public LineNode() {
    }

    public LineNode(Point startPoint, Point endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    /**
     * gets value of field {@link LineNode#id}
     *
     * @return value of field id
     * @see LineNode#id
     */
    public Long getId() {
        return id;
    }

    /**
     * gets value of field {@link LineNode#startPoint}
     *
     * @return value of field startPoint
     * @see LineNode#startPoint
     */
    public Point getStartPoint() {
        return startPoint;
    }

    /**
     * sets value of field {@link LineNode#startPoint}
     *
     * @param startPoint value of field startPoint
     * @see LineNode#startPoint
     */
    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    /**
     * gets value of field {@link LineNode#endPoint}
     *
     * @return value of field endPoint
     * @see LineNode#endPoint
     */
    public Point getEndPoint() {
        return endPoint;
    }

    /**
     * sets value of field {@link LineNode#endPoint}
     *
     * @param endPoint value of field endPoint
     * @see LineNode#endPoint
     */
    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }
}

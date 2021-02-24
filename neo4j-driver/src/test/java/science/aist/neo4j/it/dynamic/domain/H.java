package science.aist.neo4j.it.dynamic.domain;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * <p>Dummy testclass</p>
 *
 * @author Christoph Praschl
 * @since 1.0
 */
@NodeEntity
public class H {
    @Id
    private Long id;

    @Relationship
    private G g;

    /**
     * gets value of field {@link H#id}
     *
     * @return value of field id
     * @see H#id
     */
    public Long getId() {
        return id;
    }

    /**
     * sets value of field {@link H#id}
     *
     * @param id value of field id
     * @see H#id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * gets value of field {@link H#g}
     *
     * @return value of field g
     * @see H#g
     */
    public G getG() {
        return g;
    }

    /**
     * sets value of field {@link H#g}
     *
     * @param g value of field g
     * @see H#g
     */
    public void setG(G g) {
        this.g = g;
    }
}

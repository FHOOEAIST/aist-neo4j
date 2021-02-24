package science.aist.neo4j.it.dynamic.domain;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

/**
 * <p>Dummy testclass</p>
 *
 * @author Christoph Praschl
 * @since 1.0
 */
@NodeEntity
public class F {
    @Id
    protected Long id;

    @Relationship
    private List<A> as;

    /**
     * gets value of field {@link F#id}
     *
     * @return value of field id
     * @see F#id
     */
    public Long getId() {
        return id;
    }

    /**
     * sets value of field {@link F#id}
     *
     * @param id value of field id
     * @see F#id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * gets value of field {@link F#as}
     *
     * @return value of field as
     * @see F#as
     */
    public List<A> getAs() {
        return as;
    }

    /**
     * sets value of field {@link F#as}
     *
     * @param as value of field as
     * @see F#as
     */
    public void setAs(List<A> as) {
        this.as = as;
    }
}

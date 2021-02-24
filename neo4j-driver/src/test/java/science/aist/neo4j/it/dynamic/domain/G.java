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
public class G {
    @Id
    private Long id;

    @Relationship
    private List<H> hs;

    /**
     * gets value of field {@link G#id}
     *
     * @return value of field id
     * @see G#id
     */
    public Long getId() {
        return id;
    }

    /**
     * sets value of field {@link G#id}
     *
     * @param id value of field id
     * @see G#id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * gets value of field {@link G#hs}
     *
     * @return value of field hs
     * @see G#hs
     */
    public List<H> getHs() {
        return hs;
    }

    /**
     * sets value of field {@link G#hs}
     *
     * @param hs value of field hs
     * @see G#hs
     */
    public void setHs(List<H> hs) {
        this.hs = hs;
    }
}

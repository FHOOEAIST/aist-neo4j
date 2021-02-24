package science.aist.neo4j.it.deep;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Oliver Krauss
 * @since 1.0
 */
@NodeEntity
public class Bottom {

    private Long id;

    @Relationship(type = "MIDDLE_UP")
    private Middle middles;

    private String value;

    public Bottom() {
    }

    public Bottom(String value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Middle getMiddles() {
        return middles;
    }

    public void setMiddles(Middle middles) {
        this.middles = middles;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

package science.aist.neo4j.it.deep;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

/**
 * @author Oliver Krauss
 * @since 1.0
 */
@NodeEntity
public class Top {

    private Long id;

    @Relationship(type = "MIDDLE")
    private List<Middle> middles;

    private String value;

    public Top() {
    }

    public Top(String value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Middle> getMiddles() {
        return middles;
    }

    public void setMiddles(List<Middle> middles) {
        this.middles = middles;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

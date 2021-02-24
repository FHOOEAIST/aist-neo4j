package science.aist.neo4j.it.deep;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

/**
 * @author Oliver Krauss
 * @since 1.0
 */
@NodeEntity
public class Middle {

    private Long id;

    @Relationship(type = "BOTTOM")
    private List<Bottom> bottoms;

    @Relationship(type = "TOP")
    private Top top;

    private String value;

    public Middle() {
    }

    public Middle(String value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Bottom> getBottoms() {
        return bottoms;
    }

    public void setBottoms(List<Bottom> bottoms) {
        this.bottoms = bottoms;
    }

    public Top getTop() {
        return top;
    }

    public void setTop(Top top) {
        this.top = top;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

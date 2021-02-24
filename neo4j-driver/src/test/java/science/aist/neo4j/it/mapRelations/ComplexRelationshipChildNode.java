package science.aist.neo4j.it.mapRelations;

import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Oliver Krauss
 * @since 1.0
 */
@NodeEntity
public class ComplexRelationshipChildNode {


    private Long id;

    private String value;

    public ComplexRelationshipChildNode() {
    }

    public ComplexRelationshipChildNode(String value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}



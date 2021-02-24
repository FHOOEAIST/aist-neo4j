package science.aist.neo4j.it.mapRelations;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Krauss
 * @since 1.0
 */
@NodeEntity
public class ComplexRelationshipParentNode {

    private Long id;

    @Relationship(type = "COMPLEX")
    private Map<String, ComplexRelationshipChildNode> complexRelationship;

    @Relationship(type = "COMPLEX_DOS")
    private Map<String, ComplexRelationshipChildNode> otherComplexRelationship;

    @Relationship(type = "INVERTED_COMPLEX")
    private Map<ComplexRelationshipChildNode, Double> invertedComplexRelationship;

    @Relationship(type = "SUPER_COMPLEX")
    private Map<Class, ComplexRelationshipChildNode> superComplexRelationship;

    @Relationship(type = "ARRAY_COMPLEX")
    private ComplexRelationshipChildNode[] complexArrayRelationship;

    @Relationship(type = "WRITE_PAIRINGS")
    private List<ComplexRelationshipParentNode> cyclicRef = new LinkedList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, ComplexRelationshipChildNode> getComplexRelationship() {
        return complexRelationship;
    }

    public void setComplexRelationship(Map<String, ComplexRelationshipChildNode> complexRelationship) {
        this.complexRelationship = complexRelationship;
    }

    public Map<Class, ComplexRelationshipChildNode> getSuperComplexRelationship() {
        return superComplexRelationship;
    }

    public void setSuperComplexRelationship(Map<Class, ComplexRelationshipChildNode> superComplexRelationship) {
        this.superComplexRelationship = superComplexRelationship;
    }

    public Map<String, ComplexRelationshipChildNode> getOtherComplexRelationship() {
        return otherComplexRelationship;
    }

    public void setOtherComplexRelationship(Map<String, ComplexRelationshipChildNode> otherComplexRelationship) {
        this.otherComplexRelationship = otherComplexRelationship;
    }

    public ComplexRelationshipChildNode[] getComplexArrayRelationship() {
        return complexArrayRelationship;
    }

    public void setComplexArrayRelationship(ComplexRelationshipChildNode[] complexArrayRelationship) {
        this.complexArrayRelationship = complexArrayRelationship;
    }

    public List<ComplexRelationshipParentNode> getCyclicRef() {
        return cyclicRef;
    }

    public void setCyclicRef(List<ComplexRelationshipParentNode> cyclicRef) {
        this.cyclicRef = cyclicRef;
    }

    public Map<ComplexRelationshipChildNode, Double> getInvertedComplexRelationship() {
        return invertedComplexRelationship;
    }

    public void setInvertedComplexRelationship(Map<ComplexRelationshipChildNode, Double> invertedComplexRelationship) {
        this.invertedComplexRelationship = invertedComplexRelationship;
    }
}

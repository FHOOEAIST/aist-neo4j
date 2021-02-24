package science.aist.neo4j.it.nodes;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Test class to test relationship repositories
 * @since 1.0
 */
@RelationshipEntity(type = "RELATED")
public class BrokenRelationship {

    @Id
    private Long id;

    private Long count;

    @EndNode
    BnalyticsNode solution;

    public BrokenRelationship() {
    }

    public BrokenRelationship(Long count, BnalyticsNode solution) {
        this.count = count;
        this.solution = solution;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public BnalyticsNode getSolution() {
        return solution;
    }

    public void setSolution(BnalyticsNode solution) {
        this.solution = solution;
    }
}

package science.aist.neo4j.it.nodes;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * Test class to test relationship repositories
 * @author Andreas Pointner
 * @since 1.0
 */
@RelationshipEntity(type = "RELATED")
public class RelatedRelationship {

    @Id
    private Long id;

    private Long count;

    @StartNode
    BnalyticsNode step;

    @EndNode
    BnalyticsNode solution;

    public RelatedRelationship() {
    }

    public RelatedRelationship(Long count, BnalyticsNode step, BnalyticsNode solution) {
        this.count = count;
        this.step = step;
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

    public BnalyticsNode getStep() {
        return step;
    }

    public void setStep(BnalyticsNode step) {
        this.step = step;
    }

    public BnalyticsNode getSolution() {
        return solution;
    }

    public void setSolution(BnalyticsNode solution) {
        this.solution = solution;
    }
}

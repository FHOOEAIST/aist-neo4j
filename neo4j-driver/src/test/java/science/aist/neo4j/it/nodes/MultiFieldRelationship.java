package science.aist.neo4j.it.nodes;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * Test class to test relationship repositories
 *
 * @author Oliver Krauss
 * @since 1.0
 */
@RelationshipEntity(type = "MULTIS")
public class MultiFieldRelationship {

    @Id
    private Long id;

    private Long count;

    @StartNode
    TestNode start;

    @EndNode
    BnalyticsNode end;

    public MultiFieldRelationship() {
    }

    public MultiFieldRelationship(Long count, TestNode start, BnalyticsNode end) {
        this.count = count;
        this.start = start;
        this.end = end;
    }

    public Long getId() {
        return id;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public TestNode getStart() {
        return start;
    }

    public void setStart(TestNode start) {
        this.start = start;
    }

    public BnalyticsNode getEnd() {
        return end;
    }

    public void setEnd(BnalyticsNode end) {
        this.end = end;
    }
}

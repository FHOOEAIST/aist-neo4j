package science.aist.neo4j.it;

import science.aist.neo4j.it.nodes.BnalyticsNode;
import science.aist.neo4j.it.nodes.FieldRelationship;
import science.aist.neo4j.it.nodes.MultiFieldRelationship;
import science.aist.neo4j.it.nodes.TestNode;
import science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andreas Pointner
 * @since 1.0
 */
public class RelationshipAsFieldTest extends AbstractDbTest {

    @Autowired
    ReflectiveNeo4JNodeRepositoryImpl<BnalyticsNode> bnalyticsRepository;

    @Autowired
    ReflectiveNeo4JNodeRepositoryImpl<TestNode> testNodeRepository;

    @Test
    public void testRelationshipAsField() {
        // given
        TestNode source = new TestNode();
        BnalyticsNode target = new BnalyticsNode();
        FieldRelationship relationship = new FieldRelationship(3, source, target);
        source.setTesting(relationship);

        // when
        testNodeRepository.save(source);

        // then
        Assert.assertNotNull(source.getId());
        Assert.assertNotNull(target.getId());
        Assert.assertNotNull(relationship.getId());
    }

    @Test(dependsOnMethods = "testRelationshipAsField")
    public void testReadRelationshipAsField() {
        // given
        TestNode source = new TestNode();
        BnalyticsNode target = new BnalyticsNode();
        FieldRelationship relationship = new FieldRelationship(3, source, target);
        source.setTesting(relationship);
        testNodeRepository.save(source);

        // when
        TestNode loaded = testNodeRepository.findById(source.getId());

        // then
        Assert.assertNotNull(loaded);
        Assert.assertNotNull(loaded.getId());
        Assert.assertNotNull(loaded.getTesting());
        Assert.assertEquals(loaded.getTesting().getCount(), 3);
    }


    @Test
    public void testBulkRelationshipAsField() {
        // given
        TestNode source = createTestMultiNode();

        // when
        testNodeRepository.save(source);

        // then
        Assert.assertNotNull(source.getId());
        Assert.assertNotNull(source.getMultiTesting().get(0).getId());
        Assert.assertNotNull(source.getMultiTesting().get(1).getId());
        Assert.assertNotNull(source.getMultiTesting().get(2).getId());
        Assert.assertNotNull(source.getMultiTesting().get(0).getEnd().getId());
        Assert.assertNotNull(source.getMultiTesting().get(1).getEnd().getId());
        Assert.assertNotNull(source.getMultiTesting().get(2).getEnd().getId());
    }

    private TestNode createTestMultiNode() {
        TestNode source = new TestNode();
        BnalyticsNode target = new BnalyticsNode();
        BnalyticsNode target2 = new BnalyticsNode();
        BnalyticsNode target3 = new BnalyticsNode();
        MultiFieldRelationship multiFieldRelationship = new MultiFieldRelationship(1L, source, target);
        MultiFieldRelationship multiFieldRelationship2 = new MultiFieldRelationship(2L, source, target2);
        MultiFieldRelationship multiFieldRelationship3 = new MultiFieldRelationship(3L, source, target3);
        source.getMultiTesting().add(multiFieldRelationship);
        source.getMultiTesting().add(multiFieldRelationship2);
        source.getMultiTesting().add(multiFieldRelationship3);
        return source;
    }


    @Test
    public void testBulkRelationshipAsFieldPointToSame() {
        // given
        TestNode source = createTestMultiNode();
        source.getMultiTesting().get(1).setEnd(source.getMultiTesting().get(0).getEnd());
        source.getMultiTesting().get(2).setEnd(source.getMultiTesting().get(0).getEnd());

        // when
        testNodeRepository.save(source);

        // then
        Assert.assertNotNull(source.getId());
        Assert.assertNotNull(source.getMultiTesting().get(0).getId());
        Assert.assertNotNull(source.getMultiTesting().get(1).getId());
        Assert.assertNotNull(source.getMultiTesting().get(2).getId());
        Assert.assertNotNull(source.getMultiTesting().get(0).getEnd().getId());
        Assert.assertNotNull(source.getMultiTesting().get(1).getEnd().getId());
        Assert.assertNotNull(source.getMultiTesting().get(2).getEnd().getId());
    }

    @Test
    public void testRelationshipBulkAsField() {
        // given
        TestNode source = new TestNode();
        BnalyticsNode target = new BnalyticsNode();
        FieldRelationship relationship = new FieldRelationship(11, source, target);
        source.setTesting(relationship);
        TestNode source2 = new TestNode();
        BnalyticsNode target2 = new BnalyticsNode();
        FieldRelationship relationship2 = new FieldRelationship(12, source2, target2);
        source2.setTesting(relationship2);
        TestNode source3 = new TestNode();
        BnalyticsNode target3 = new BnalyticsNode();
        FieldRelationship relationship3 = new FieldRelationship(13, source3, target3);
        source3.setTesting(relationship3);
        List<TestNode> testNodes = new ArrayList<>();
        testNodes.add(source);
        testNodes.add(source2);
        testNodes.add(source3);

        // when
        testNodeRepository.saveAll(testNodes);

        // then
        Assert.assertNotNull(source.getId());
        Assert.assertNotNull(target.getId());
        Assert.assertNotNull(relationship.getId());
        Assert.assertNotNull(source2.getId());
        Assert.assertNotNull(target2.getId());
        Assert.assertNotNull(relationship2.getId());
        Assert.assertNotNull(source3.getId());
        Assert.assertNotNull(target3.getId());
        Assert.assertNotNull(relationship3.getId());
    }

    @Test
    public void testBulkRelationshipBulkAsField() {
        // given
        TestNode n1 = createTestMultiNode();
        TestNode n2 = createTestMultiNode();
        TestNode n3 = createTestMultiNode();
        List<TestNode> testNodes = new ArrayList<>();
        testNodes.add(n1);
        testNodes.add(n2);
        testNodes.add(n3);

        // when
        testNodeRepository.saveAll(testNodes);

        // then
        Assert.assertNotNull(testNodes.get(0).getId());
        Assert.assertNotNull(testNodes.get(0).getMultiTesting().get(0).getId());
        Assert.assertNotNull(testNodes.get(0).getMultiTesting().get(1).getId());
        Assert.assertNotNull(testNodes.get(0).getMultiTesting().get(2).getId());
        Assert.assertNotNull(testNodes.get(0).getMultiTesting().get(0).getEnd().getId());
        Assert.assertNotNull(testNodes.get(0).getMultiTesting().get(1).getEnd().getId());
        Assert.assertNotNull(testNodes.get(0).getMultiTesting().get(2).getEnd().getId());
        Assert.assertNotNull(testNodes.get(1).getId());
        Assert.assertNotNull(testNodes.get(1).getMultiTesting().get(0).getId());
        Assert.assertNotNull(testNodes.get(1).getMultiTesting().get(1).getId());
        Assert.assertNotNull(testNodes.get(1).getMultiTesting().get(2).getId());
        Assert.assertNotNull(testNodes.get(1).getMultiTesting().get(0).getEnd().getId());
        Assert.assertNotNull(testNodes.get(1).getMultiTesting().get(1).getEnd().getId());
        Assert.assertNotNull(testNodes.get(1).getMultiTesting().get(2).getEnd().getId());
        Assert.assertNotNull(testNodes.get(2).getId());
        Assert.assertNotNull(testNodes.get(2).getMultiTesting().get(0).getId());
        Assert.assertNotNull(testNodes.get(2).getMultiTesting().get(1).getId());
        Assert.assertNotNull(testNodes.get(2).getMultiTesting().get(2).getId());
        Assert.assertNotNull(testNodes.get(2).getMultiTesting().get(0).getEnd().getId());
        Assert.assertNotNull(testNodes.get(2).getMultiTesting().get(1).getEnd().getId());
        Assert.assertNotNull(testNodes.get(2).getMultiTesting().get(2).getEnd().getId());
    }

}

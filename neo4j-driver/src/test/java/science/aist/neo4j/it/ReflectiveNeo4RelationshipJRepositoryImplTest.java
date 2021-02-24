package science.aist.neo4j.it;

import science.aist.neo4j.Neo4jRepository;
import science.aist.neo4j.it.nodes.BnalyticsNode;
import science.aist.neo4j.it.nodes.BrokenRelationship;
import science.aist.neo4j.it.nodes.RelatedRelationship;
import science.aist.neo4j.reflective.ClassInformation;
import science.aist.neo4j.reflective.ReflectiveNeo4JRelationshipRepositoryImpl;
import science.aist.neo4j.reflective.RelationshipInformation;
import science.aist.neo4j.util.Pair;
import org.neo4j.driver.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andreas Pointner
 * @since 1.0
 */
public class ReflectiveNeo4RelationshipJRepositoryImplTest extends AbstractDbTest {

    @Autowired
    ReflectiveNeo4JRelationshipRepositoryImpl<RelatedRelationship> relationshipRepository;

    @Test
    public void createClassInformation() {
        // given

        // when
        ClassInformation info = ClassInformation.constructClassInformation(RelatedRelationship.class);

        // then
        Assert.assertEquals(info.getClazz(), RelatedRelationship.class);
        Assert.assertEquals(info.getType(), ClassInformation.Neo4JType.RELATIONSHIP);
        Assert.assertEquals(info.getName(), "RELATED");
        Assert.assertNotNull(info.getId());
        Assert.assertEquals(info.getRelationships().size(), 2);
        Assert.assertFalse(info.getRelationships().get("source").isBulk());
        Assert.assertEquals(info.getRelationships().get("source").getDirection(), RelationshipInformation.Direction.INCOMING);
        Assert.assertNotNull(info.getRelationships().get("source").getTargetClassInformation());
        Assert.assertEquals(info.getRelationships().get("source").getType(), "source");
        Assert.assertEquals(info.getRelationships().get("source").getTargetClassInformation().getClazz(), BnalyticsNode.class);
        Assert.assertFalse(info.getRelationships().get("target").isBulk());
        Assert.assertEquals(info.getRelationships().get("target").getDirection(), RelationshipInformation.Direction.OUTGOING);
        Assert.assertNotNull(info.getRelationships().get("target").getTargetClassInformation());
        Assert.assertEquals(info.getRelationships().get("target").getType(), "target");
        Assert.assertEquals(info.getRelationships().get("target").getTargetClassInformation().getClazz(), BnalyticsNode.class);
        Assert.assertEquals(info.getFields().size(), 1);
        Assert.assertEquals(info.getFields().get("count").getName(), "count");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void createClassInformationFail() {
        // given

        // when
        ClassInformation info = ClassInformation.constructClassInformation(BrokenRelationship.class);

        // then we should fail before here
        Assert.fail();
    }

    @Test
    public void testRepositoryInitialization() {
        // given

        // when
        Neo4jRepository<RelatedRelationship, Long> repository = new ReflectiveNeo4JRelationshipRepositoryImpl<>(null, RelatedRelationship.class);

        // then
        // nothing. We just don't wan't this to fail
    }

    @Test
    public void testSave() {
        // given
        BnalyticsNode bStart = new BnalyticsNode();
        bStart.setTitle("START");
        BnalyticsNode bEnd = new BnalyticsNode();
        bEnd.setTitle("END");
        RelatedRelationship r = new RelatedRelationship(123L, bStart, bEnd);

        // when
        r = relationshipRepository.save(r);

        // then
        Assert.assertNotNull(r.getId());
    }

    @Test(dependsOnMethods = "testSave")
    public void findById() {
        // given
        BnalyticsNode bStart = new BnalyticsNode();
        bStart.setTitle("START");
        BnalyticsNode bEnd = new BnalyticsNode();
        bEnd.setTitle("END");
        RelatedRelationship r = new RelatedRelationship(123L, bStart, bEnd);
        r = relationshipRepository.save(r);

        // when
        RelatedRelationship x = relationshipRepository.findById(r.getId());

        // then
        Assert.assertNotNull(x);
        Assert.assertNotNull(x.getStep());
        Assert.assertNotNull(x.getSolution());
    }


    @Test(dependsOnMethods = "testSave")
    public void testFindAll() {
        // given

        // when
        Iterable<RelatedRelationship> all = relationshipRepository.findAll();


        // then
        Assert.assertNotNull(all);
        Iterator<RelatedRelationship> iterator = all.iterator();
        Assert.assertTrue(iterator.hasNext());
    }

    @Test(dependsOnMethods = {"testFindAll"})
    public void testSaveAll() {
        // given
        BnalyticsNode bStart = new BnalyticsNode();
        bStart.setTitle("S");
        BnalyticsNode bEnd = new BnalyticsNode();
        bEnd.setTitle("E");
        RelatedRelationship r = new RelatedRelationship(123L, bStart, bEnd);
        RelatedRelationship r2 = new RelatedRelationship(124L, bStart, bEnd);
        RelatedRelationship r3 = new RelatedRelationship(125L, bStart, bEnd);
        List<RelatedRelationship> list = new ArrayList<>();
        list.add(r);
        list.add(r2);
        list.add(r3);

        // when
        Iterable<RelatedRelationship> all = relationshipRepository.saveAll(list);

        // then
        Assert.assertNotNull(all);
        Iterator<RelatedRelationship> iterator = all.iterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertNotNull(iterator.next().getId());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertNotNull(iterator.next().getId());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertNotNull(iterator.next().getId());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test(dependsOnMethods = "testSaveAll")
    public void testFindBy() {
        // given
        BnalyticsNode bStart = new BnalyticsNode();
        bStart.setTitle("START");
        BnalyticsNode bEnd = new BnalyticsNode();
        bEnd.setTitle("END");
        RelatedRelationship r = new RelatedRelationship(321654987L, bStart, bEnd);
        relationshipRepository.save(r);

        List<Pair<String, Object>> list = new ArrayList<>();
        list.add(new Pair<>("count", 321654987L));

        // when
        Iterable<RelatedRelationship> result = relationshipRepository.findAllBy(list);

        // then
        Assert.assertTrue(result.iterator().hasNext());
        Assert.assertEquals(result.iterator().next().getId(), r.getId());
    }

    @Test(dependsOnMethods = "testSaveAll")
    public void testCustomQuery() {
        // given
        BnalyticsNode bStart = new BnalyticsNode();
        bStart.setTitle("START");
        BnalyticsNode bEnd = new BnalyticsNode();
        bEnd.setTitle("END");
        RelatedRelationship r = new RelatedRelationship(984338421L, bStart, bEnd);
        relationshipRepository.save(r);

        // when
        RelatedRelationship result = relationshipRepository.query("MATCH (s)-[r]-(t) WHERE s.title = $start AND t.title = $end AND r.count = $count return r", Values.parameters("start", "START", "end", "END", "count", 984338421L));

        // then
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), r.getId());
    }

    @Test(dependsOnMethods = "testSaveAll")
    public void testCustomBulkQuery() {
        // given
        BnalyticsNode bStart = new BnalyticsNode();
        bStart.setTitle("START");
        BnalyticsNode bEnd = new BnalyticsNode();
        bEnd.setTitle("END");
        RelatedRelationship r = new RelatedRelationship(123456789L, bStart, bEnd);
        relationshipRepository.save(r);

        // when
        Iterable<RelatedRelationship> result = relationshipRepository.queryAll("MATCH (s)-[r]-(t) WHERE s.title = $start AND t.title = $end return r", Values.parameters("start", "START", "end", "END"));

        // then
        Assert.assertNotNull(result);
        Assert.assertTrue(result.iterator().hasNext());
        Assert.assertNotNull(result.iterator().next());
    }


}

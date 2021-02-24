package science.aist.neo4j.it;

import science.aist.neo4j.Neo4jRepository;
import science.aist.neo4j.it.deep.Bottom;
import science.aist.neo4j.it.deep.Middle;
import science.aist.neo4j.it.deep.Top;
import science.aist.neo4j.it.dynamic.domain.A;
import science.aist.neo4j.it.dynamic.domain.B;
import science.aist.neo4j.it.nodes.AnalyticsNode;
import science.aist.neo4j.it.nodes.BnalyticsNode;
import science.aist.neo4j.reflective.ClassInformation;
import science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl;
import science.aist.neo4j.reflective.RelationshipInformation;
import science.aist.neo4j.util.Pair;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Andreas Pointner
 * @since 1.0
 */
public class ReflectiveNeo4NodeJRepositoryImplTest extends AbstractDbTest {

    @Autowired
    ReflectiveNeo4JNodeRepositoryImpl<BnalyticsNode> bnalyticsRepository;

    @Autowired
    ReflectiveNeo4JNodeRepositoryImpl<Top> topRepository;

    @Test
    public void createClassInformation() {
        // given

        // when
        ClassInformation info = ClassInformation.constructClassInformation(BnalyticsNode.class);

        // then
        Assert.assertEquals(info.getClazz(), BnalyticsNode.class);
        Assert.assertEquals(info.getType(), ClassInformation.Neo4JType.NODE);
        Assert.assertEquals(info.getName(), "BGradeAnalytics");
        Assert.assertEquals(info.getLabels(), "BGradeAnalytics:AnalyticsNode");
        Assert.assertNotNull(info.getId());
        Assert.assertEquals(info.getRelationships().size(), 4);
        Assert.assertNotNull(info.getRelationships().get("unnamedRel").getTargetClassInformation());
        Assert.assertFalse(info.getRelationships().get("unnamedRel").isBulk());
        Assert.assertEquals(info.getRelationships().get("unnamedRel").getDirection(), RelationshipInformation.Direction.OUTGOING);
        Assert.assertNotNull(info.getRelationships().get("NAMED_REL").getTargetClassInformation());
        Assert.assertFalse(info.getRelationships().get("NAMED_REL").isBulk());
        Assert.assertEquals(info.getRelationships().get("NAMED_REL").getDirection(), RelationshipInformation.Direction.OUTGOING);
        Assert.assertNotNull(info.getRelationships().get("unnamedMulti").getTargetClassInformation());
        Assert.assertTrue(info.getRelationships().get("unnamedMulti").isBulk());
        Assert.assertEquals(info.getRelationships().get("unnamedMulti").getDirection(), RelationshipInformation.Direction.OUTGOING);
        Assert.assertFalse(info.getRelationships().get("staticRelationship").isBulk());
        Assert.assertEquals(info.getRelationships().get("staticRelationship").getDirection(), RelationshipInformation.Direction.OUTGOING);
        Assert.assertNotNull(info.getRelationships().get("staticRelationship").getTargetClassInformation());
        Assert.assertEquals(info.getRelationships().get("staticRelationship").getType(), "staticRelationship");
        Assert.assertEquals(info.getFields().size(), 5);
        Assert.assertEquals(info.getFields().get("title").getName(), "title");
    }


    @Test(expectedExceptions = RuntimeException.class)
    public void createClassInformationFail() {
        // given

        // when
        ClassInformation info = ClassInformation.constructClassInformation(Object.class);

        // then we should fail before here
        Assert.fail();
    }

    @Test
    public void testRepositoryInitialization() {
        // given

        // when
        Neo4jRepository<BnalyticsNode, Long> repository = new ReflectiveNeo4JNodeRepositoryImpl<>(null, BnalyticsNode.class);

        // then
        // nothing. We just don't wan't this to fail
    }


    @Test
    public void testArray() {
        // given
        BnalyticsNode bnalyticsNode = new BnalyticsNode();
        bnalyticsNode.getTestArray().add("a");
        bnalyticsNode.getTestArray().add("b");
        bnalyticsNode.getTestArray().add("c");

        // when
        bnalyticsNode = bnalyticsRepository.save(bnalyticsNode);

        // then
        BnalyticsNode resultNode = bnalyticsRepository.findById(bnalyticsNode.getId());
        Assert.assertEquals(resultNode.getTestArray().size(), 3);
        Assert.assertTrue(resultNode.getTestArray().contains("a"));
        Assert.assertTrue(resultNode.getTestArray().contains("b"));
        Assert.assertTrue(resultNode.getTestArray().contains("c"));
    }


    @Test
    public void testFindBy() {
        // given
        BnalyticsNode bnalyticsNode = new BnalyticsNode();
        bnalyticsNode.setTitle("Hello");
        bnalyticsNode.setDontIgnoreMe("World");
        bnalyticsNode = bnalyticsRepository.save(bnalyticsNode);
        List<Pair<String, Object>> list = new ArrayList<>();
        list.add(new Pair<>("title", "Hello"));
        list.add(new Pair<>("dontIgnoreMe", "World"));

        // when
        Iterable<BnalyticsNode> result = bnalyticsRepository.findAllBy(list);

        // then
        Assert.assertTrue(result.iterator().hasNext());
        Assert.assertEquals(result.iterator().next().getId(), bnalyticsNode.getId());
    }


    @Test
    public void testFindNotBy() {
        // given
        List<Pair<String, Object>> list = new ArrayList<>();
        list.add(new Pair<>("title", "NEVER"));
        list.add(new Pair<>("dontIgnoreMe", "SETTHIS"));

        // when
        Iterable<BnalyticsNode> result = bnalyticsRepository.findAllBy(list);

        // then
        Assert.assertFalse(result.iterator().hasNext());
    }

    @Test
    public void testFindNotBySingle() {
        // given
        List<Pair<String, Object>> list = new ArrayList<>();
        list.add(new Pair<>("title", "NEVER"));
        list.add(new Pair<>("dontIgnoreMe", "SETTHIS"));

        // when
        BnalyticsNode result = bnalyticsRepository.findBy(list);

        // then
        Assert.assertNull(result);
    }

    @Test
    public void testFindById() {
        // given
        Long id = bnalyticsRepository.save(new BnalyticsNode()).getId();

        // when
        BnalyticsNode node = bnalyticsRepository.findById(id);

        // then
        Assert.assertNotNull(node);
    }

    private long topId;

    @Test
    public void testFindSubtree() {
        // given
        Bottom b1 = new Bottom("X");
        Bottom b2 = new Bottom("Y");
        Bottom b3 = new Bottom("Z");
        Middle m1 = new Middle("A");
        m1.setBottoms(Arrays.asList(b1, b2));
        b1.setMiddles(m1);
        b2.setMiddles(m1);
        Middle m2 = new Middle("B");
        m2.setBottoms(Arrays.asList(b3));
        b3.setMiddles(m2);
        Middle m3 = new Middle("C");
        Top top = new Top("1");
        top.setMiddles(Arrays.asList(m1, m2, m3));
        m1.setTop(top);
        m2.setTop(top);
        m3.setTop(top);
        topId = topRepository.save(top).getId();

        // when
        Top node = topRepository.findSubtree(topId);

        // then
        Assert.assertNotNull(node);
        Assert.assertEquals(node.getValue(), "1");
        Assert.assertEquals(node.getMiddles().size(), 3);
        Middle a = node.getMiddles().stream().filter(x -> x.getValue().equals("A")).findFirst().orElse(null);
        Middle b = node.getMiddles().stream().filter(x -> x.getValue().equals("B")).findFirst().orElse(null);
        Middle c = node.getMiddles().stream().filter(x -> x.getValue().equals("C")).findFirst().orElse(null);
        Assert.assertEquals(a.getValue(), "A");
        Assert.assertEquals(b.getValue(), "B");
        Assert.assertEquals(c.getValue(), "C");
        Assert.assertEquals(a.getTop(), node);
        Assert.assertEquals(b.getTop(), node);
        Assert.assertEquals(c.getTop(), node);
        Assert.assertEquals(a.getBottoms().size(), 2);
        Assert.assertTrue(a.getBottoms().stream().anyMatch(x -> x.getValue().equals("X")));
        Assert.assertTrue(a.getBottoms().stream().anyMatch(x -> x.getValue().equals("Y")));
        Assert.assertEquals(a.getBottoms().get(0).getMiddles(), a);
        Assert.assertEquals(a.getBottoms().get(1).getMiddles(), a);
        Assert.assertEquals(b.getBottoms().size(), 1);
        Assert.assertEquals(b.getBottoms().get(0).getValue(), "Z");
        Assert.assertEquals(b.getBottoms().get(0).getMiddles(), b);
        Assert.assertNull(c.getBottoms());
    }

    @Test(dependsOnMethods = "testFindSubtree")
    public void testFindSubtreeDepth() {
        // given

        // when
        Top node = topRepository.findSubtree(topId, 0);

        // then
        Assert.assertNotNull(node);
        Assert.assertNotNull(node);
        Assert.assertEquals(node.getValue(), "1");
        Assert.assertNull(node.getMiddles());
    }

    @Test(dependsOnMethods = "testFindSubtree")
    public void testFindSubtreeRelationships() {
        // given

        // when
        Top node = topRepository.findSubtree(topId, -1, Arrays.asList("MIDDLE", "BOTTOM"));

        // then
        Assert.assertNotNull(node);
        Assert.assertEquals(node.getValue(), "1");
        Assert.assertEquals(node.getMiddles().size(), 3);
        Middle a = node.getMiddles().stream().filter(x -> x.getValue().equals("A")).findFirst().orElse(null);
        Middle b = node.getMiddles().stream().filter(x -> x.getValue().equals("B")).findFirst().orElse(null);
        Middle c = node.getMiddles().stream().filter(x -> x.getValue().equals("C")).findFirst().orElse(null);
        Assert.assertEquals(a.getValue(), "A");
        Assert.assertEquals(b.getValue(), "B");
        Assert.assertEquals(c.getValue(), "C");
        Assert.assertNull(a.getTop());
        Assert.assertNull(b.getTop());
        Assert.assertNull(c.getTop());
        Assert.assertEquals(a.getBottoms().size(), 2);
        Assert.assertTrue(a.getBottoms().stream().anyMatch(x -> x.getValue().equals("X")));
        Assert.assertTrue(a.getBottoms().stream().anyMatch(x -> x.getValue().equals("Y")));
        Assert.assertNull(a.getBottoms().get(0).getMiddles());
        Assert.assertNull(a.getBottoms().get(1).getMiddles());
        Assert.assertEquals(b.getBottoms().size(), 1);
        Assert.assertEquals(b.getBottoms().get(0).getValue(), "Z");
        Assert.assertNull(b.getBottoms().get(0).getMiddles());
        Assert.assertNull(c.getBottoms());
    }

    @Test
    public void testFindNotById() {
        // given
        Long id = Long.MAX_VALUE; // yes this can actually fail, but only when the test db handles 2.147.483.647 nodes

        // when
        BnalyticsNode node = bnalyticsRepository.findById(id);

        // then
        Assert.assertNull(node);
    }

    @Test
    public void testCustomQuery() {
        // given
        BnalyticsNode bnalyticsNode = new BnalyticsNode("Hello World!");
        bnalyticsNode = bnalyticsRepository.save(bnalyticsNode);

        // when
        BnalyticsNode node = bnalyticsRepository.queryTyped("MATCH (n) where n.title contains $content RETURN n", Values.parameters("content", "o W"));

        // then
        Assert.assertNotNull(node);
        Assert.assertEquals(node.getId(), bnalyticsNode.getId());
    }

    @Test
    public void testCustomQueryNothing() {
        // given

        // when
        BnalyticsNode node = bnalyticsRepository.queryTyped("MATCH (n) where n.title = \"NONONONONONO\" RETURN n", null);

        // then
        Assert.assertNull(node);
    }

    @Test(expectedExceptions = ClientException.class)
    public void testCustomQueryFail() {
        // given

        // when
        BnalyticsNode node = bnalyticsRepository.queryTyped("MATCH (n)", null);

        // then we should never get here
        Assert.fail();
    }


    @Test
    public void testCustomBulkQuery() {
        // given
        bnalyticsRepository.deleteAll();
        bnalyticsRepository.save(new BnalyticsNode("DNA"));
        bnalyticsRepository.save(new BnalyticsNode("RNA"));
        bnalyticsRepository.save(new BnalyticsNode("NSA"));
        bnalyticsRepository.save(new BnalyticsNode("NAH"));

        // when
        Iterable<BnalyticsNode> node = bnalyticsRepository.queryAllTyped("MATCH (n) where n.title contains $content RETURN n", Values.parameters("content", "NA"));

        // then
        Assert.assertNotNull(node);
        Iterator<BnalyticsNode> iterator = node.iterator();
        Assert.assertTrue(iterator.hasNext());
        iterator.next();
        Assert.assertTrue(iterator.hasNext());
        iterator.next();
        Assert.assertTrue(iterator.hasNext());
        iterator.next();
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testCustomBulkQueryNothing() {
        // given

        // when
        Iterable<BnalyticsNode> node = bnalyticsRepository.queryAllTyped("MATCH (n) where n.title = \"JSJSJSJSJSJ\" RETURN n", null);

        // then
        Assert.assertNotNull(node);
        Assert.assertFalse(node.iterator().hasNext());
    }

    @Test(expectedExceptions = ClientException.class)
    public void testCustomBulkQueryFail() {
        // given

        // when
        Iterable<BnalyticsNode> node = bnalyticsRepository.queryAllTyped("MATCH (n)", null);

        // then
        Assert.fail(); // we should never get here
    }

    @Test
    public void testNamedRelationship() {
        // given
        BnalyticsNode b = new BnalyticsNode("ConnectMe");
        AnalyticsNode a = new AnalyticsNode("Analytics");
        b.setNamedRel(a);

        // when
        bnalyticsRepository.save(b);

        // then
        Assert.assertNotNull(b.getId());
        Assert.assertNotNull(b.getNamedRel());
        Assert.assertNotNull(b.getNamedRel().getId());
    }

    @Test
    public void testUnnamedRelationship() {
        // given
        BnalyticsNode b = new BnalyticsNode("ConnectMe");
        AnalyticsNode a = new AnalyticsNode("Analytics");
        b.setUnnamedRel(a);

        // when
        bnalyticsRepository.save(b);

        // then
        Assert.assertNotNull(b.getId());
        Assert.assertNotNull(b.getUnnamedRel());
        Assert.assertNotNull(b.getUnnamedRel().getId());
    }

    @Test
    public void testUnnamedBulk() {
        // given
        BnalyticsNode b = new BnalyticsNode("ConnectMe");
        AnalyticsNode a1 = new AnalyticsNode("Analytics1");
        AnalyticsNode a2 = new AnalyticsNode("Analytics2");
        AnalyticsNode a3 = new AnalyticsNode("Analytics3");
        b.setUnnamedMulti(Arrays.asList(a1, a2, a3));

        // when
        bnalyticsRepository.save(b);

        // then
        Assert.assertNotNull(b.getId());
        Assert.assertNotNull(b.getUnnamedMulti());
        Assert.assertNotNull(b.getUnnamedMulti().get(0));
        Assert.assertNotNull(b.getUnnamedMulti().get(0).getId());
        Assert.assertNotNull(b.getUnnamedMulti().get(1));
        Assert.assertNotNull(b.getUnnamedMulti().get(1).getId());
        Assert.assertNotNull(b.getUnnamedMulti().get(2));
        Assert.assertNotNull(b.getUnnamedMulti().get(2).getId());
    }

    @Test
    public void testStaticField() {
        // given
        BnalyticsNode b = new BnalyticsNode("TheStaticOne");
        BnalyticsNode.setStaticField("static");

        // when
        bnalyticsRepository.save(b);
        BnalyticsNode.setStaticField("I changed this");
        BnalyticsNode c = bnalyticsRepository.findById(b.getId());

        // then -> we ignore the static field per default
        Assert.assertEquals(BnalyticsNode.getStaticField(), "I changed this");
    }

    @Test
    public void testStaticIncludedField() {
        // given
        BnalyticsNode b = new BnalyticsNode("TheStaticInclude");
        BnalyticsNode.setStaticIncludedField("static");

        // when
        bnalyticsRepository.save(b);
        BnalyticsNode.setStaticIncludedField("I changed this");
        BnalyticsNode c = bnalyticsRepository.findById(b.getId());

        // then -> we ignore the static field per default
        Assert.assertEquals(BnalyticsNode.getStaticIncludedField(), "static");
    }

    @Test
    public void testStaticRelationship() {
        // given
        BnalyticsNode b = new BnalyticsNode("TheStaticRelationship");
        AnalyticsNode a = new AnalyticsNode("TheStaticRelation");
        AnalyticsNode notSaved = new AnalyticsNode("NotSavedRelationship");
        BnalyticsNode.setStaticRelationship(a);

        // when
        bnalyticsRepository.save(b);
        BnalyticsNode.setStaticRelationship(notSaved);
        BnalyticsNode c = bnalyticsRepository.findById(b.getId());

        // then -> we ignore the static field per default
        Assert.assertEquals(BnalyticsNode.getStaticRelationship().getId(), a.getId());
    }


    @Autowired
    @Qualifier("reflectiveRepoA")
    private ReflectiveNeo4JNodeRepositoryImpl<A> aRepo;

    @Autowired
    @Qualifier("reflectiveRepoB")
    private ReflectiveNeo4JNodeRepositoryImpl<B> bRepo;

    /**
     * Test query method with queryHelp call
     */
    @Test
    public void testQuery() {
        // given
        A a = new A();
        A save = aRepo.save(a);

        String query = "Match(n:A) Where ID(n) = " + save.getId() + " Return ID(n)";

        // when
        Long res = aRepo.query(query, null, Long.class);

        // then
        Assert.assertNotNull(res);
    }

    /**
     * Test query method with execute call
     */
    @Test
    public void testQuery2() {
        // given
        B b = new B();
        B save = bRepo.save(b);

        String query = "Match(n:B) Where ID(n) = " + save.getId() + " Return n";

        // when
        B res = aRepo.query(query, null, B.class);

        // then
        Assert.assertNotNull(res);
    }

    /**
     * Test queryAll method with queryAllHelp call
     */
    @Test
    public void testQueryAll() {
        // given
        A a = new A();
        A save = aRepo.save(a);
        A a2 = new A();
        A save2 = aRepo.save(a2);
        A a3 = new A();
        A save3 = aRepo.save(a3);

        List<Long> ids = Arrays.asList(a.getId(), a2.getId(), a3.getId());

        String query = "Match(n:A) Where ID(n) = " + save.getId() + " OR ID(n) = " + save2.getId() + " OR ID(n) = " + save3.getId() + " Return ID(n)";

        // when
        Stream<Long> res = aRepo.queryAll(query, null, Long.class);

        // then
        List<Long> collect = res.collect(Collectors.toList());
        Assert.assertNotNull(collect);
        Assert.assertEquals(collect.size(), 3);
        collect.forEach(aLong -> Assert.assertTrue(ids.contains(aLong)));
    }

    /**
     * Test queryAll method with executeAll call
     */
    @Test
    public void testQueryAll2() {
        // given
        B b = new B();
        B save = bRepo.save(b);
        B b2 = new B();
        B save2 = bRepo.save(b2);
        B b3 = new B();
        B save3 = bRepo.save(b3);

        List<Long> ids = Arrays.asList(b.getId(), b2.getId(), b3.getId());

        String query = "Match(n:B) Where ID(n) = " + save.getId() + " OR ID(n) = " + save2.getId() + " OR ID(n) = " + save3.getId() + " Return n";

        // when
        Stream<B> res = aRepo.queryAll(query, null, B.class);

        // then
        List<B> collect = res.collect(Collectors.toList());
        Assert.assertNotNull(res);
        Assert.assertNotNull(collect);
        Assert.assertEquals(collect.size(), 3);
        collect.forEach(elem -> Assert.assertTrue(ids.contains(elem.getId())));
    }
}

package science.aist.neo4j.it;

import science.aist.neo4j.it.diffspace.c.NamespaceC;
import science.aist.neo4j.it.namespace.RootNode;
import science.aist.neo4j.it.namespace.a.NamespaceA;
import science.aist.neo4j.it.namespace.b.NamespaceB;
import science.aist.neo4j.it.nodes.AnalyticsNode;
import science.aist.neo4j.reflective.ClassInformation;
import science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl;
import science.aist.neo4j.util.Pair;
import org.neo4j.driver.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Andreas Pointner
 * @since 1.0
 */
public class NamespaceAwareReflectiveNeo4NodeJRepositoryImplTest extends AbstractDbTest {

    @Autowired
    ReflectiveNeo4JNodeRepositoryImpl<RootNode> rootRepository;

    @Autowired
    ReflectiveNeo4JNodeRepositoryImpl<NamespaceA> aRepository;

    @Autowired
    ReflectiveNeo4JNodeRepositoryImpl<NamespaceB> bRepository;

    @Autowired
    ReflectiveNeo4JNodeRepositoryImpl<NamespaceC> cRepository;

    @Test
    public void createClassInformation() {
        // given

        // when
        ClassInformation info = ClassInformation.constructClassInformation(RootNode.class);

        // then
        Assert.assertEquals(info.getNamespace(), "test");
        Assert.assertEquals(info.getNsName(), "test_RootNode");
    }

    @Test
    public void createClassInformationA() {
        // given

        // when
        ClassInformation info = ClassInformation.constructClassInformation(NamespaceA.class);

        // then
        Assert.assertEquals(info.getNamespace(), "science_aist_neo4j_it_namespace_a");
        Assert.assertEquals(info.getNsLabels(), "science_aist_neo4j_it_namespace_a_NamespaceA:test_RootNode");
    }

    @Test
    public void createClassInformationB() {
        // given

        // when
        ClassInformation info = ClassInformation.constructClassInformation(NamespaceB.class);

        // then
        Assert.assertEquals(info.getNamespace(), "science_aist_neo4j_it_namespace_b");
        Assert.assertEquals(info.getNsLabels(), "science_aist_neo4j_it_namespace_b_NamespaceB:test_RootNode");
    }

    @Test
    public void createClassInformationC() {
        // given

        // when
        ClassInformation info = ClassInformation.constructClassInformation(NamespaceC.class);

        // then
        Assert.assertEquals(info.getNamespace(), "science_aist_neo4j_it_diffspace_c");
        Assert.assertEquals(info.getNsLabels(), "science_aist_neo4j_it_diffspace_c_NamespaceC:test_RootNode");
    }

    @Test
    public void testSaveRoot() {
        // given
        RootNode r = new RootNode("asdf");
        r.setAnalytics(new AnalyticsNode("Analytics"));
        r.setTheOtherAnalytics(new AnalyticsNode("OtterlyAnalytical"));

        // when
        r = rootRepository.save(r);

        // then
        Assert.assertNotNull(r.getId());
    }

    /**
     * This test checks update behaviour of existing nodes with extending non-existing namespaces -&gt; when class hierarchy is given
     */
    @Test(dependsOnMethods = "testSaveRoot")
    public void testSaveARootExists() {
        // given
        RootNode r = rootRepository.findBy("rootField", "asdf");
        NamespaceA a = new NamespaceA(r.getRootField(), "aText", "General Confusion");
        a.setId(r.getId());
        a.setAdditiveProperties(new HashMap<>());
        a.getAdditiveProperties().put("test", "toast");

        // when
        a = aRepository.save(a);

        // then
        RootNode check = rootRepository.findById(r.getId());
        Assert.assertEquals(a.getRootField(), check.getRootField());
        Assert.assertEquals(a.getId(), check.getId());
        Assert.assertNull(a.getAnalytics()); // as we didn't load it from the DB!
        Assert.assertNull(a.getTheOtherAnalytics());
    }

    @Test(dependsOnMethods = "testSaveARootExists")
    public void testLoadAWithRelationship() {
        // given

        // when
        NamespaceA aLoad = aRepository.findBy("general", "General Confusion");

        // then
        Assert.assertNotNull(aLoad.getAnalytics());
        Assert.assertNotNull(aLoad.getTheOtherAnalytics());
    }

    /**
     * This test checks update behaviour of existing nodes with extending non-existing namespaces -&gt; when no class hierarchy is given
     */
    @Test(dependsOnMethods = "testSaveARootExists")
    public void testSaveBwhileAalreadyExists() {
        // given
        RootNode r = rootRepository.findBy("rootField", "asdf");
        NamespaceA a = aRepository.findBy("rootField", "asdf");
        Assert.assertEquals(r.getId(), a.getId());
        NamespaceB b = bRepository.findById(r.getId());
        b.setGeneral("Confused General");
        b.setB("BEEEEEEEEEES");


        // when
        b = bRepository.save(b);

        // then
        NamespaceA check = aRepository.findById(r.getId());
        NamespaceB checkB = bRepository.findById(r.getId());
        Assert.assertEquals(b.getId(), check.getId());
        Assert.assertEquals(check.getA(), a.getA());
        Assert.assertEquals(check.getGeneral(), a.getGeneral());
        Assert.assertEquals(checkB.getB(), b.getB());
        Assert.assertEquals(checkB.getGeneral(), b.getGeneral());
        Assert.assertNotEquals(check.getGeneral(), checkB.getGeneral());
    }


    @Test(dependsOnMethods = "testSaveBwhileAalreadyExists")
    public void testLabelsField() {
        // given
        ClassInformation info = ClassInformation.constructClassInformation(RootNode.class);
        RootNode r = rootRepository.findBy("rootField", "asdf");

        // when
        List<String> labels = info.getLabels(r);

        // then
        Assert.assertEquals(labels.size(), 3);
        Assert.assertTrue(labels.contains("test_RootNode"));
        Assert.assertTrue(labels.contains("science_aist_neo4j_it_namespace_a_NamespaceA"));
        Assert.assertTrue(labels.contains("science_aist_neo4j_it_namespace_b_NamespaceB"));
    }

    @Test(dependsOnMethods = "testSaveBwhileAalreadyExists")
    public void testSyncField() {
        // given
        ClassInformation info = ClassInformation.constructClassInformation(RootNode.class);
        RootNode r = rootRepository.findBy("rootField", "asdf");

        // when
        Map<String, Object> labels = info.getAlternativeNamespaceFields(r);

        // then
        Assert.assertEquals(labels.size(), 5);
        Assert.assertTrue(labels.get("science_aist_neo4j_it_namespace_a_additiveProperties") instanceof Map);
    }

    @Test(dependsOnMethods = "testSaveARootExists")
    public void findById() {
        // given
        NamespaceA a = new NamespaceA("idFindTest", "aaa", "Id find");
        long id = aRepository.save(a).getId();

        // when
        NamespaceA find = aRepository.findById(id);

        // then
        Assert.assertNotNull(find);
        Assert.assertEquals(find.getRootField(), a.getRootField());
        Assert.assertEquals(find.getA(), a.getA());
        Assert.assertEquals(find.getGeneral(), a.getGeneral());
    }


    @Test
    public void findByIdHasOnlyParentLabels() {
        // given
        RootNode r = new RootNode("IamROOT");
        rootRepository.save(r);

        // when
        NamespaceA find = aRepository.findById(r.getId());

        // then
        Assert.assertNotNull(find);
        Assert.assertEquals(find.getRootField(), r.getRootField());
    }

    @Test
    public void findByIdNodeHasAdditionalLabels() {
        // given
        NamespaceC c = new NamespaceC("IamCoot", "c", "generali");
        cRepository.save(c);

        // when
        NamespaceA find = aRepository.findById(c.getId());

        // then
        Assert.assertNotNull(find);
        Assert.assertEquals(find.getRootField(), c.getRootField());
    }

    @Test
    public void findByIdNodeHasChildlabels() {
        // given
        NamespaceC c = new NamespaceC("IamCoot", "c", "generali");
        cRepository.save(c);

        // when
        RootNode find = rootRepository.findById(c.getId());

        // then
        Assert.assertNotNull(find);
        Assert.assertEquals(find.getRootField(), c.getRootField());
    }


    @Test(dependsOnMethods = {"testSaveBwhileAalreadyExists", "findById", "findByIdHasOnlyParentLabels", "findByIdNodeHasAdditionalLabels", "findByIdNodeHasChildlabels"})
    public void testFindAll() {
        // given
        // done in the other tests

        // when
        List<RootNode> rootNodes = new ArrayList<>();
        rootRepository.findAll().iterator().forEachRemaining(rootNodes::add);
        List<NamespaceA> aNodes = new ArrayList<>();
        aRepository.findAll().iterator().forEachRemaining(aNodes::add);
        List<NamespaceB> bNodes = new ArrayList<>();
        bRepository.findAll().iterator().forEachRemaining(bNodes::add);
        List<NamespaceC> cNodes = new ArrayList<>();
        cRepository.findAll().iterator().forEachRemaining(cNodes::add);

        // then
        Assert.assertEquals(rootNodes.size(), 5);
        Assert.assertEquals(aNodes.size(), 2);
        Assert.assertEquals(bNodes.size(), 1);
        Assert.assertEquals(cNodes.size(), 2);
    }

    @Test(dependsOnMethods = {"testFindAll"})
    public void testSaveAll() {
        // give
        NamespaceC c1 = new NamespaceC("C1", "c4", "c7");
        NamespaceC c2 = new NamespaceC("C2", "c5", "c8");
        NamespaceC c3 = new NamespaceC("C3", "c6", "c9");
        List<NamespaceC> cs = Arrays.asList(c1, c2, c3);

        // when
        cRepository.saveAll(cs);

        // then
        Assert.assertNotNull(c1.getId());
        Assert.assertNotNull(c2.getId());
        Assert.assertNotNull(c3.getId());
    }

    @Test(dependsOnMethods = "testSaveAll")
    public void testFindBy() {
        // given
        NamespaceC c = new NamespaceC("C1", "c4", "different");
        cRepository.save(c);
        List<Pair<String, Object>> list = new ArrayList<>();
        list.add(new Pair<>("test_rootField", "C1")); // qualified
        list.add(new Pair<>("general", "different")); // unqualified

        // when
        Iterable<NamespaceC> result = cRepository.findAllBy(list);

        // then
        Assert.assertTrue(result.iterator().hasNext());
        Assert.assertEquals(result.iterator().next().getId(), c.getId());
    }

    @Test(dependsOnMethods = "testSaveAll")
    public void testCustomQuery() {
        // given

        // when
        NamespaceB node = bRepository.queryTyped("MATCH (n:science_aist_neo4j_it_diffspace_c_NamespaceC) where n.test_rootField contains $content RETURN n", Values.parameters("content", "C3"));

        // then
        Assert.assertNotNull(node);
        Assert.assertNull(node.getB());
    }

    @Test(dependsOnMethods = "testSaveAll")
    public void testCustomBulkQuery() {
        // given

        // when
        Iterable<NamespaceB> nodes = bRepository.queryAllTyped("MATCH (n:science_aist_neo4j_it_diffspace_c_NamespaceC) where n.test_rootField contains $content RETURN n", Values.parameters("content", "C"));

        // then
        Assert.assertNotNull(nodes);
        Assert.assertTrue(nodes.iterator().hasNext());
        nodes.iterator().next();
        Assert.assertTrue(nodes.iterator().hasNext());
        nodes.iterator().next();
        Assert.assertTrue(nodes.iterator().hasNext());
    }


}

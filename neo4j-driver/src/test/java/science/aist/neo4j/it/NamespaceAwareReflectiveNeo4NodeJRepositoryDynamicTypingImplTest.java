package science.aist.neo4j.it;

import science.aist.neo4j.it.namespace.BootNode;
import science.aist.neo4j.it.namespace.RootNode;
import science.aist.neo4j.it.nodes.BnalyticsNode;
import science.aist.neo4j.reflective.ClassInformation;
import science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Andreas Pointner
 * @since 1.0
 */
public class NamespaceAwareReflectiveNeo4NodeJRepositoryDynamicTypingImplTest extends AbstractDbTest {

    @Autowired
    ReflectiveNeo4JNodeRepositoryImpl<RootNode> rootRepository;

    @Autowired
    ReflectiveNeo4JNodeRepositoryImpl<BootNode> bootRepository;

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
    public void createClassInformationB() {
        // given

        // when
        ClassInformation info = ClassInformation.constructClassInformation(BootNode.class);

        // then
        Assert.assertEquals(info.getNamespace(), "test");
        Assert.assertEquals(info.getNsLabels(), "test_BootNode:test_RootNode");
    }

    @Test
    public void testDynamicClassing() {
        // given
        BnalyticsNode b1 = new BnalyticsNode("B1");
        b1.setDontIgnoreMe("B1");
        BnalyticsNode b2 = new BnalyticsNode("B2");
        b2.setDontIgnoreMe("B2");
        BnalyticsNode b3 = new BnalyticsNode("B3");
        b3.setDontIgnoreMe("B3");
        BnalyticsNode b4 = new BnalyticsNode("B4");
        b4.setDontIgnoreMe("B4");
        BnalyticsNode b5 = new BnalyticsNode("B5");
        b5.setDontIgnoreMe("B5");

        BootNode node = new BootNode("XXX");
        node.setAnalytics(b1);
        node.setTheOtherAnalytics(b2);
        node.addDynamicTypeChecker(b3);
        node.addDynamicTypeChecker(b4);
        node.addDynamicTypeChecker(b5);

        // when
        Long id = bootRepository.save(node).getId();
        BootNode bNalyticsNodes = bootRepository.findById(id);

        // then
        Assert.assertNotNull(bNalyticsNodes);
        Assert.assertEquals(bNalyticsNodes.getAnalytics().getDontIgnoreMe(), "B1");
        Assert.assertEquals(bNalyticsNodes.getTheOtherAnalytics().getTitle(), "B2"); // not actually a BnalyticsNode
        Assert.assertEquals(BnalyticsNode.class, bNalyticsNodes.getDynamicTypeChecker().get(0).getClass());
        Assert.assertEquals(BnalyticsNode.class, bNalyticsNodes.getDynamicTypeChecker().get(1).getClass());
        Assert.assertEquals(BnalyticsNode.class, bNalyticsNodes.getDynamicTypeChecker().get(2).getClass());
    }

}

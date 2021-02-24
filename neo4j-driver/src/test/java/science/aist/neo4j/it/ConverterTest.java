package science.aist.neo4j.it;

import science.aist.neo4j.it.dummy.A;
import science.aist.neo4j.it.dummy.B;
import science.aist.neo4j.it.dummy.C;
import science.aist.neo4j.it.nodes.NodiestNode;
import science.aist.neo4j.it.nodes.Point;
import science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Andreas Pointner
 * @since 1.0
 */
public class ConverterTest extends AbstractDbTest {

    @Autowired
    @Qualifier("nodiestNodeRepository")
    ReflectiveNeo4JNodeRepositoryImpl<NodiestNode> repository;

    public long id;

    @Test
    public void testSaveConversions() {
        // given
        NodiestNode node = new NodiestNode("TEST");
        node.setIntegerComplexTest(1234);
        node.setIntegerTest(4321);
        node.setListString(new LinkedList<>(Arrays.asList("A", "B", "C")));
        node.setArrayString(new String[]{"D", "E", "F"});
        node.setMapString(new HashMap<>());
        node.getMapString().put("X", "1");
        node.getMapString().put("Y", "2");
        node.setSimpleDatatypeArray(new float[]{1.0f, 2.0f, 3.0f});
        ArrayList<Class>[] arrayLists = new ArrayList[2];
        arrayLists[1] = new ArrayList<>();
        arrayLists[1].add(Object.class);
        node.setTypedArray(arrayLists);
        node.setHardMap(new HashMap<>());
        List<List<Class>> nestedList = new ArrayList<>();
        node.getHardMap().put(NodiestNode.class, nestedList);
        nestedList.add(Arrays.asList(A.class, B.class, C.class));
        node.setPoint(new Point(1, 2));
        node.setPoints(new ArrayList<>());
        node.getPoints().add(new Point(3, 4));
        node.getPoints().add(new Point(5, 6));
        node.setPointsOnlyChildConveter(new ArrayList<>());
        node.getPointsOnlyChildConveter().add(new Point(7, 8));
        node.getPointsOnlyChildConveter().add(new Point(9, 10));

        // when
        repository.save(node);

        // then
        Assert.assertNotNull(node.getId());
        id = node.getId();
    }

    @Test(dependsOnMethods = "testSaveConversions")
    public void testLoadConversions() {
        // given

        // when
        // The query typed ensures that the @ of the "OtherPointConverter" is used instead of the "," in the regular one.
        NodiestNode node = repository.queryTyped("match (n) where n.point = \"1@2\" and n.points=\"3@4;5@6\" and n.`pointsOnlyChildConveter.0`=\"7@8\" return n", null);

        // then
        Assert.assertNotNull(node);
        Assert.assertEquals(node.getIntegerComplexTest(), new Integer(1234));
        Assert.assertEquals(node.getIntegerTest(), 4321);
        Assert.assertNotNull(node.getListString());
        Assert.assertEquals(node.getListString().size(), 3);
        Assert.assertTrue(node.getListString().contains("A"));
        Assert.assertTrue(node.getListString().contains("B"));
        Assert.assertTrue(node.getListString().contains("C"));
        Assert.assertNotNull(node.getArrayString());
        Assert.assertEquals(node.getArrayString().length, 3);
        Assert.assertEquals(node.getArrayString()[0], "D");
        Assert.assertEquals(node.getArrayString()[1], "E");
        Assert.assertEquals(node.getArrayString()[2], "F");
        Assert.assertNotNull(node.getMapString());
        Assert.assertEquals(node.getMapString().size(), 2);
        Assert.assertEquals(node.getMapString().get("X"), "1");
        Assert.assertEquals(node.getMapString().get("Y"), "2");
        Assert.assertEquals(node.getTypedArray().length, 2);
        Assert.assertNull(node.getTypedArray()[0]);
        Assert.assertNotNull(node.getTypedArray()[1]);
        Assert.assertTrue(node.getTypedArray()[1].contains(Object.class));
        Assert.assertNotNull(node.getHardMap());
        Assert.assertTrue(node.getHardMap().containsKey(NodiestNode.class));
        Assert.assertEquals(node.getHardMap().get(NodiestNode.class).size(), 1);
        Assert.assertEquals(node.getHardMap().get(NodiestNode.class).get(0).size(), 3);
        Assert.assertTrue(node.getHardMap().get(NodiestNode.class).get(0).contains(A.class));
        Assert.assertTrue(node.getHardMap().get(NodiestNode.class).get(0).contains(B.class));
        Assert.assertTrue(node.getHardMap().get(NodiestNode.class).get(0).contains(C.class));
        Assert.assertNotNull(node.getPoint());
        Assert.assertEquals(node.getPoint().getX(), 1);
        Assert.assertEquals(node.getPoint().getY(), 2);
        Assert.assertNotNull(node.getPoints());
        Assert.assertEquals(node.getPoints().size(), 2);
        Assert.assertNotNull(node.getSimpleDatatypeArray());
        Assert.assertEquals(node.getSimpleDatatypeArray().length, 3);
        Assert.assertEquals(node.getSimpleDatatypeArray()[0], 1.0f);
        Assert.assertEquals(node.getSimpleDatatypeArray()[1], 2.0f);
        Assert.assertEquals(node.getSimpleDatatypeArray()[2], 3.0f);
    }
}

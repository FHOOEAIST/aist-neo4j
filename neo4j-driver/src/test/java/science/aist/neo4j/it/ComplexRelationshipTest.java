package science.aist.neo4j.it;

import science.aist.neo4j.it.mapRelations.ComplexRelationshipChildNode;
import science.aist.neo4j.it.mapRelations.ComplexRelationshipParentNode;
import science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andreas Pointner
 * @since 1.0
 */
public class ComplexRelationshipTest extends AbstractDbTest {

    @Autowired
    @Qualifier("complexRelationshipParentRepository")
    ReflectiveNeo4JNodeRepositoryImpl<ComplexRelationshipParentNode> repository;


    @Autowired
    @Qualifier("complexRelationshipChildRepository")
    ReflectiveNeo4JNodeRepositoryImpl<ComplexRelationshipChildNode> childRepository;

    public long id;

    @Test
    public void testSaveCycle() {
        // given
        ComplexRelationshipParentNode cycleContainer = new ComplexRelationshipParentNode();
        ComplexRelationshipParentNode parent = new ComplexRelationshipParentNode();
        ComplexRelationshipParentNode otherParent = new ComplexRelationshipParentNode();
        cycleContainer.getCyclicRef().add(parent);
        parent.getCyclicRef().add(otherParent);
        otherParent.getCyclicRef().add(parent);

        // when
        repository.save(cycleContainer);

        // then
        Assert.assertNotNull(cycleContainer.getId());
        Assert.assertNotNull(parent.getId());
        Assert.assertNotNull(otherParent.getId());
        Assert.assertNotNull(repository.findById(cycleContainer.getId()));
        Assert.assertNotNull(repository.findById(parent.getId()));
        Assert.assertNotNull(repository.findById(otherParent.getId()));
    }

    @Test
    public void testSaveComplex() {
        // given
        ComplexRelationshipParentNode parent = new ComplexRelationshipParentNode();
        parent.setComplexRelationship(new HashMap<>());
        parent.getComplexRelationship().put("A", new ComplexRelationshipChildNode("childA"));
        parent.getComplexRelationship().put("B", new ComplexRelationshipChildNode("childB"));
        parent.getComplexRelationship().put("C", new ComplexRelationshipChildNode("childC"));
        parent.setOtherComplexRelationship(new HashMap<>());
        parent.getOtherComplexRelationship().put("A", new ComplexRelationshipChildNode("childX"));
        parent.getOtherComplexRelationship().put("B", new ComplexRelationshipChildNode("childY"));
        parent.getOtherComplexRelationship().put("C", new ComplexRelationshipChildNode("childZ"));
        parent.setSuperComplexRelationship(new HashMap<>());
        parent.getSuperComplexRelationship().put(Object.class, new ComplexRelationshipChildNode("childD"));
        parent.getSuperComplexRelationship().put(Map.class, new ComplexRelationshipChildNode("childE"));
        parent.getSuperComplexRelationship().put(List.class, new ComplexRelationshipChildNode("childF"));
        parent.setInvertedComplexRelationship(new HashMap<>());
        parent.getInvertedComplexRelationship().put(new ComplexRelationshipChildNode("patema"), 3.0);
        parent.getInvertedComplexRelationship().put(new ComplexRelationshipChildNode("inverted"), 1.0);
        parent.setComplexArrayRelationship(new ComplexRelationshipChildNode[]{
            new ComplexRelationshipChildNode("arrA"), new ComplexRelationshipChildNode("arrB"), new ComplexRelationshipChildNode("arrC")});

        // when
        id = repository.save(parent).getId();

        // then
        Assert.assertNotNull(parent.getId());
        Assert.assertNotNull(parent.getComplexRelationship().get("A").getId());
        Assert.assertNotNull(parent.getComplexRelationship().get("B").getId());
        Assert.assertNotNull(parent.getComplexRelationship().get("C").getId());
        Assert.assertNotNull(parent.getInvertedComplexRelationship().keySet().iterator().next().getId());
    }

    @Test(dependsOnMethods = "testSaveComplex")
    public void testLoadComplex() {
        // given

        // when
        ComplexRelationshipParentNode node = repository.findById(id);

        // then
        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getComplexRelationship());
        Assert.assertEquals(node.getComplexRelationship().size(), 3);
        Assert.assertEquals(node.getComplexRelationship().get("A").getValue(), "childA");
        Assert.assertEquals(node.getComplexRelationship().get("B").getValue(), "childB");
        Assert.assertEquals(node.getComplexRelationship().get("C").getValue(), "childC");
        Assert.assertNotNull(node.getOtherComplexRelationship());
        Assert.assertEquals(node.getOtherComplexRelationship().size(), 3);
        Assert.assertEquals(node.getOtherComplexRelationship().get("A").getValue(), "childX");
        Assert.assertEquals(node.getOtherComplexRelationship().get("B").getValue(), "childY");
        Assert.assertEquals(node.getOtherComplexRelationship().get("C").getValue(), "childZ");
        Assert.assertNotNull(node.getSuperComplexRelationship());
        Assert.assertEquals(node.getSuperComplexRelationship().size(), 3);
        Assert.assertEquals(node.getSuperComplexRelationship().get(Object.class).getValue(), "childD");
        Assert.assertEquals(node.getSuperComplexRelationship().get(Map.class).getValue(), "childE");
        Assert.assertEquals(node.getSuperComplexRelationship().get(List.class).getValue(), "childF");
        Assert.assertNotNull(node.getComplexArrayRelationship());
        Assert.assertEquals(node.getComplexArrayRelationship().length, 3);
        Assert.assertEquals(node.getComplexArrayRelationship()[0].getValue(), "arrA");
        Assert.assertEquals(node.getComplexArrayRelationship()[1].getValue(), "arrB");
        Assert.assertEquals(node.getComplexArrayRelationship()[2].getValue(), "arrC");
        Assert.assertNotNull(node.getInvertedComplexRelationship());
        Assert.assertEquals(node.getInvertedComplexRelationship().size(), 2);
        Assert.assertNotNull(node.getInvertedComplexRelationship().keySet().stream().filter(x -> x.getValue().equals("patema")).findFirst().orElse(null));
        Assert.assertNotNull(node.getInvertedComplexRelationship().keySet().stream().filter(x -> x.getValue().equals("inverted")).findFirst().orElse(null));
        Assert.assertEquals(3.0, 3.0);
        Assert.assertEquals(node.getInvertedComplexRelationship().get(node.getInvertedComplexRelationship().keySet().stream().filter(x -> x.getValue().equals("patema")).findFirst().orElseGet(null)), Double.valueOf(3.0));
        Assert.assertEquals(node.getInvertedComplexRelationship().get(node.getInvertedComplexRelationship().keySet().stream().filter(x -> x.getValue().equals("inverted")).findFirst().orElseGet(null)), Double.valueOf(1.0));
    }

    @Test(dependsOnMethods = "testLoadComplex")
    public void testReSaveComplex() {
        // given
        ComplexRelationshipParentNode node = repository.findById(id);
        node.getComplexRelationship().get("A").setValue("CHANGED_CHILD");
        long childId = node.getComplexRelationship().get("A").getId();

        // TODO #2 we can't currently override as this is equivalent to a relationship delete
        // node.getComplexRelationship().put("B", new ComplexRelationshipChildNode("NEW CHILD"));

        // when
        childRepository.save(node.getComplexRelationship().get("A"));
        repository.save(node);
        node = repository.findById(id);

        // then
        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getComplexRelationship());
        Assert.assertEquals(node.getComplexRelationship().size(), 3);
        Assert.assertTrue(node.getComplexRelationship().get("A").getId() == childId);
        Assert.assertEquals(node.getComplexRelationship().get("A").getValue(), "CHANGED_CHILD");
        // TODO #2 we can't currently override as this is equivalent to a relationship delete
        Assert.assertEquals(node.getComplexRelationship().get("B").getValue(), "childB");
        //Assert.assertEquals(node.getComplexRelationship().get("B").getValue(), "NEW CHILD");
        Assert.assertEquals(node.getComplexRelationship().get("C").getValue(), "childC");
    }
}

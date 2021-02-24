package science.aist.neo4j.it;

import science.aist.neo4j.it.namespace.NodeWithExtensions;
import science.aist.neo4j.it.namespace.a.NodeWithOtherExtensions;
import science.aist.neo4j.namespace.NamespaceAwareReflectiveNeo4JNodeRepositoryImpl;
import science.aist.neo4j.reflective.ClassInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andreas Pointner
 * @since 1.0
 */
public class ExtensionTest extends AbstractDbTest {

    @Autowired
    NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<NodeWithExtensions> nodeWithExtensionsRepository;

    @Test
    public void createClassInformationExtension() {
        // given

        // when
        ClassInformation info = ClassInformation.constructClassInformation(NodeWithExtensions.class, true);

        // then
        Assert.assertNotNull(info.getFields().get("test_fieldInClass"));
        Assert.assertNull(info.getFields().get("test_extendingField"));
        Assert.assertNull(info.getFields().get("test_extendingCollectionField"));
        Assert.assertNotNull(info.getFields().get("extension_random_extension_field"));
        Assert.assertNotNull(info.getFields().get("extension_random_extension_collection"));
    }

    @Test
    public void createClassInformationDifferentExtension() {
        // given

        // when
        ClassInformation info = ClassInformation.constructClassInformation(NodeWithOtherExtensions.class, true);

        // then
        Assert.assertNotNull(info.getFields().get("science_aist_neo4j_it_namespace_a_fieldInClass"));
        Assert.assertNull(info.getFields().get("test_extendingField"));
        Assert.assertNull(info.getFields().get("test_extendingCollectionWithDifferentFieldname"));
        Assert.assertNotNull(info.getFields().get("extension_random_extension_sameNameDifferentExtension"));
        Assert.assertNotNull(info.getFields().get("extension_random_extension_collection"));
    }

    @Test
    public void storeWithExtension() {
        // given
        NodeWithExtensions ex = new NodeWithExtensions();
        ex.setFieldInClass("fieldInClassValue");
        ex.setExtendingField("YAY our first extension");
        ex.setExtendingCollectionField(new ArrayList<>());
        ex.getExtendingCollectionField().add("Collection Extension A");
        ex.getExtendingCollectionField().add("Collection Extension B");
        ex.getExtendingCollectionField().add("Collection Extension C");
        ClassInformation classInformation = ClassInformation.constructClassInformation(NodeWithExtensions.class, true);
        classInformation.setExtension(ex, "singular", "Expression");
        List<String> plural = new ArrayList<>();
        plural.add("One");
        plural.add("Two");
        plural.add("Three");
        classInformation.setExtension(ex, "plural", plural);

        // when
        nodeWithExtensionsRepository.save(ex);

        // then
        Assert.assertNotNull(ex);
        storedId = ex.getId();
    }

    private long storedId;

    @Test(dependsOnMethods = "storeWithExtension")
    public void loadWithExtension() {
        // given
        long storedId = this.storedId;
        ClassInformation classInformation = ClassInformation.constructClassInformation(NodeWithExtensions.class, true);

        // when
        NodeWithExtensions ex = nodeWithExtensionsRepository.findById(storedId);

        // then
        Assert.assertNotNull(ex.getExtendingField());
        Assert.assertNotNull(ex.getExtendingCollectionField());
        Assert.assertEquals(ex.getExtendingField(), "YAY our first extension");
        Assert.assertEquals(ex.getExtendingCollectionField().size(), 3);
        Assert.assertEquals(ex.getExtendingCollectionField().get(0), "Collection Extension A");
        Assert.assertEquals(ex.getExtendingCollectionField().get(1), "Collection Extension B");
        Assert.assertEquals(ex.getExtendingCollectionField().get(2), "Collection Extension C");
        Assert.assertNotNull(classInformation.getExtension(ex, "singular"));
        Assert.assertEquals(classInformation.getExtension(ex, "singular"), "Expression");
        Assert.assertNotNull(classInformation.getExtension(ex, "plural"));
        List<String> plural = (List<String>) classInformation.getExtension(ex, "plural");
        Assert.assertEquals(plural.size(), 3);
        Assert.assertEquals(plural.get(0), "One");
        Assert.assertEquals(plural.get(1), "Two");
        Assert.assertEquals(plural.get(2), "Three");

    }

    @Test(dependsOnMethods = "loadWithExtension")
    public void castToOtherClass() {
        // given
        long storedId = this.storedId;
        NodeWithExtensions ex = nodeWithExtensionsRepository.findById(storedId);
        ClassInformation classInformation = ClassInformation.constructClassInformation(NodeWithOtherExtensions.class, true);

        // when
        NodeWithOtherExtensions nodeWithOtherExtensions = ClassInformation.castToClass(ex, NodeWithOtherExtensions.class);

        // then
        Assert.assertNull(nodeWithOtherExtensions.getFieldInClass());
        Assert.assertNull(nodeWithOtherExtensions.getExtendingField());
        Assert.assertNotNull(nodeWithOtherExtensions.getExtendingCollectionWithDifferentFieldname());
        Assert.assertEquals(nodeWithOtherExtensions.getExtendingCollectionWithDifferentFieldname().size(), 3);
        Assert.assertEquals(nodeWithOtherExtensions.getExtendingCollectionWithDifferentFieldname().get(0), "Collection Extension A");
        Assert.assertEquals(nodeWithOtherExtensions.getExtendingCollectionWithDifferentFieldname().get(1), "Collection Extension B");
        Assert.assertEquals(nodeWithOtherExtensions.getExtendingCollectionWithDifferentFieldname().get(2), "Collection Extension C");
        Assert.assertNotNull(classInformation.getExtension(nodeWithOtherExtensions, "random_extension_field"));
        Assert.assertEquals(classInformation.getExtension(nodeWithOtherExtensions, "random_extension_field"), "YAY our first extension");
        Assert.assertNotNull(nodeWithOtherExtensions.getSingular());
        Assert.assertEquals(nodeWithOtherExtensions.getSingular(), "Expression");
        Assert.assertNotNull(classInformation.getExtension(nodeWithOtherExtensions, "plural"));
        List<String> plural = (List<String>) classInformation.getExtension(nodeWithOtherExtensions, "plural");
        Assert.assertEquals(plural.size(), 3);
        Assert.assertEquals(plural.get(0), "One");
        Assert.assertEquals(plural.get(1), "Two");
        Assert.assertEquals(plural.get(2), "Three");
    }

}

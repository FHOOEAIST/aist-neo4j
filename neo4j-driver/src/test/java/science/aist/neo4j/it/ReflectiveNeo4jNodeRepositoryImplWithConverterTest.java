package science.aist.neo4j.it;

import science.aist.neo4j.Neo4jQueryRepository;
import science.aist.neo4j.Neo4jRepository;
import science.aist.neo4j.it.nodes.LineNode;
import science.aist.neo4j.it.nodes.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl;

/**
 * <p>Tests {@link ReflectiveNeo4JNodeRepositoryImpl} with {@link org.neo4j.ogm.typeconversion.AttributeConverter}</p>
 *
 * @author Andreas Pointner
 * @since 1.0
 */
public class ReflectiveNeo4jNodeRepositoryImplWithConverterTest extends AbstractDbTest {
    @Autowired
    @Qualifier("reflectiveRepoLine")
    Neo4jRepository<LineNode, Long> lineNodeLongNeo4jRepository;

    @Autowired
    @Qualifier("nodeWithExtensionsRepository")
    Neo4jQueryRepository queryRepository;

    @BeforeMethod
    @AfterMethod
    public void cleanup() {
        queryRepository.query("MATCH (n:LineNode) DETACH DELETE n", null, Void.class);
    }

    @Test
    public void testSave() {
        // given
        Point p1 = new Point(1, 2);
        Point p2 = new Point(3, 4);
        LineNode ln = new LineNode(p1, p2);

        // when
        ln = lineNodeLongNeo4jRepository.save(ln);

        // then
        Assert.assertNotNull(ln);
        Assert.assertNotNull(ln.getId());
    }

    @Test
    public void testLoad() {
        // given
        Point p1 = new Point(1, 2);
        Point p2 = new Point(3, 4);
        LineNode ln = new LineNode(p1, p2);
        ln = lineNodeLongNeo4jRepository.save(ln);

        // when
        LineNode loadedNode = lineNodeLongNeo4jRepository.findById(ln.getId());

        // then
        Assert.assertNotNull(loadedNode);
        Assert.assertNotNull(loadedNode.getId());
        Assert.assertEquals(loadedNode.getId(), ln.getId());
        Assert.assertNotNull(loadedNode.getStartPoint());
        Assert.assertNotNull(loadedNode.getEndPoint());
        Assert.assertEquals(loadedNode.getStartPoint().getX(), p1.getX());
        Assert.assertEquals(loadedNode.getStartPoint().getY(), p1.getY());
        Assert.assertEquals(loadedNode.getEndPoint().getX(), p2.getX());
        Assert.assertEquals(loadedNode.getEndPoint().getY(), p2.getY());
    }
}

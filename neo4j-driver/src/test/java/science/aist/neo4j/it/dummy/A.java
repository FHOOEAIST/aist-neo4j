package science.aist.neo4j.it.dummy;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Dummy test class</p>
 *
 * @author Christoph Praschl
 * @since 1.0
 */
@NodeEntity
public class A {

    public Long id;

    @Relationship
    public List<B> elements = new ArrayList<>();

    @Relationship
    public B singleElement;

}

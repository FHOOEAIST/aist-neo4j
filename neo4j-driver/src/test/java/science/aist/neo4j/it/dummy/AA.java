package science.aist.neo4j.it.dummy;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

/**
 * <p>Dummy testclass</p>
 *
 * @author Christoph Praschl
 * @since 1.0
 */
@NodeEntity
public class AA extends A {

    @Relationship
    public B b;

    @Relationship
    public List<B> bees;

}

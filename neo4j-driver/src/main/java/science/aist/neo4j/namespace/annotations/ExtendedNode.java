package science.aist.neo4j.namespace.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a node class that extends an existing class in a parent namespace
 * The PARENT is the label of the parent class that the node class extends.
 * <p>
 * This annotation is only required if the java class doesn't extend from the corresponding root class.
 * <p>
 * Please be aware that if there is a chain of extensions, this must be FULLY qualifying:
 * Ex. test.A -&gt; test.x.B -&gt; ClassWithAnnotation. -&gt; parent = test_x_B:test_A
 *
 * @author Oliver Krauss
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExtendedNode {

    String PARENT = null;

    String parent();
}

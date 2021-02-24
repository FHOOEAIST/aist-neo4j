package science.aist.neo4j.namespace.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Package Level Annotation for Neo4J Namespaces. (annotation needs to be in package-info.java at the package level)
 * This annotation exists solely to shorten the usual namespace down to "NS".
 * Namespaces don't understand extensions of each other, this actually happens on the class level
 * <p>
 * Ex.: Class in science.example.ExampleNode
 * Annotation with NS = "ex" -&gt; "ex"
 * No Annotation -&gt; "science.example"
 * <p>
 * Warning: We have no conflict resolution. If you define the same namespace for different packages with the same class-labels
 * this will inevitably lead to collisions.
 *
 * @author Oliver Krauss
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface Namespace {

    String NS = "";

    String ns();
}

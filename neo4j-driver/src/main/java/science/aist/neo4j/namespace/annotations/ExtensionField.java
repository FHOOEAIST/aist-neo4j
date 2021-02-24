package science.aist.neo4j.namespace.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field in a class that is an extension and NOT part of the class itself.
 * Note: this has NOTHING to do with ExtendedNode
 * <p>
 * Extensions are in their own namespace "extension", which is reserved for this specific purpose.
 * Extensions can be
 * 0..1 just a regular field
 * 0..* a field with a collection type
 *
 * <p>
 * This annotation is required for all fields that you want to load. All non-declared extensions will be loaded into the hidden extensions field
 * <p>
 *
 * @author Oliver Krauss
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExtensionField {

    /**
     * Name of the extension. Please note that if this is not globally unique you will run into trouble.
     */
    String Name = "";

    String name();
}

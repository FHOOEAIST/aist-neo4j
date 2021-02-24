package science.aist.neo4j.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a static field that should be included for storage in the DB.
 * Per default static fields are excluded. Except if they have this, or the {@link org.neo4j.ogm.annotation.Relationship} annotation.
 *
 * @author Oliver Krauss
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StaticField {
}

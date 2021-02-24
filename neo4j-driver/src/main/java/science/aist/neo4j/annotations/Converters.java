package science.aist.neo4j.annotations;

import science.aist.neo4j.reflective.FieldConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface for attaching multiple {@link FieldConverter}s to a field
 *
 * @author Oliver Krauss
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Converters {

    Converter[] value();
}

package science.aist.neo4j.annotations;

import science.aist.neo4j.reflective.FieldConverter;

import java.lang.annotation.*;

/**
 * Interface for attaching a {@link FieldConverter} to a field
 *
 * @author Oliver Krauss
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(Converters.class)
public @interface Converter {

    /**
     * Class that the converter shall be attached to
     */
    Class overrides = null;

    Class overrides() default void.class;

    /**
     * Converter to be loaded for field conversion
     */
    Class<? extends FieldConverter> converter = null;

    Class<? extends FieldConverter> converter();
}

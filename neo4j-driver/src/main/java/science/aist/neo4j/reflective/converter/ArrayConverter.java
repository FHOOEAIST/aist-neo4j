package science.aist.neo4j.reflective.converter;

import science.aist.neo4j.reflective.FieldConverter;

/**
 * @author Oliver Krauss
 * @since 1.0
 */

public interface ArrayConverter<D, T> extends FieldConverter<D, T[]> {

    /**
     * Injects converter to transform a string to a type
     * @param typeConverter to be used for conversion
     */
    void setTypeConverter(FieldConverter<String, T> typeConverter);
}

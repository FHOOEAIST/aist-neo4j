package science.aist.neo4j.reflective.converter;

import science.aist.neo4j.reflective.FieldConverter;

import java.util.Collection;

/**
 * Converter that handles lists
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public interface CollectionConverter<V> extends FieldConverter<String, Collection<V>> {

    /**
     * Converter for the type the list contains
     *
     * @param valueConverter to do the conversion
     */
    void setTypeConverter(FieldConverter<String, V> valueConverter);

    /**
     * Functionality called by {@link ConverterProvider} to determine which class this will be initialized with.
     *
     * @param type for whilc initialization should be determined
     */
    void determineInitialization(Class type);
}

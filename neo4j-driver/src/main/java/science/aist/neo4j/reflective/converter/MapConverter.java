package science.aist.neo4j.reflective.converter;

import science.aist.neo4j.reflective.FieldConverter;

import java.util.Map;

/**
 * Converter that handles arrays
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public interface MapConverter<K, V> extends FieldConverter<String, Map<K, V>> {

    /**
     * Injects converter to convert a string to a map's key class
     * @param keyConverter used to convert string
     */
    void setKeyConverter(FieldConverter<String, K> keyConverter);

    /**
     * Injects converter to convert a string to a map's value class
     * @param valueConverter used to convert string
     */
    void setValueConverter(FieldConverter<String, V> valueConverter);
}

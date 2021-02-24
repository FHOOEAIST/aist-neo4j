package science.aist.neo4j.reflective.converter;

import science.aist.neo4j.reflective.FieldConverter;

import java.util.Map;

/**
 * Converter for int values, as they are implicitly cast to Long by Neo4J
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class IntConverter implements FieldConverter<Long, Integer> {

    @Override
    public void mapForDb(String name, Integer value, Map<String, Object> map) {
        if (value == null) {
            return;
        }
        map.put(name, value);
    }

    @Override
    public Integer toJavaValue(Integer currentValue, Object value) {
        if (value == null) {
            return null;
        }
        return ((Long) value).intValue();
    }
}

package science.aist.neo4j.reflective;

import java.util.Map;

/**
 * Converter interface for transforming a field to a db value.
 *
 * @param <D> Database value
 * @param <J> Java value
 * @author Oliver Krauss
 * @since 1.0
 */
public interface FieldConverter<D, J> {

    /**
     * Adds the value to the map for database processing
     *
     * @param name  of field
     * @param value to be pushed to db
     * @param map   map the value will be entered in
     */
    void mapForDb(String name, J value, Map<String, Object> map);

    /**
     * Transforms from db value to value understood by the java class
     *
     * @param currentValue value the java object has right now
     * @param newValue     db to be transformed
     * @return Java value
     */
    J toJavaValue(J currentValue, Object newValue);
}

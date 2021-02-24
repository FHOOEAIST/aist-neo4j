package science.aist.neo4j.reflective.converter;

import science.aist.neo4j.reflective.FieldConverter;
import science.aist.seshat.Logger;

import java.util.Map;

/**
 * Converter for int values, as they are implicitly cast to Long by Neo4J
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class ClassConverter implements FieldConverter<String, Class> {


    @Override
    public void mapForDb(String name, Class value, Map<String, Object> map) {
        if (value == null) {
            return;
        }
        // TODO #21 - We should extract the reserved signs and consolidate them soon
        map.put(name, value.getName().replace('.', '|'));
    }

    @Override
    public Class toJavaValue(Class currentValue, Object newValue) {
        if (newValue == null) {
            return null;
        }
        try {
            String classname = ((String) newValue).replace('|', '.');
            switch (classname) {
                case "byte":
                    return byte.class;
                case "char":
                    return char.class;
                case "short":
                    return short.class;
                case "int":
                    return int.class;
                case "long":
                    return long.class;
                case "double":
                    return double.class;
                case "float":
                    return float.class;
                case "boolean":
                    return boolean.class;
                case "void":
                    return void.class;
                default:
                    return Class.forName(classname);
            }
        } catch (ClassNotFoundException e) {
            Logger.getInstance().error("Could not cast class from DB " + newValue, e);
        }
        return null;
    }
}

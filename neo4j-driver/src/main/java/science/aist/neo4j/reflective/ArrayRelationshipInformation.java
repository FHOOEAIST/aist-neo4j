package science.aist.neo4j.reflective;

import science.aist.neo4j.reflective.converter.ArrayConverterImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handles the metadata for the {@link ReflectiveNeo4JNodeRepositoryImpl}
 * It contains information on an Array relationship NODE[] where the INDEX will be moved into a {@link MapRelationship}.
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class ArrayRelationshipInformation extends KeyRelationshipInformation {

    public ArrayRelationshipInformation(String name, Field field, Method getter, Method setter, Class genericClass, String type, String direction) {
        super(name, field, getter, setter, genericClass, type, direction, field.getDeclaringClass(), field.getType().getComponentType(), String.class);
    }

    @Override
    public Object get(Object object) {
        Object[] o = (Object[]) super.get(object);
        if (o == null) {
            return null;
        }

        HashMap<Object, Long> finalSync = getSync(object);
        String keyHeader = "";
        if (finalSync != null && !finalSync.isEmpty()) {
            keyHeader = (String)finalSync.keySet().iterator().next();
            keyHeader = keyHeader.substring(0, keyHeader.indexOf("/") + 1);
        }

        List<MapRelationship> list = new ArrayList<>();
        for (int i = 0; i < o.length; i++) {
            list.add(new MapRelationship(finalSync.getOrDefault(keyHeader + i, null), object, o[i], o.length + String.valueOf(ArrayConverterImpl.SIZE_POSTFIX) + i));
        }
        return list;
    }
}

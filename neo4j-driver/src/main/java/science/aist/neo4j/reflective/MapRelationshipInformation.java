package science.aist.neo4j.reflective;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the metadata for the {@link ReflectiveNeo4JNodeRepositoryImpl}
 * It contains information on a Map relationship {@code Map<Key, NODE>} where the Key will be moved into a {@link MapRelationship}.
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class MapRelationshipInformation extends KeyRelationshipInformation {

    public MapRelationshipInformation(String name, Field field, Method getter, Method setter, Class genericClass, String type, String direction, Type genericType) {
        super(name, field, getter, setter, genericClass, type, direction, field.getDeclaringClass(),
            (Class) ((ParameterizedType) genericType).getActualTypeArguments()[1],
            (Class) ((ParameterizedType) genericType).getActualTypeArguments()[0]);
    }

    @Override
    public Object get(Object object) {
        Map<Object, Object> o = (Map<Object, Object>) super.get(object);
        if (o == null) {
            return null;
        }

        HashMap<Object, Long> finalSync = getSync(object);

        List<MapRelationship> list = new ArrayList<>();
        o.entrySet().forEach(x -> {
            if (inverted) {
                list.add(new MapRelationship(finalSync.getOrDefault(x.getKey(), null), object, x.getKey(), x.getValue()));
            } else {
                list.add(new MapRelationship(finalSync.getOrDefault(x.getKey(), null), object, x.getValue(), x.getKey()));
            }
        });

        return list;
    }
}

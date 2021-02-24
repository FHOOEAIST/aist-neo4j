package science.aist.neo4j.it.converter;

import science.aist.neo4j.it.nodes.Point;
import science.aist.neo4j.reflective.FieldConverter;
import science.aist.neo4j.reflective.converter.CollectionConverter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Convert a point into a string and vice versa</p>
 *
 * @author Andreas Pointner
 * @author Oliver Krauss
 * @since 1.0
 */
public class PointListConverter implements CollectionConverter<Point> {

    private FieldConverter<String, Point> typeConverter;

    @Override
    public void setTypeConverter(FieldConverter<String, Point> valueConverter) {
        this.typeConverter = valueConverter;
    }

    @Override
    public void determineInitialization(Class type) {
        // nothing to see here
    }

    @Override
    public void mapForDb(String name, Collection<Point> value, Map<String, Object> map) {
        map.put(name, value.stream().map(x -> {
            HashMap<String, Object> hashMap = new HashMap<>();
            typeConverter.mapForDb("tmp", x, hashMap);
            return (String) hashMap.get("tmp");
        }).collect(Collectors.joining(";")));
    }

    @Override
    public Collection<Point> toJavaValue(Collection<Point> currentValue, Object newValue) {
        String[] split = ((String) newValue).split(";");
        return Arrays.stream(split).map(x -> typeConverter.toJavaValue(null, x)).collect(Collectors.toList());
    }
}

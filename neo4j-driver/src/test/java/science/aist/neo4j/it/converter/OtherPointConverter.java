package science.aist.neo4j.it.converter;

import science.aist.neo4j.it.nodes.Point;
import science.aist.neo4j.reflective.FieldConverter;

import java.util.Map;

/**
 * Switched out the literal, so I can trace in the DB if this converter will be used or the regular one.
 *
 * @author Andreas Pointner
 * @author Oliver Krauss
 * @since 1.0
 */
public class OtherPointConverter implements FieldConverter<String, Point> {

    @Override
    public void mapForDb(String name, Point value, Map<String, Object> map) {
        if (value == null) {
            return;
        }
        map.put(name, value.getX() + "@" + value.getY());
    }

    @Override
    public Point toJavaValue(Point currentValue, Object value) {
        String[] split = ((String) value).split("@");
        if (split.length != 2)
            throw new IllegalArgumentException("the value string must contain two ints separated by a ','");
        return new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }
}

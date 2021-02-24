package science.aist.neo4j.util;

import org.neo4j.driver.Value;
import org.neo4j.driver.types.IsoDuration;
import org.neo4j.driver.types.Point;

import java.time.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>Util class to cast different values</p>
 *
 * @author Andreas Pointner
 * @author Christoph Praschl
 * @since 1.0
 */
public class ValueCast {
    private ValueCast() {

    }

    /**
     * Casts anything to anything
     *
     * @param s   the element to cast
     * @param <T> the type in which it gets casted
     * @param <S> the input type
     * @return the casted elemented
     */
    @SuppressWarnings("unchecked")
    public static <T, S> T cast(S s) {
        return (T) s;
    }

    /**
     * Casts a value return to a java language type
     *
     * @param result the value which should be casted
     * @param clazz  the class of the type in which it should be casted
     * @param <T>    the type in which it should be casted
     * @return the casted result value
     */
    public static <T> T castToJavaLang(Value result, Class<T> clazz) {
        if (clazz.equals(Boolean.class)) {
            return cast(result.asBoolean());
        }
        if (clazz.equals(String.class)) {
            return cast(result.asString());
        }
        if (clazz.equals(Character.class)) {
            return cast(result.asInt());
        }
        if (clazz.equals(Long.class)) {
            return cast(result.asLong());
        }
        if (clazz.equals(Integer.class)) {
            return cast(result.asInt());
        }
        if (clazz.equals(Double.class)) {
            return cast(result.asDouble());
        }
        if (clazz.equals(Float.class)) {
            return cast(result.asFloat());
        }
        if (clazz.equals(LocalDate.class)) {
            return cast(result.asLocalDate());
        }
        if (clazz.equals(OffsetTime.class)) {
            return cast(result.asOffsetTime());
        }
        if (clazz.equals(LocalTime.class)) {
            return cast(result.asLocalTime());
        }
        if (clazz.equals(LocalDateTime.class)) {
            return cast(result.asLocalDateTime());
        }
        if (clazz.equals(ZonedDateTime.class)) {
            return cast(result.asZonedDateTime());
        }
        if (clazz.equals(IsoDuration.class)) {
            return cast(result.asIsoDuration());
        }
        if (clazz.equals(Point.class)) {
            return cast(result.asPoint());
        }
        if (clazz.equals(List.class)) {
            return cast(result.asList());
        }
        if (clazz.equals(Map.class)) {
            return cast(result.asMap());
        }
        if (clazz.equals(Iterable.class)) {
            return cast(result.asList());
        }
        if (clazz.equals(Iterator.class)) {
            return cast(result.asList().iterator());
        }
        if (clazz.equals(byte[].class)) {
            return cast(result.asByteArray());
        }
        if (clazz.equals(Object.class)) {
            return cast(result.asObject());
        }

        throw new UnsupportedOperationException("Currently only some java.lang data types are supported. " + clazz.getSimpleName() + " is not supported!");
    }
}

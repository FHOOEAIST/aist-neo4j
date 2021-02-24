package science.aist.neo4j.reflective.converter;

import science.aist.neo4j.reflective.FieldConverter;
import science.aist.neo4j.util.Pair;

import java.lang.reflect.Array;
import java.util.Map;

/**
 * Converter that handles arrays.
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class ArrayConverterImpl<T> implements ArrayConverter<String, T> {

    /**
     * Converter for type array contains
     */
    private FieldConverter<String, T> typeConverter;

    public static final char SIZE_POSTFIX = '/';

    @Override
    public void mapForDb(String name, T[] value, Map<String, Object> map) {
        if (value == null) {
            return;
        }

        for (int i = value.length - 1; i > -1; i--) {
            // TODO #25 I am storing the LENGTH of the array to save time when loading to java. This solution could be better!
            if (typeConverter != null) {
                typeConverter.mapForDb(name + "." + value.length + SIZE_POSTFIX + i, value[i], map);
            } else {
                map.put(name + "." + value.length + SIZE_POSTFIX + i, value[i]);
            }
        }
    }

    @Override
    public T[] toJavaValue(T[] currentValue, Object newValue) {
        // load indexes
        Pair<String, Object> pair = (Pair<String, Object>) newValue;
        String strKey = pair.getKey();
        int sizeIndex = strKey.indexOf(SIZE_POSTFIX);
        int endIndex = strKey.indexOf('.');

        int key = Integer.valueOf(strKey.substring(sizeIndex + 1, endIndex >= 0 ? endIndex : strKey.length()));
        // load value
        T value;
        if (typeConverter != null) {
            Object subValue = pair.getValue();
            if (endIndex > 0) {
                subValue = new Pair<>(strKey.substring(endIndex + 1), subValue);
            }
            value = typeConverter.toJavaValue(null, subValue);
        } else {
            value = (T) pair.getValue();
        }

        // initialize array with right type
        if (currentValue == null) {
            int size = Integer.valueOf(strKey.substring(0, sizeIndex));
            currentValue = (T[]) Array.newInstance(value.getClass(), size);
        }

        // assign value
        currentValue[key] = value;

        return currentValue;
    }

    @Override
    public void setTypeConverter(FieldConverter<String, T> typeConverter) {
        this.typeConverter = typeConverter;
    }

}

package science.aist.neo4j.reflective.converter.arrayprimitive;

import science.aist.neo4j.reflective.FieldConverter;
import science.aist.neo4j.util.Pair;

import java.util.Map;

import static science.aist.neo4j.reflective.converter.ArrayConverterImpl.SIZE_POSTFIX;

/**
 * Converter that works on simple datatype array short[], which can't be cast to Object[].
 *
 * @author Oliver Krauss
 * @since 1.0
 */
// TODO #25 I am storing the LENGTH of the array to save time when loading to java. This solution could be better!
public class ShortArrayConverterImpl implements FieldConverter<String, Object> {
    @Override
    public void mapForDb(String name, Object uncastValue, Map<String, Object> map) {
        if (uncastValue == null) {
            return;
        }
        short[] value = (short[]) uncastValue;
        for (int i = value.length - 1; i > -1; i--) {
            map.put(name + "." + value.length + SIZE_POSTFIX + i, value[i]);
        }
    }

    @Override
    public Object toJavaValue(Object uncastCurrentValue, Object newValue) {
        short[] currentValue = (short[]) uncastCurrentValue;

        // load indexes
        Pair<String, Object> pair = (Pair<String, Object>) newValue;
        String strKey = pair.getKey();
        int sizeIndex = strKey.indexOf(SIZE_POSTFIX);
        int endIndex = strKey.indexOf('.');

        int key = Integer.valueOf(strKey.substring(sizeIndex + 1, endIndex >= 0 ? endIndex : strKey.length()));
        // load value
        short value = ((Long) pair.getValue()).shortValue();

        // initialize array with right type
        if (currentValue == null) {
            int size = Integer.valueOf(strKey.substring(0, sizeIndex));
            currentValue = new short[size];
        }

        // assign value
        currentValue[key] = value;

        return currentValue;
    }
}

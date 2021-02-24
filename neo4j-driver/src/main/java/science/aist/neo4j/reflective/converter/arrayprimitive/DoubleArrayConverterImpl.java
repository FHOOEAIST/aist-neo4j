package science.aist.neo4j.reflective.converter.arrayprimitive;

import science.aist.neo4j.reflective.FieldConverter;
import science.aist.neo4j.util.Pair;
import science.aist.neo4j.reflective.converter.ArrayConverterImpl;

import java.util.Map;

/**
 * Converter that works on simple datatype array double[], which can't be cast to Object[].
 *
 * @author Oliver Krauss
 * @since 1.0
 */
// TODO #25 I am storing the LENGTH of the array to save time when loading to java. This solution could be better!
public class DoubleArrayConverterImpl implements FieldConverter<String, Object> {
    @Override
    public void mapForDb(String name, Object uncastValue, Map<String, Object> map) {
        if (uncastValue == null) {
            return;
        }
        double[] value = (double[]) uncastValue;
        for (int i = value.length - 1; i > -1; i--) {
            map.put(name + "." + value.length + ArrayConverterImpl.SIZE_POSTFIX + i, value[i]);
        }
    }

    @Override
    public Object toJavaValue(Object uncastCurrentValue, Object newValue) {
        double[] currentValue = (double[]) uncastCurrentValue;

        // load indexes
        Pair<String, Object> pair = (Pair<String, Object>) newValue;
        String strKey = pair.getKey();
        int sizeIndex = strKey.indexOf(ArrayConverterImpl.SIZE_POSTFIX);
        int endIndex = strKey.indexOf('.');

        int key = Integer.valueOf(strKey.substring(sizeIndex + 1, endIndex >= 0 ? endIndex : strKey.length()));
        // load value
        double value = ((double) pair.getValue());

        // initialize array with right type
        if (currentValue == null) {
            int size = Integer.valueOf(strKey.substring(0, sizeIndex));
            currentValue = new double[size];
        }

        // assign value
        currentValue[key] = value;

        return currentValue;
    }
}

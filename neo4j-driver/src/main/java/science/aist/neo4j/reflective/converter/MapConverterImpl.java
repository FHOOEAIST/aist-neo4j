package science.aist.neo4j.reflective.converter;

import science.aist.neo4j.reflective.FieldConverter;
import science.aist.neo4j.util.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Converter that handles arrays
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class MapConverterImpl<K, V> implements MapConverter<K, V> {

    /**
     * Optional converter for the Key of the map
     */
    private FieldConverter<String, K> keyConverter;

    /**
     * Optional converter for the Value of the map
     */
    private FieldConverter<String, V> valueConverter;

    @Override
    public void mapForDb(String name, Map<K, V> value, Map<String, Object> map) {
        if (value == null) {
            return;
        }

        value.entrySet().forEach(x -> {
            // load key
            String key = null;
            if (keyConverter != null) {
                HashMap<String, Object> keymap = new HashMap<>();
                keyConverter.mapForDb("key", x.getKey(), keymap);
                key = (String) keymap.get("key");
            } else {
                key = (String) x.getKey();
            }

            // process values
            key = name + "." + key;
            if (valueConverter != null) {
                valueConverter.mapForDb(key, x.getValue(), map);
            } else {
                map.put(key, x.getValue());
            }
        });
    }

    @Override
    public Map<K, V> toJavaValue(Map<K, V> currentValue, Object newValue) {
        // initialize if necessary
        if (currentValue == null) {
            currentValue = new HashMap<>(); // TODO #20 enable something other than hashmap
        }
        Pair<String, Object> pair = (Pair<String, Object>) newValue;

        // load key
        String keyStr = "";
        K key = null;
        int subkeyIndex = -1;
        try {
            keyStr = pair.getKey();
            subkeyIndex = keyStr.indexOf(".");
            if (keyConverter != null) {
                String rootKey = keyStr;
                if (subkeyIndex > 0) {
                    rootKey = keyStr.substring(0, subkeyIndex);
                }
                key = keyConverter.toJavaValue(null, rootKey);
            } else {
                key = (K) keyStr;
            }
        } catch (Exception e) {
            // don't care happens if the key was already cast
            key = (K) pair.getKey();
        }

        // load value
        V value = null;
        if (valueConverter != null) {
            Object nestedValue = pair.getValue();
            if (subkeyIndex > 0) {
                nestedValue = new Pair<>(keyStr.substring(subkeyIndex + 1), nestedValue);
            }
            value = valueConverter.toJavaValue(currentValue.getOrDefault(key, null), nestedValue);
        } else {
            value = (V) pair.getValue();
        }

        currentValue.put(key, value);
        return currentValue;
    }

    public void setKeyConverter(FieldConverter<String, K> keyConverter) {
        this.keyConverter = keyConverter;
    }

    public void setValueConverter(FieldConverter<String, V> valueConverter) {
        this.valueConverter = valueConverter;
    }
}

package science.aist.neo4j.reflective.converter;

import science.aist.neo4j.reflective.FieldConverter;
import science.aist.neo4j.reflective.converter.arrayprimitive.*;
import science.aist.seshat.Logger;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized class for providing DEFAULT converters.
 * All converters in this provider will ALWAYS be applied per default.
 * To use a different converter you can override with the @Converter annotaiton
 * To prevent a field from converting simply add @Converter with the converter field NOT SET.
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class ConverterProvider {

    public class ArrayRepresentative {
    }

    public static final Class ARRAY_CONVERTER_ID = ArrayRepresentative.class;

    /**
     * Map of all default registered converters
     */
    private static Map<Class, Class<? extends FieldConverter>> converters = init(null);

    /**
     * Init function provides converters. ALWAYS provides a default, even without spring context.
     * Can be overriden with Spring by supplying a converter map, or during runtime by using this function
     *
     * @param converterMap to be provided instead of the hardcoded defaults
     * @return default converter map
     */
    public static Map<Class, Class<? extends FieldConverter>> init(Map<Class, Class<? extends FieldConverter>> converterMap) {
        return init(converterMap, true);
    }

    /**
     * Init function provides converters. ALWAYS provides a default, even without spring context.
     * Can be overriden with Spring by supplying a converter map, or during runtime by using this function
     *
     * @param converterMap to be provided instead of the hardcoded defaults
     * @param defaults     if the default converter shall still be added (will NOT override anything in converterMap)
     * @return default converter map
     */
    public static Map<Class, Class<? extends FieldConverter>> init(Map<Class, Class<? extends FieldConverter>> converterMap, boolean defaults) {
        Map<Class, Class<? extends FieldConverter>> defaultConverters;

        // check if we can add values
        if (converterMap != null) {
            defaultConverters = converterMap;
        } else {
            defaultConverters = new HashMap<>();
        }

        // add defaults
        if (defaults) {
            // simple types
            defaultConverters.putIfAbsent(int.class, IntConverter.class);
            defaultConverters.putIfAbsent(Integer.class, IntConverter.class);
            defaultConverters.putIfAbsent(Class.class, ClassConverter.class);

            // collection types
            defaultConverters.putIfAbsent(ARRAY_CONVERTER_ID, ArrayConverterImpl.class);
            defaultConverters.putIfAbsent(byte[].class, ByteArrayConverterImpl.class);
            defaultConverters.putIfAbsent(char[].class, CharArrayConverterImpl.class);
            defaultConverters.putIfAbsent(short[].class, ShortArrayConverterImpl.class);
            defaultConverters.putIfAbsent(int[].class, IntArrayConverterImpl.class);
            defaultConverters.putIfAbsent(long[].class, LongArrayConverterImpl.class);
            defaultConverters.putIfAbsent(float[].class, FloatArrayConverterImpl.class);
            defaultConverters.putIfAbsent(double[].class, DoubleArrayConverterImpl.class);
            defaultConverters.putIfAbsent(boolean[].class, BooleanArrayConverterImpl.class);
            defaultConverters.putIfAbsent(Map.class, MapConverterImpl.class);
            defaultConverters.putIfAbsent(Collection.class, CollectionConverterImpl.class);
        }

        converters = defaultConverters;
        return defaultConverters;
    }

    /**
     * Returns converter for class
     *
     * @param fieldClass         class that actually needs to be converted
     * @param type               generic type (optional) of field
     * @param converterOverrides overrides for (sub) classes that shall be returned in the converter hierarchy instead
     * @return converter (with possible sub-converters)
     */
    public static FieldConverter getConverter(Class fieldClass, Type type, Map<Class, Class<? extends FieldConverter>> converterOverrides) {
        Class clazz = fieldClass;

        if (clazz.isArray()) {
            if (clazz.getComponentType().isPrimitive() && converters.containsKey(clazz)) {
                // inject the class.type which for primitives is always primitive[] (component type is just primitive)
                try {
                    return converters.get(clazz).getConstructor(null).newInstance();
                } catch (Exception e) {
                    Logger.getInstance().error("Converter could not be initialized", e);
                    return null;
                }
            } else if (converters.containsKey(ARRAY_CONVERTER_ID)) {
                FieldConverter subConverter = getConverter(clazz.getComponentType(), type instanceof GenericArrayType ? ((GenericArrayType) type).getGenericComponentType() : null, converterOverrides);
                ArrayConverter converter = (ArrayConverter) getConverter(ARRAY_CONVERTER_ID, type, converterOverrides);
                converter.setTypeConverter(subConverter);
                return converter;
            } else {
                return null;
            }
        }

        while (clazz != null) {
            Class<? extends FieldConverter> converter = converterOverrides.getOrDefault(clazz, null);
            if (converter == null) {
                converter = converters.getOrDefault(clazz, null);
            }

            if (converter != null) {
                try {
                    FieldConverter fieldConverter = converter.getConstructor(null).newInstance();
                    if (fieldConverter instanceof MapConverter) {
                        // load key and value converters
                        MapConverter mapConverter = (MapConverter) fieldConverter;
                        ParameterizedType pT = (ParameterizedType) type;
                        mapConverter.setKeyConverter(loadTyped(pT.getActualTypeArguments()[0], converterOverrides));
                        mapConverter.setValueConverter(loadTyped(pT.getActualTypeArguments()[1], converterOverrides));
                    } else if (fieldConverter instanceof CollectionConverter) {
                        // load type converter
                        CollectionConverter collectionConverter = (CollectionConverter) fieldConverter;
                        ParameterizedType pT = (ParameterizedType) type;
                        collectionConverter.setTypeConverter(loadTyped(pT.getActualTypeArguments()[0], converterOverrides));
                        collectionConverter.determineInitialization((Class) pT.getRawType());
                    }
                    return fieldConverter;
                } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    Logger.getInstance().error("Converter could not be initialized", e);
                }
            }
            clazz = clazz.getSuperclass();
        }

        // if we didn't find the real classes, re-attempt with interfaces
        for (Class interF : fieldClass.getInterfaces()) {
            FieldConverter converter = getConverter(interF, type, converterOverrides);
            if (converter != null) {
                return converter;
            }
        }
        return null;
    }

    private static FieldConverter loadTyped(Type type, Map<Class, Class<? extends FieldConverter>> converterOverrides) {
        return type instanceof ParameterizedType ? getConverter((Class) ((ParameterizedType) type).getRawType(), type, converterOverrides) :
            (type instanceof GenericArrayType ? getConverter(ARRAY_CONVERTER_ID, ((GenericArrayType) type).getGenericComponentType(), converterOverrides) :
                getConverter((Class) type, null, converterOverrides));
    }

}

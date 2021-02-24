package science.aist.neo4j.reflective;

import science.aist.neo4j.annotations.Converter;
import science.aist.neo4j.annotations.Converters;
import science.aist.neo4j.reflective.converter.ConverterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * RelationshipInformation handles the metadata for the {@link ReflectiveNeo4JNodeRepositoryImpl}
 * It contains specific information on fields in a node
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class FieldInformation {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Name of field
     */
    protected String name;

    /**
     * The field itself
     */
    protected Field field;

    /**
     * Getter for field
     */
    protected Method getter;

    /**
     * Setter for field
     */
    protected Method setter;

    /**
     * If the field is a map type
     */
    protected boolean map;

    protected FieldConverter converter;

    /**
     * Type that is stored in this field
     */
    protected Class fieldClass;

    public FieldInformation(String name, Field field, Method getter, Method setter, Class genericType) {
        if (genericType == null) {
            this.fieldClass = field.getType();
        } else {
            this.fieldClass = genericType;
        }

        this.name = name;
        this.field = field;
        this.field.setAccessible(true);
        this.getter = getter;
        this.setter = setter;
        this.map = fieldClass.equals(Map.class);

        Map<Class, Class<? extends FieldConverter>> converters = new HashMap<>();
        if (this.field.isAnnotationPresent(Converter.class) || this.field.isAnnotationPresent(Converters.class)) {
            for (Converter converter : field.getAnnotationsByType(Converter.class)) {
                converters.put(converter.overrides() == void.class ? fieldClass : converter.overrides(), converter.converter());
            }
        }
        converter = ConverterProvider.getConverter(fieldClass, field.getGenericType(), converters);
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the field value as database value!
     *
     * @param object to be returned from
     * @return value the field has as is (can be null)
     */
    public Object get(Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns the field value as database value!
     *
     * @param object to be returned from
     * @param map    that will be stored in DB
     */
    public void prepareForDb(Object object, Map<String, Object> map) {
        try {
            Object o = field.get(object);
            if (converter != null) {
                converter.mapForDb(this.getName(), o, map);
            } else {
                map.put(this.getName(), o);
            }
        } catch (IllegalAccessException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * Sets the field to a given value in an object.
     *
     * @param object whose field should be set
     * @param value  that the field should be set to -&gt; is a database Value!
     */
    public void set(Object object, Object value) {
        if (converter != null) {
            value = converter.toJavaValue(this.get(object), value);
        }
        try {
            field.set(object, value);
        } catch (Exception e) {
            logger.error("Set call in Reflective Repository should never fail");
            throw new RuntimeException("SET failed in FieldInformation", e);
        }
    }

    public boolean isMap() {
        return map;
    }

    public Field getField() {
        return field;
    }
}


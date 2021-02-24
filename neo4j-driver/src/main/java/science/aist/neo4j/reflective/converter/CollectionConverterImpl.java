package science.aist.neo4j.reflective.converter;

import science.aist.neo4j.reflective.FieldConverter;
import science.aist.neo4j.util.Pair;
import science.aist.seshat.Logger;

import javax.management.AttributeList;
import javax.management.relation.RoleList;
import javax.management.relation.RoleUnresolvedList;
import javax.print.attribute.standard.JobStateReasons;
import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextSupport;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.*;

/**
 * Default implementation of list converter
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class CollectionConverterImpl<T> implements CollectionConverter<T> {

    /**
     * Converter for type contained in list. If null is assumed to be string.
     */
    private FieldConverter<String, T> typeConverter;

    private Class<? extends Collection> initializerClass;

    private Logger logger = Logger.getInstance();

    @Override
    public void mapForDb(String name, Collection<T> value, Map<String, Object> map) {
        if (value == null) {
            return;
        }

        String format = "%1$" + String.valueOf(value.size()).length() + "s";

        int i = 0;
        for (T t : value) {
            String fixedOrderName = name + "." + String.format(format, Integer.toString(i)).replace(" ", "0");
            if (typeConverter != null) {
                typeConverter.mapForDb(fixedOrderName, t, map);
            } else {
                map.put(fixedOrderName, t);
            }
            i++;
        }
    }

    @Override
    public Collection<T> toJavaValue(Collection<T> currentValue, Object newValue) {
        // load indexes
        Pair<String, Object> pair = (Pair<String, Object>) newValue;

        // initialize collection if necessary
        if (currentValue == null) {
            try {
                currentValue = initializerClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Could not instantiate " + initializerClass.getName(), e);
            }
        }

        T value;
        if (pair.getKey() != null) {

            // load original position
            String keyStr = pair.getKey();
            int subkeyIndex = keyStr.indexOf('.');
            int key = Integer.valueOf(keyStr.substring(0, subkeyIndex > 0 ? subkeyIndex : keyStr.length()));

            // load value
            if (typeConverter != null) {
                Object nestedValue = pair.getValue();
                if (subkeyIndex > 0) {
                    nestedValue = new Pair<>(keyStr.substring(subkeyIndex + 1), nestedValue);
                }
                T nestedCurrent = currentValue.size() > key ? getAtPosition(currentValue, key) : null;
                value = typeConverter.toJavaValue(nestedCurrent, nestedValue);
                if (nestedCurrent != null) {
                    // if nested collection or value was modified, no need to modify here.
                    return currentValue;
                }
            } else {
                value = (T) pair.getValue();
            }
        } else {
            value = (T) pair.getValue();
        }

        currentValue.add(value); // TODO #20 we can't guarantee the ORDER

        return currentValue;
    }

    private T getAtPosition(Collection<T> currentValue, int key) {
        Iterator<T> iterator = currentValue.iterator();
        for (int counter = 0; counter < key; counter++)
            iterator.next();
        return iterator.next();
    }

    @Override
    public void setTypeConverter(FieldConverter<String, T> typeConverter) {
        this.typeConverter = typeConverter;
    }

    @Override
    public void determineInitialization(Class type) {
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            // deal with non-concrete classes by moving through the Collections according to likelyhood
            if (type.isAssignableFrom(LinkedList.class)) {
                initializerClass = LinkedList.class;
            } else if (type.isAssignableFrom(HashSet.class)) {
                initializerClass = HashSet.class;
            } else if (type.isAssignableFrom(Stack.class)) {
                initializerClass = Stack.class;
            } else if (type.isAssignableFrom(Vector.class)) {
                initializerClass = Vector.class;
            } else if (type.isAssignableFrom(PriorityQueue.class)) {
                initializerClass = PriorityQueue.class;
            } else if (type.isAssignableFrom(ArrayDeque.class)) {
                initializerClass = ArrayDeque.class;
            } else if (type.isAssignableFrom(ArrayList.class)) {
                initializerClass = ArrayList.class;
            } else if (type.isAssignableFrom(DelayQueue.class)) {
                initializerClass = DelayQueue.class;
            } else if (type.isAssignableFrom(TreeSet.class)) {
                initializerClass = TreeSet.class;
            } else if (type.isAssignableFrom(JobStateReasons.class)) {
                initializerClass = JobStateReasons.class;
            } else if (type.isAssignableFrom(LinkedBlockingDeque.class)) {
                initializerClass = LinkedBlockingDeque.class;
            } else if (type.isAssignableFrom(LinkedBlockingQueue.class)) {
                initializerClass = LinkedBlockingQueue.class;
            } else if (type.isAssignableFrom(LinkedHashSet.class)) {
                initializerClass = LinkedHashSet.class;
            } else if (type.isAssignableFrom(AttributeList.class)) {
                initializerClass = AttributeList.class;
            } else if (type.isAssignableFrom(CopyOnWriteArrayList.class)) {
                initializerClass = CopyOnWriteArrayList.class;
            } else if (type.isAssignableFrom(CopyOnWriteArraySet.class)) {
                initializerClass = CopyOnWriteArraySet.class;
            } else if (type.isAssignableFrom(PriorityBlockingQueue.class)) {
                initializerClass = PriorityBlockingQueue.class;
            } else if (type.isAssignableFrom(RoleList.class)) {
                initializerClass = RoleList.class;
            } else if (type.isAssignableFrom(RoleUnresolvedList.class)) {
                initializerClass = RoleUnresolvedList.class;
            } else if (type.isAssignableFrom(ArrayBlockingQueue.class)) {
                initializerClass = ArrayBlockingQueue.class;
            } else if (type.isAssignableFrom(BeanContextServicesSupport.class)) {
                initializerClass = BeanContextServicesSupport.class;
            } else if (type.isAssignableFrom(BeanContextSupport.class)) {
                initializerClass = BeanContextSupport.class;
            } else if (type.isAssignableFrom(ConcurrentLinkedDeque.class)) {
                initializerClass = ConcurrentLinkedDeque.class;
            } else if (type.isAssignableFrom(ConcurrentLinkedQueue.class)) {
                initializerClass = ConcurrentLinkedQueue.class;
            } else if (type.isAssignableFrom(ConcurrentSkipListSet.class)) {
                initializerClass = ConcurrentSkipListSet.class;
            } else if (type.isAssignableFrom(LinkedTransferQueue.class)) {
                initializerClass = LinkedTransferQueue.class;
            } else if (type.isAssignableFrom(SynchronousQueue.class)) {
                initializerClass = SynchronousQueue.class;
            } else {
                throw new IllegalArgumentException("This collection class is unknown to the driver and it can't be initialized: " + type.getName());
            }
        } else {
            initializerClass = type;
        }
    }
}

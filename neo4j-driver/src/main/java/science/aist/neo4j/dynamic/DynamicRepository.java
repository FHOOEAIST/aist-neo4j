package science.aist.neo4j.dynamic;

import science.aist.neo4j.Neo4jRepository;
import science.aist.neo4j.reflective.ClassInformation;
import science.aist.neo4j.repository.AbstractNeo4JNodeRepositoyImpl;
import science.aist.neo4j.transaction.TransactionManager;
import science.aist.seshat.Logger;
import org.neo4j.driver.Result;
import org.neo4j.driver.Values;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;
import org.springframework.lang.NonNull;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * <p>Repository for saving a dynamic object tree</p>
 *
 * @author Christoph Praschl
 * @since 1.0
 */
public class DynamicRepository {
    private static Logger logger = Logger.getInstance(DynamicRepository.class);
    private Map<Class, AbstractNeo4JNodeRepositoyImpl> repositories;
    private TransactionManager transactionManager;

    public DynamicRepository(@NonNull Map<Class, AbstractNeo4JNodeRepositoyImpl> repositories, @NonNull TransactionManager transactionManager) {
        this.repositories = repositories;
        this.transactionManager = transactionManager;
    }

    /**
     * Saves dynamically typed object tree
     *
     * @param node root node to be saved
     * @param <T>  Type of node
     * @return node
     */
    public <T> T save(T node) {
        try (Transaction transaction = transactionManager.beginTransaction()) {
            Set<Object> alreadySaved = new HashSet<>();
            saveHelp(node, alreadySaved, null);
            transaction.commit();
            return node;
        }
    }

    /**
     * Dynamically loads the object tree with the node of the given id as root
     *
     * @param id    id of the root node
     * @param clazz type of the root node
     * @param <T>   type of the root node
     * @return The root node with loaded sub nodes
     */
    public <T> T load(Long id, Class<T> clazz) {
        return load(id, clazz, new ArrayList<>());
    }

    /**
     * Dynamically loads the object tree with the node of the given id as root
     *
     * @param id      id of the root node
     * @param clazz   type of the root node
     * @param classes classes of nodes which should not be loaded (if empty or null everything will be loaded)
     * @param <T>     type of the root node
     * @return The root node with loaded sub nodes
     */
    public <T> T load(Long id, Class<T> clazz, Class<?>... classes) {
        return load(id, clazz, Arrays.asList(classes));
    }

    /**
     * Dynamically loads the object tree with the node of the given id as root
     *
     * @param id      id of the root node
     * @param clazz   type of the root node
     * @param classes classes of nodes which should not be loaded (if empty or null everything will be loaded)
     * @param <T>     type of the root node
     * @return The root node with loaded sub nodes
     */
    public <T> T load(Long id, Class<T> clazz, List<Class<?>> classes) {
        return load(id, clazz, classes, false);
    }

    /**
     * Dynamically loads the object tree with the node of the given id as root
     *
     * @param id                id of the root node
     * @param clazz             type of the root node
     * @param classes           classes of nodes which should (not) be loaded (if empty or null everything will be loaded or nothing will be loaded)
     * @param classesForInclude if true classes parameter will be used as include list; otherwise as an exclude list
     * @param <T>               type of the root node
     * @return The root node with loaded sub nodes
     */
    public <T> T load(Long id, Class<T> clazz, List<Class<?>> classes, boolean classesForInclude) {
        return load(id, clazz, classes, classesForInclude, new HashMap<>());
    }

    /**
     * Dynamically loads the object tree with the node of the given id as root
     * Using the loadMapping parameter this method also allows to load elements with a given dynamic type.
     *
     * @param id                id of the root node
     * @param clazz             type of the root node
     * @param classes           classes of nodes which should (not) be loaded (if empty or null everything will be loaded or nothing will be loaded); behaviour depends on classesForInclude parameter
     * @param classesForInclude if true the classes parameter will be used as include list; otherwise as an exclude list
     * @param loadMapping       Map associating a base class which should be interpreted as another class. This means: Load class A with the repository of class B
     * @param <T>               type of the root node
     * @return The root node with loaded sub nodes
     */
    public <T> T load(Long id, Class<T> clazz, List<Class<?>> classes, boolean classesForInclude, Map<Class<?>, Class<?>> loadMapping) {
        // uniform parameter classesToExclude since isEmpty() and null are processed the same way
        List<Class<?>> classesToExcludeIntern = classes == null ? new ArrayList<>() : classes;
        Map<Class<?>, Class<?>> mapping = loadMapping == null ? new HashMap<>() : loadMapping;
        return loadHelp(id, clazz, classesToExcludeIntern, new HashMap<>(), classesForInclude, mapping);
    }

    @SuppressWarnings("unchecked")
    private <T> T loadHelp(Long id, Class<T> clazz, List<Class<?>> classes, HashMap<Long, Object> objectCache, boolean classesForInclude, Map<Class<?>, Class<?>> loadMapping) {
        // if object was already loaded use cached one
        if (objectCache.containsKey(id)) {
            return (T) objectCache.get(id);
        }

        T loadedElement = null;

        // get mapped class if defined
        Class<?> targetClass = loadMapping.getOrDefault(clazz, clazz);

        List<String> labels = this.getLabels(id);
        ClassInformation classInformation = ClassInformation.constructClassInformation(targetClass);
        List<String> classInformationLabels = Arrays.asList(classInformation.getNsLabels().split(":"));
        boolean canCast = labels.containsAll(classInformationLabels);

        if (canCast) {
            // get required repository for object, load and cache it
            AbstractNeo4JNodeRepositoyImpl abstractNeo4JRepository = repositories.get(targetClass);
            loadedElement = (T) abstractNeo4JRepository.findById(id);
        }

        // if element could not be loaded with given dynamic type retry it with the static type
        if (loadedElement == null && loadMapping.containsKey(clazz)) {
            AbstractNeo4JNodeRepositoyImpl abstractNeo4JNodeRepositoy = repositories.get(clazz);
            loadedElement = (T) abstractNeo4JNodeRepositoy.findById(id);
        }

        // if element could not be loaded with dynamic nor with static type throw an exception
        if (loadedElement == null) {
            throw new IllegalArgumentException("Could not load element with id " + id + " as " + targetClass.toString());
        }
        objectCache.put(id, loadedElement);


        // check all fields and also reload all relationship fields
        for (Field field : getAllFieldsOfClass(clazz)) {
            // If the field has no relationship annotation ignore it.
            if (field.getAnnotation(Relationship.class) == null) continue;

            // two scenarios:
            // 1) classesToExclude is actually used as an exclude list, then proceed, if the class in not contained in it
            // 2) classesToExcluded is used as an include list, then proceed, if the class is contained in it, or if the class
            //    is a collection class, because then the elements of the collection could be inside the list.
            if ((!classesForInclude && !classes.contains(field.getType()))
                || (classesForInclude && (classes.contains(field.getType()) || Collection.class.isAssignableFrom(field.getType())))) {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                try {
                    // extract field value
                    Object o = field.get(loadedElement);
                    if (o != null) {
                        Class<?> oClass = o.getClass();
                        // check if it is a n:m relationship or if it is a 1:1 relationship
                        if (Collection.class.isAssignableFrom(oClass)) {
                            Collection col = (Collection) o;
                            List tmp = new ArrayList();

                            // extract all elements of the collection
                            for (Object iterableElement : col) {
                                // with the first element of the n:m relationship check if it is an excluded class
                                if ((classes.contains(iterableElement.getClass()) && !classesForInclude) || (!classes.contains(iterableElement.getClass()) && classesForInclude)) {
                                    break;
                                }
                                Long oId = getId(iterableElement);
                                Object o1 = loadHelp(oId, iterableElement.getClass(), classes, objectCache, classesForInclude, loadMapping);
                                tmp.add(o1);
                            }

                            // clear the collection and re-add the elements
                            col.clear();
                            col.addAll(tmp);
                        } else {
                            Long oId = getId(o);
                            // load and reset the field value
                            field.set(loadedElement, loadHelp(oId, oClass, classes, objectCache, classesForInclude, loadMapping));
                        }
                    }
                } catch (IllegalAccessException e) {
                    logger.debug("Could not access field " + field.getName(), e);
                }

                field.setAccessible(accessible);
            }
        }


        return loadedElement;
    }

    public List<String> getLabels(long id) {
        return transactionManager.executeRead(tx -> {
            Result labelResult = tx.run("MATCH (n) WHERE id(n) = $id RETURN labels(n)", Values.parameters("id", id));
            if (labelResult.hasNext()) {
                return labelResult.next().get(0).asList(Value::asString);
            }
            return null;
        });
    }

    /**
     * Retrieves the value of the id field (by annotation or name) of a neo4j node class.
     *
     * @param o object containing the field
     * @return the value of the id field
     */
    private Long getId(Object o) {
        Field idField = null;
        for (Field field : getAllFieldsOfClass(o.getClass())) {
            Id annotation = field.getAnnotation(Id.class);
            if (annotation != null) {
                // if an id-annotated field is found this will always be preferred
                idField = field;
                break;
            } else if (field.getName().equalsIgnoreCase("id")) {
                idField = field;
            }
        }

        if (idField == null || !idField.getType().getSimpleName().equalsIgnoreCase("long")) {
            throw new IllegalStateException("Could not retrieve an id");
        }
        boolean accessible = idField.isAccessible();
        idField.setAccessible(true);
        Long o1 = null;
        try {
            o1 = (Long) idField.get(o);
        } catch (IllegalAccessException e) {
            logger.debug("Could not extract id field", e);
        }
        idField.setAccessible(accessible);

        return o1;
    }


    private List<Field> getAllFieldsOfClass(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            fields.addAll(getAllFieldsOfClass(superclass));
        }
        return fields;
    }

    private <T> void saveHelp(T node, Set<Object> alreadySaved, Class<?> staticFieldTypeOfNode) {
        if (!alreadySaved.contains(node)) {
            Neo4jRepository neo4jRepository = repositories.get(node.getClass());
            if (staticFieldTypeOfNode == null || !staticFieldTypeOfNode.equals(node.getClass())) {
                neo4jRepository.save(node);
                alreadySaved.add(node);
            }
            for (Field declaredField : getAllFields(node.getClass())) {
                boolean accessible = declaredField.isAccessible();
                declaredField.setAccessible(true);
                boolean isTransient = Modifier.isTransient(declaredField.getModifiers());
                if (declaredField.getAnnotation(Relationship.class) != null && declaredField.getAnnotation(Transient.class) == null && !isTransient) {
                    // check if collection type
                    if (Collection.class.isAssignableFrom(declaredField.getType())) {
                        this.<Collection<?>>getWithoutException(node, declaredField).ifPresent(collection ->
                            collection.forEach(o -> saveHelp(o, alreadySaved, null))
                        );
                        // check if map type
                    } else if (Map.class.isAssignableFrom(declaredField.getType())) {
                        this.<Map<?, ?>>getWithoutException(node, declaredField).ifPresent(map ->
                            map.values().forEach(o -> saveHelp(o, alreadySaved, null))
                        );
                        // check if array
                    } else if (declaredField.getType().isArray()) {
                        getWithoutException(node, declaredField).ifPresent(array -> {
                            Class arrayType = declaredField.getType().getComponentType();
                            for (int i = 0; i < Array.getLength(array); i++) {
                                saveHelp(Array.get(array, i), alreadySaved, arrayType);
                            }
                        });
                        // else normal node type
                    } else {
                        getWithoutException(node, declaredField).ifPresent(o -> saveHelp(o, alreadySaved, declaredField.getType()));
                    }
                }
                declaredField.setAccessible(accessible);
            }
        }
    }

    private void getAllFields(Class<?> c, List<Field> fields) {
        fields.addAll(Arrays.asList(c.getDeclaredFields()));
        if (c.getSuperclass() != null) {
            getAllFields(c.getSuperclass(), fields);
        }
    }

    private List<Field> getAllFields(Class<?> c) {
        List<Field> fields = new ArrayList<>();
        getAllFields(c, fields);
        return fields;
    }


    @SuppressWarnings("unchecked")
    private <T> Optional<T> getWithoutException(Object o, Field field) {
        try {
            return Optional.ofNullable((T) field.get(o));
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

}

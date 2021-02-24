package science.aist.neo4j.reflective;

import science.aist.neo4j.annotations.StaticField;
import science.aist.neo4j.namespace.annotations.ExtendedNode;
import science.aist.neo4j.namespace.annotations.ExtensionField;
import science.aist.neo4j.namespace.annotations.Namespace;
import science.aist.neo4j.util.Pair;
import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.types.Node;
import org.neo4j.ogm.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassInformation handles the metadata for the {@link ReflectiveNeo4JNodeRepositoryImpl}
 * It contains info on the id, fields and relationships that should be modelled in the database
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class ClassInformation {

    private static Logger logger = LoggerFactory.getLogger(ClassInformation.class);

    public static final String NEO4J_SYNC_FIELD = "_Neo4JSync";

    public static final String Neo4J_RELATION_FIELD = "_Neo4JRelationships";

    public static final String NEO4J_LABELS_FIELD = "_Neo4JLabels";

    public static final String NEO4J_EXTENSIONS_FIELD = "_Neo4JExtensions";

    public static final String EXTENSION_IDENTIFIER = "extension_";

    public static final String NEO4J_MAP_RELATIONSHIPS_FIELD = "_Neo4JMapRelationshipsField";

    public static final String SOURCE = "source";

    public static final String TARGET = "target";

    /**
     * Class info that was already loaded will not be rebuilt
     */
    private static Map<String, ClassInformation> classInfo = new HashMap<>();

    /**
     * Repository currently asking for Class Information. Used to auto-inject auto generated repositories.
     */
    public static ReflectiveNeo4JNodeRepositoryImpl requestingRepository;

    /**
     * Constructs the Class information from a given class including the GENERIC TYPES.
     * TODO #22: Currently this is only used for MapRelationship, we may be able to support generics in general.
     *
     * @param targetClass      Parameterized fieldClass of class.
     * @param relationshipName name of relationship, as this becomes part of the class identity.
     * @return Class information of targetClass
     */
    protected static ClassInformation constructClassInformation(ParameterizedType targetClass, String relationshipName) {
        String identity = relationshipName + targetClass.getTypeName();
        if (classInfo.containsKey(identity)) {
            return classInfo.get(identity);
        }
        try {
            return new ClassInformation((Class) targetClass.getRawType(), targetClass, false, relationshipName);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Constructs the Class Information from a given class. It will fail if the class does not contain a valid ID field
     * A Valid ID Field is either named "id" or contains the {@link Id} Annotation
     *
     * @param clazz Class to be analized
     * @return ClassInformation for Neo4J DB transformation
     */
    public static ClassInformation constructClassInformation(Class clazz) {
        return constructClassInformation(clazz, false);
    }

    /**
     * Constructs the Class Information from a given class. It will fail if the class does not contain a valid ID field
     * A Valid ID Field is either named "id" or contains the {@link Id} Annotation
     *
     * @param clazz          Class to be analized
     * @param namespaceaware -&gt; if it is constructed with or without namespacing, per default it is not
     * @return ClassInformation for Neo4J DB transformation
     */
    public static ClassInformation constructClassInformation(Class clazz, boolean namespaceaware) {
        if (classInfo.containsKey(clazz.getName())) {
            ClassInformation classInformation = classInfo.get(clazz.getName());
            if (classInformation.isNamespaceaware() == namespaceaware || !namespaceaware) {
                return classInformation;
            } else {
                // replace class information with !namespaceaware of previous information
                try {
                    ClassInformation classInformation1 = new ClassInformation(clazz, null, namespaceaware, null);
                    for (ClassInformation value : classInfo.values()) {
                        for (RelationshipInformation relationshipInformation : value.getRelationships().values()) {
                            if (relationshipInformation.getTargetClassInformation() == classInformation) {
                                relationshipInformation.setTargetClassInformation(classInformation1);
                            }
                        }
                    }
                    return classInformation1;
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(), e);
                    throw new IllegalStateException(e);
                }

            }
        }
        try {
            return new ClassInformation(clazz, null, namespaceaware, null);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Class that this info is for
     */
    protected Class<?> clazz;

    /**
     * Signature of class including the generic type names
     */
    protected String signature;

    /**
     * All labels of the class, incl. its own name on the first position
     */
    protected LinkedList<String> labels = new LinkedList<>();

    /**
     * All labels of the class with namespace added. Own name on first position
     */
    protected LinkedList<String> nsLabels = new LinkedList<>();

    /**
     * Namespace of the class
     */
    protected String namespace = "";

    /**
     * Id of the the class
     */
    protected FieldInformation id;

    /**
     * Fields in the db for this class (@Transient -&gt; not in db)
     */
    protected Map<String, FieldInformation> fields = new HashMap<>();

    /**
     * Relationships in db
     */
    protected Map<String, RelationshipInformation> relationships = new HashMap<>();

    /**
     * Neo4J Instance Type for this Class Information
     */
    protected Neo4JType type = Neo4JType.UNDEFINED;

    /**
     * if the class information will behave with or without namespaces. It WILL load the information regardless, but manages casting differently.
     */
    protected boolean namespaceaware;

    /**
     * Type of neo4J instance, Can be either a Node, or a Relationship. If Undefined is selected the type always defaults to Node
     *
     * @author Oliver Krauss .
     */
    public enum Neo4JType {
        NODE,
        RELATIONSHIP,
        UNDEFINED
    }

    private ClassInformation(Class<?> clazz, ParameterizedType genericType, boolean namespaceaware, String relationshipName) throws ClassNotFoundException {
        this.signature = relationshipName != null ? relationshipName : "";
        if (genericType != null) {
            this.signature += genericType.getTypeName();
        } else {
            this.signature += clazz.getName();
        }
        classInfo.put(this.signature, this);

        this.clazz = clazz;

        this.namespaceaware = namespaceaware;

        if (clazz != null) {
            this.namespace = clazz.getPackage().getName().replace(".", "_");
            if (clazz.getPackage().getAnnotation(Namespace.class) != null) {
                this.namespace = clazz.getPackage().getAnnotation(Namespace.class).ns();
            }
        }

        while (clazz != null) {
            String ns = clazz.getPackage().getName().replace(".", "_");
            if (clazz.getPackage().getAnnotation(Namespace.class) != null) {
                ns = clazz.getPackage().getAnnotation(Namespace.class).ns();
            }

            if (clazz.isAnnotationPresent(NodeEntity.class)) {
                if (type == Neo4JType.RELATIONSHIP) {
                    throw new IllegalStateException("Class can't be Node and Relationship at the same time");
                }
                type = Neo4JType.NODE;
                if (clazz.getAnnotation(NodeEntity.class).label().length() > 0) {
                    this.labels.addLast(clazz.getAnnotation(NodeEntity.class).label());
                    this.nsLabels.addLast(ns + "_" + clazz.getAnnotation(NodeEntity.class).label());
                } else {
                    this.labels.addLast(clazz.getSimpleName());
                    this.nsLabels.addLast(ns + "_" + clazz.getSimpleName());
                }
            }

            if (clazz.isAnnotationPresent(RelationshipEntity.class)) {
                if (type == Neo4JType.NODE) {
                    throw new IllegalStateException("Class can't be Node and Relationship at the same time");
                }
                if (type != Neo4JType.RELATIONSHIP) {
                    type = Neo4JType.RELATIONSHIP;
                    this.labels.addLast(clazz.getAnnotation(RelationshipEntity.class).type());
                    this.nsLabels.addLast(ns + "_" + clazz.getAnnotation(RelationshipEntity.class).type());
                }
            }


            // handle science.neo4j.nodes that have a different class hierarchy than the db
            if (clazz.isAnnotationPresent(ExtendedNode.class)) {
                String parentNodes = clazz.getAnnotation(ExtendedNode.class).parent();
                if (!parentNodes.isEmpty()) {
                    for (String parent : parentNodes.split(":")) {
                        this.labels.addLast(parent.substring(parent.lastIndexOf('_') + 1));
                        this.nsLabels.addLast(parent);
                    }
                }
            }

            for (Field declaredField : clazz.getDeclaredFields()) {
                loadField(genericType, declaredField, ns);
            }
            for (Field field : clazz.getFields()) {
                loadField(genericType, field, ns);
            }

            clazz = clazz.getSuperclass();
        }


        if (this.labels.isEmpty() || (this.clazz.isAnnotationPresent(ExtendedNode.class) && this.labels.size() == 1)) {
            this.labels.addFirst(this.clazz.getSimpleName());
            this.nsLabels.addFirst(namespace + "_" + this.clazz.getSimpleName());
        }

        // if no field had the @Id annotation default to id
        if (id == null) {
            if (namespaceaware) {
                id = fields.remove(fields.keySet().stream().filter(x -> x.contains("_id")).findFirst().orElseThrow(IllegalStateException::new));
            } else {
                id = fields.remove("id");
            }
        }

        // if type was not defined ever, we assume node as default
        if (type == Neo4JType.UNDEFINED) {
            type = Neo4JType.NODE;
        }

        // do the final checks to see if this is a valid node class
        if (id == null) {
            throw new IllegalStateException("This is not a valid node or relationship class, it has no ID field (annotation or name)");
        }

        // do the final checks to see if this is a valid relationship class
        if (type == Neo4JType.RELATIONSHIP) {
            if (!relationships.containsKey(SOURCE)) {
                throw new IllegalStateException("Relationship has no source");
            }
            if (!relationships.containsKey(TARGET)) {
                throw new IllegalStateException("Relationship has no target");
            }
        }
    }

    private void loadField(ParameterizedType genericType, Field field, String namespace) throws ClassNotFoundException {
        Class genericClass = null;
        if (genericType != null && field.getGenericType() instanceof TypeVariable) {
            // find actual fieldClass of the generic value
            String genericTypeName = ((TypeVariable) field.getGenericType()).getName();
            TypeVariable[] typeParameters = ((Class) genericType.getRawType()).getTypeParameters();
            for (int i = 0; i < typeParameters.length; i++) {
                if (typeParameters[i].getName().equals(genericTypeName)) {
                    genericClass = (Class) genericType.getActualTypeArguments()[i];
                    break;
                }
            }
        }

        // ignore transient fields
        if (field.isAnnotationPresent(Transient.class) || Modifier.isTransient(field.getModifiers())) {
            return;
        }

        String name = field.getName();
        if (namespaceaware) {
            name = namespace + "_" + name;

            if (field.isAnnotationPresent(ExtensionField.class)) {
                name = EXTENSION_IDENTIFIER + field.getAnnotation(ExtensionField.class).name();
            }
        }

        // ignore meta fields -> they usualls start with $ and are used in interception or bytecode recompilation frameworks.
        if (field.isSynthetic()) {
            return;
        }

        // ignore static fields unless they have the static-store annotation OR the relationship annotation
        if (Modifier.isStatic(field.getModifiers()) && !field.isAnnotationPresent(StaticField.class) && !field.isAnnotationPresent(Relationship.class)) {
            return;
        }


        Method getter = null;
        Method setter = null;
        try {
            setter = clazz.getMethod("set" + name.substring(0, 1).toUpperCase() + name.substring(1), field.getType());
        } catch (NoSuchMethodException e) {
            // don't care
        }
        try {
            getter = clazz.getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1));
        } catch (NoSuchMethodException e) {
            // don't care
        }

        FieldInformation information = new FieldInformation(name, field, getter, setter, genericClass);

        // identify as id field
        if (field.isAnnotationPresent(Id.class)) {
            id = information;
            return;
        }

        // identify as relationship
        if (field.isAnnotationPresent(Relationship.class)) {
            String annotationType = field.getAnnotation(Relationship.class).type();
            if (namespaceaware && !annotationType.isEmpty() && !annotationType.contains("_")) {
                annotationType = namespace + "_" + annotationType;
            }

            RelationshipInformation relationshipInformation;
            if (Map.class.isAssignableFrom(field.getType())) {
                // insert complex relationship
                relationshipInformation =
                    new MapRelationshipInformation(name, field, getter, setter, genericClass, annotationType, field.getAnnotation(Relationship.class).direction(), field.getGenericType());
            } else if (field.getType().isArray()) {
                relationshipInformation =
                    new ArrayRelationshipInformation(name, field, getter, setter, genericClass, annotationType, field.getAnnotation(Relationship.class).direction());

            } else {
                // default to regular relationship
                relationshipInformation =
                    new RelationshipInformation(name, field, getter, setter, genericClass, annotationType, field.getAnnotation(Relationship.class).direction(), field.getGenericType());
            }

            // fail on duplicate relationship
            checkForDuplicates(relationshipInformation.getType(), field);
            relationships.put(relationshipInformation.getType(), relationshipInformation);
            return;

        }

        // identify as source of relationship class
        if (field.isAnnotationPresent(StartNode.class)) {
            RelationshipInformation relationshipInformation =
                new RelationshipInformation(name, field, getter, setter, genericClass, SOURCE, "INCOMING", field.getGenericType());
            // fail on duplicate relationship
            checkForDuplicates(relationshipInformation.getType(), field);
            relationships.put(relationshipInformation.getType(), relationshipInformation);
            return;
        }

        // identify as target of relationship class
        if (field.isAnnotationPresent(EndNode.class)) {
            RelationshipInformation relationshipInformation =
                new RelationshipInformation(name, field, getter, setter, genericClass, TARGET, "OUTGOING", field.getGenericType());
            // fail on duplicate relationship
            checkForDuplicates(relationshipInformation.getType(), field);
            relationships.put(relationshipInformation.getType(), relationshipInformation);
            return;
        }

        if (fields.containsKey(name)) {
            checkForDuplicates(field, fields.get(name).getField());
        }
        fields.put(name, information);
    }

    /**
     * Check if a field is duplicated or not
     *
     * @param field to be checked
     * @return if the field is NOT duplicate
     * @oaram type          relationship-type that needs to be checked
     */
    private void checkForDuplicates(String type, Field field) {
        if (relationships.containsKey(type)) {
            checkForDuplicates(field, relationships.get(type).getField());
        }
    }

    /**
     * Check if a field is duplicated or not
     *
     * @param field         to be checked
     * @param existingField to be checked
     * @return if the field is NOT duplicate
     */
    private void checkForDuplicates(Field field, Field existingField) {
        if (!existingField.getName().equals(field.getName()) || !existingField.getDeclaringClass().getName().equals(field.getDeclaringClass().getName())) {
            throw new IllegalStateException("Field " + field.getDeclaringClass().getName() + "." + field.getName() + " clashes with " + existingField.getDeclaringClass().getName() + "." + existingField.getName());
        }
    }


    protected Object cast(org.neo4j.driver.types.Relationship value, Node source, Node target, Map<Long, Object> castNodes) {
        Long id = (value.id() * -1) - 1;
        if (castNodes != null && castNodes.containsKey(id)) {
            return castNodes.get(id);
        }
        List<org.neo4j.driver.types.Relationship> rels = new ArrayList<>();
        List<Node> nodes = new ArrayList<>();
        if (source != null) {
            rels.add(new InternalRelationship(Long.MIN_VALUE, id, source.id(), SOURCE));
            nodes.add(source);
        }
        if (target != null) {
            rels.add(new InternalRelationship(Long.MIN_VALUE, id, target.id(), TARGET));
            nodes.add(target);
        }
        return cast(id, value.asMap(), rels, nodes, castNodes);
    }

    private Object cast(org.neo4j.driver.types.Relationship rel, List<org.neo4j.driver.types.Relationship> relationships, List<Node> children, Map<Long, Object> nodes) {
        Long id = (rel.id() * -1) - 1;
        if (nodes != null && nodes.containsKey(id)) {
            return nodes.get(id);
        }
        relationships.add(new InternalRelationship(Long.MIN_VALUE, id, rel.startNodeId(), SOURCE));
        relationships.add(new InternalRelationship(Long.MIN_VALUE, id, rel.endNodeId(), TARGET));
        return cast(id, rel.asMap(), relationships, children, nodes);
    }


    protected Object cast(Node value, List<org.neo4j.driver.types.Relationship> relationships, List<Node> children, Map<Long, Object> nodes) {
        if (nodes != null && nodes.containsKey(value.id())) {
            return nodes.get(value.id());
        }
        Object cast = cast(value.id(), value.asMap(), relationships, children, nodes);
        if (namespaceaware) {
            // store labels from db in hidden field for meta-reading
            ArrayList<String> labels = new ArrayList<>();
            value.labels().forEach(labels::add);
            try {
                clazz.getDeclaredField(NEO4J_LABELS_FIELD).set(cast, labels);
            } catch (Exception e) {
                logger.error("Error - SET in Cast failed");
                throw new IllegalStateException(e);
            }
        }
        return cast;
    }

    /**
     * Casts a database node or relationship into a java object
     *
     * @param id            java object will be asigned to
     * @param value         map of values contained in the node
     * @param relationships relationships in the subtree
     * @param children      nodes not yet cast in the subtree
     * @param nodes         nodes already cast in the subtree
     * @return cast result
     */
    protected Object cast(Long id, Map<String, Object> value, List<org.neo4j.driver.types.Relationship> relationships, List<Node> children, Map<Long, Object> nodes) {
        try {
            // create node
            Object result = clazz.newInstance();

            // move node to cast-nodes -> Do NOT remove from children, as we may need this for namespace casting
            nodes.put(id, result);

            // prepare hidden field synchronization
            Map<String, Object> sync = namespaceaware ? (Map<String, Object>) clazz.getDeclaredField(NEO4J_SYNC_FIELD).get(result) : new HashMap<>();
            Map<String, Object> syncRelationships = namespaceaware ? (Map<String, Object>) clazz.getDeclaredField(Neo4J_RELATION_FIELD).get(result) : new HashMap<>();
            Map<String, Object> extensions = namespaceaware ? (Map<String, Object>) clazz.getDeclaredField(NEO4J_EXTENSIONS_FIELD).get(result) : new HashMap<>();


            // set Id
            long realId = id >= 0 ? id : ((id + 1) * -1);
            this.id.set(result, realId);

            // pre-sort fields before assigning so we can keep order of collections
            Set<String> keys = new TreeSet<>();
            keys.addAll(value.keySet());

            // Set fields
            keys.forEach(key -> {
                if (key.contains(".")) {
                    // special combinatory treatment
                    int nameIndex = key.indexOf('.');
                    String rootName = key.substring(0, nameIndex);
                    if (this.fields.containsKey(rootName)) {
                        this.fields.get(rootName).set(result, new Pair<>(key.substring(nameIndex + 1), value.get(key)));
                    } else {
                        Map<String, Object> storage = rootName.startsWith(EXTENSION_IDENTIFIER) ? extensions : sync;
                        if (!storage.containsKey(rootName)) {
                            storage.put(rootName, new HashMap<>());
                        }
                        ((Map) storage.get(rootName)).put(key.substring(nameIndex + 1), value.get(key));
                    }
                } else {
                    // default to field
                    if (fields.containsKey(key)) {
                        this.fields.get(key).set(result, value.get(key));
                    } else {
                        Map<String, Object> storage = key.startsWith(EXTENSION_IDENTIFIER) ? extensions : sync;
                        storage.put(key, value.get(key));
                    }
                }
            });

            // set relationships
            if (relationships != null && !relationships.isEmpty()) {
                // note we compare with "id" here, as "realId" may allow relationships to suddenly be the startnode of another relationship
                List<org.neo4j.driver.types.Relationship> currentRelationships = relationships.stream().filter(x -> x.startNodeId() == id).collect(Collectors.toList());
                // we remove all already processed relationships
                relationships.removeAll(currentRelationships);
                currentRelationships.forEach(rel -> {
                    RelationshipInformation ship = this.relationships.get(rel.type());
                    if (ship != null) {
                        Object target = null;
                        Node node = children.stream().filter(x -> x.id() == rel.endNodeId()).findFirst().orElse(null);
                        if (ship.getTargetClassInformation().getType().equals(Neo4JType.RELATIONSHIP)) {
                            // handle as relationship with values
                            target = ship.getTargetClassInformation().cast(rel, relationships, children, nodes);
                            // create the connection back to the source
                            ship.getTargetClassInformation().getRelationships().get(SOURCE).set(target, result);
                        } else {
                            // handle as regular relationship
                            target = ship.getTargetClassInformation().cast(node, relationships, children, nodes);
                        }
                        if (ship.isBulk()) {
                            ship.set(result, new Pair<>(null, target));
                        } else {
                            ship.set(result, target);
                        }
                    } else {
                        syncRelationships.put(rel.type(), new Pair<>(children.stream().filter(x -> x.id() == rel.endNodeId()).findFirst().orElse(null), rel));
                    }
                });
            }

            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * turns the object into a map of fields-&gt;values
     *
     * @param node to be turned into a map
     * @return map of all fields
     */
    public Map<String, Object> objectifyProperties(Object node) {
        Map<String, Object> map = new HashMap<>();
        for (FieldInformation field : this.fields.values()) {
            field.prepareForDb(node, map);
        }
        if (namespaceaware) {
            helpObjectifyMap(getAlternativeNamespaceFields(node), map);

            // ensure extensions are corretly set on write
            Map<String, Object> corretedExtensions = new HashMap<>();
            getExtensions(node).entrySet().forEach(x -> {
                String key = x.getKey().startsWith(EXTENSION_IDENTIFIER) ? x.getKey() : EXTENSION_IDENTIFIER + x.getKey();
                corretedExtensions.put(key, x.getValue());
            });

            helpObjectifyMap(corretedExtensions, map);
        }

        return map;
    }

    private void helpObjectifyMap(Map<String, Object> mapToObjectify, Map<String, Object> propertyMap) {
        if (mapToObjectify != null) {
            // de-map the maps
            mapToObjectify.entrySet().stream().filter(x -> x.getValue() instanceof Map).forEach(x -> ((Map<?, ?>) x.getValue()).forEach((key, value) -> propertyMap.put(x.getKey() + "." + key, value)));
            // add non-map fields
            mapToObjectify.entrySet().stream().filter(x -> !(x.getValue() instanceof Map)).forEach(x -> propertyMap.put(x.getKey(), x.getValue()));
        }
    }

    /**
     * Provides a map of fields that alternative namespaces used in the given node.
     * If the system doesn't consider namespaces, or no alternative namespaces exist yet NULL is returned
     *
     * @param node for which namepsace fields should be retrieved
     * @return Map with all alternative fields or null
     * Please note that the return map can also contain maps!
     * <p>
     * Warning: This is NOT READ ONLY. Changes WILL be stored in the DB.
     */
    public Map<String, Object> getAlternativeNamespaceFields(Object node) {
        try {
            return namespaceaware ? (Map<String, Object>) clazz.getDeclaredField(NEO4J_SYNC_FIELD).get(node) : null;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the database labels from the given node
     *
     * @param node to be checked
     * @return database labels or the labels the node will receive when stored into the db.
     * <p>
     * Warning: this is READ ONLY. No changes will be stored in the DB.
     */
    public List<String> getLabels(Object node) {
        try {
            return (List<String>) clazz.getDeclaredField(NEO4J_LABELS_FIELD).get(node);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.warn(e.getMessage(), e);
        }
        return namespaceaware ? this.nsLabels : this.labels;
    }


    /**
     * Provides a map of relationships that alternative namespaces used in the given node.
     * If the system doesn't consider namespaces, or no alternative namespaces exist yet NULL is returned
     *
     * @param node for which relationships should be retrieved
     * @return Map with all alternative relationships or null (The pair contains {@code <TARGETNODE, RELATIONSHIP>})
     * Please note that the return map can also contain maps!
     * <p>
     * Warning: this is READ ONLY. No changes will be stored in the DB.
     */
    public Map<String, Pair<Node, org.neo4j.driver.types.Relationship>> getAlternativeRelationshipsInNamespaces(Object node) {
        try {
            return namespaceaware ? (Map<String, Pair<Node, org.neo4j.driver.types.Relationship>>) clazz.getDeclaredField(Neo4J_RELATION_FIELD).get(node) : null;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    public Class getClazz() {
        return clazz;
    }

    public String getName() {
        return labels.getFirst();
    }

    public String getLabels() {
        return String.join(":", labels);
    }

    public FieldInformation getId() {
        return id;
    }

    public Map<String, FieldInformation> getFields() {
        return fields;
    }

    public Map<String, RelationshipInformation> getRelationships() {
        return relationships;
    }

    public Neo4JType getType() {
        return type;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getNsName() {
        return nsLabels.getFirst();
    }

    public String getNsLabels() {
        return String.join(":", nsLabels);
    }

    /**
     * gets value of field {@link ClassInformation#namespaceaware}
     *
     * @return value of field namespaceaware
     * @see ClassInformation#namespaceaware
     */
    public boolean isNamespaceaware() {
        return namespaceaware;
    }

    /**
     * Casts a Neo4J database object to the required class
     *
     * @param o   object to be cast
     * @param c   class to cast to
     * @param <T> Type = class given as c
     * @return object now of class c
     */
    public static <T> T castToClass(Object o, Class<T> c) {
        T result = null;

        // TODO #26 We should check for correct label BUT this can only happen after #12 has been fixed

        try {
            // load class information with namespaceaware, as false should not support polymorphism
            ClassInformation targetInfo = ClassInformation.constructClassInformation(c, true); // TODO #27: This makes me think that we should set Namespaceaware as a global for the entire driver, instead of allowing it per class
            ClassInformation sourceInfo = ClassInformation.constructClassInformation(o.getClass(), true);

            final T cast = c.newInstance();

            Map<String, Object> extensions = sourceInfo.getExtensions(o);
            Map<String, Object> sync = sourceInfo.getAlternativeNamespaceFields(o);

            // sync maps of what we need to move over to the new class after setting all EXISTING fields;
            HashMap<String, FieldInformation> missingSourceSync = new HashMap<>(sourceInfo.fields);
            HashMap<String, Object> missingExtensionSync = new HashMap<>(extensions);
            HashMap<String, Object> missingSync = new HashMap<>(sync);

            targetInfo.getFields().entrySet().forEach(x -> {
                FieldInformation f = sourceInfo.getFields().get(x.getKey());
                if (f != null) {
                    missingSourceSync.remove(x.getKey());
                    // load from existing object
                    try {
                        x.getValue().getField().set(cast, f.get(o));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    // load from hidden
                    Map<String, Object> storage = x.getKey().startsWith(EXTENSION_IDENTIFIER) ? extensions : sync;
                    if (x.getKey().startsWith(EXTENSION_IDENTIFIER)) {
                        missingExtensionSync.remove(x.getKey());
                    } else {
                        missingSync.remove(x.getKey());
                    }
                    x.getValue().set(cast, storage.get(x.getKey()));
                }
            });

            // sync the missed fields
            targetInfo.getAlternativeNamespaceFields(cast).putAll(missingSync);
            targetInfo.getExtensions(cast).putAll(missingExtensionSync);
            missingSourceSync.entrySet().forEach(x -> {
                if (x.getKey().startsWith(EXTENSION_IDENTIFIER)) {
                    targetInfo.getExtensions(cast).put(x.getKey(), x.getValue().get(o));
                } else {
                    targetInfo.getAlternativeNamespaceFields(cast).put(x.getKey(), x.getValue().get(o));
                }
            });

            targetInfo.getRelationships().entrySet().forEach(x -> {
                RelationshipInformation r = sourceInfo.getRelationships().get(x.getKey());
                if (r != null) {
                    try {
                        x.getValue().getField().set(cast, r.get(o));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    r = x.getValue();
                    Pair<Node, org.neo4j.driver.types.Relationship> pair = sourceInfo.getAlternativeRelationshipsInNamespaces(o).get(x.getKey());
                    if (pair != null) {
                        Object target;
                        if (r.getTargetClassInformation().getType().equals(Neo4JType.RELATIONSHIP)) {
                            // handle as relationship with values
                            target = r.getTargetClassInformation().cast(pair.getValue(), null, pair.getKey(), new HashMap<>());
                            // create the connection back to the source
                            r.getTargetClassInformation().getRelationships().get(SOURCE).set(target, cast);
                        } else {
                            // handle as regular relationship
                            target = r.getTargetClassInformation().cast(pair.getKey(), null, null, new HashMap<>());
                        }
                        if (r.isBulk()) {
                            r.set(cast, new Pair<>(null, target));
                        } else {
                            r.set(cast, target);
                        }
                    }
                }
            });


            result = cast;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Returns all extensions provided in the object
     *
     * @param node object where extensions should be accessed
     * @return all extensions (map can be modified!)
     */
    public Map<String, Object> getExtensions(Object node) {
        try {
            return (Map<String, Object>) clazz.getDeclaredField(NEO4J_EXTENSIONS_FIELD).get(node);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }


    /**
     * Returns the extension of name or null if it doesnt exist
     *
     * @param node object that the extension should be loaded out of
     * @param name of extension that should be returned
     * @return extension or null (object CAN NOT be modified)
     */
    public Object getExtension(Object node, String name) {
        if (!name.startsWith(EXTENSION_IDENTIFIER)) {
            name = EXTENSION_IDENTIFIER + name;
        }
        try {
            Map<String, Object> extensions = (Map<String, Object>) clazz.getDeclaredField(NEO4J_EXTENSIONS_FIELD).get(node);
            return extensions.get(name);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Adds the extension of name to the object o.
     * Warning: If the extension already exists it will be overwritten!
     *
     * @param node      object that the extension should be added to
     * @param name      name of extension to be set
     * @param extension to be added (can only be primitive!)
     */
    public void setExtension(Object node, String name, Object extension) {
        try {
            Map<String, Object> extensions = (Map<String, Object>) clazz.getDeclaredField(NEO4J_EXTENSIONS_FIELD).get(node);
            extensions.put(name, extension);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * Adds the extension of name to the object o.
     * Will only write extension if it does NOT exist
     *
     * @param node      object that the extension should be added to
     * @param name      name of extension to be set
     * @param extension to be added (can only be primitive!)
     * @return true if write was successful, false if the extension already existed
     */
    public boolean setExtensionSafe(Object node, String name, Object extension) {
        try {
            Map<String, Object> extensions = (Map<String, Object>) clazz.getDeclaredField(NEO4J_EXTENSIONS_FIELD).get(node);
            if (extensions.containsKey(name)) {
                return false;
            }
            extensions.put(name, extension);
            return true;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.warn(e.getMessage(), e);
        }
        return false;
    }
}


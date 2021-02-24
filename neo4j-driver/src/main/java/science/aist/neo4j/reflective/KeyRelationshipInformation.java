package science.aist.neo4j.reflective;

import science.aist.neo4j.repository.AbstractNeo4JRepository;
import science.aist.neo4j.transaction.TransactionManager;
import science.aist.neo4j.util.Pair;
import org.neo4j.ogm.annotation.NodeEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles the metadata for the {@link ReflectiveNeo4JNodeRepositoryImpl}
 * It contains information on a Map relationship {@code Map<Key, NODE>} where the Key will be moved into a {@link MapRelationship}.
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public abstract class KeyRelationshipInformation extends RelationshipInformation {

    /**
     * Class of the start node (also the one containing the map)
     */
    protected Class startNodeClass;

    /**
     * Class of the end node
     */
    protected Class endNodeClass;

    /**
     * Class of the key in the map
     */
    protected Class keyClass;

    /**
     * Field in StartNode that lets us sync relationship ids to keys
     */
    protected Field relationshipSyncField;

    /**
     * Transaction manager to be assigned to the auto-generated relationship repository
     */
    protected static List<TransactionManager> managers = new ArrayList<>();

    protected boolean inverted = false;

    public KeyRelationshipInformation(String name, Field field, Method getter, Method setter, Class genericClass, String type, String direction, Class startNodeClass, Class endNodeClass, Class keyClass) {
        super(name, field, getter, setter, genericClass, type, direction);

        if (keyClass.isAnnotationPresent(NodeEntity.class)) {
            // for the map relationship to work out we need a start (always the declaring class)
            // an end (always a NODE ENTITY)
            // a key that is stored as a relationship value (only must be unique if the key is also the Map<KEY, ...
            // thus if keyClass is a node, we must assume the relationship needs to be flipped
            inverted = true;
            Class clazz = keyClass;
            keyClass = endNodeClass;
            endNodeClass = clazz;
        }


        // Map relationship is always bulk
        this.bulk = true;

        // load classes for generic typing
        this.startNodeClass = startNodeClass;
        this.endNodeClass = endNodeClass;
        this.keyClass = keyClass;

        // construct generic type information
        ParameterizedType targetClass = ParameterizedTypeImpl.make(MapRelationship.class, new Class[]{startNodeClass, endNodeClass, keyClass}, null);
        this.targetClassInformation = ClassInformation.constructClassInformation(targetClass, this.type);

        // replace incorrect type labels
        LinkedList<String> labels = new LinkedList<>();
        this.targetClassInformation.labels.forEach(x -> {
            labels.add(x.replace("MAP_REL", this.type));
        });
        this.targetClassInformation.labels = labels;

        // replace incorrect namespace labels
        LinkedList<String> nsLabels = new LinkedList<>();
        this.targetClassInformation.nsLabels.forEach(x -> {
            nsLabels.add(x.replace("MAP_REL", this.type));
        });
        this.targetClassInformation.nsLabels = nsLabels;

        try {
            relationshipSyncField = startNodeClass.getDeclaredField(ClassInformation.NEO4J_MAP_RELATIONSHIPS_FIELD);
        } catch (NoSuchFieldException e) {
            logger.error("Preprocessor did not add required field ", e);
            throw new RuntimeException("Preprocessor did not add required field");
        }

        // construct repository to use the relationship information
        Class finalEndNodeClass = endNodeClass;
        managers.forEach(manager -> {
            ReflectiveNeo4JRelationshipRepositoryImpl generatedRepository = new ReflectiveNeo4JRelationshipRepositoryImpl(manager, targetClass, this.type);
            generatedRepository.sourceRepository = AbstractNeo4JRepository.getProvidedRepository(manager, startNodeClass.getName());
            generatedRepository.targetRepository = AbstractNeo4JRepository.getProvidedRepository(manager, finalEndNodeClass.getName());
            AbstractNeo4JRepository.provideRepository(manager, targetClass.getTypeName(), generatedRepository);
        });
    }

    protected HashMap<Object, Long> getSync(Object object) {
        HashMap<Object, Long> sync = null;
        try {
            HashMap<Object, HashMap<Object, Long>> syncMap = (HashMap) relationshipSyncField.get(object);
            if (syncMap != null) {
                sync = syncMap.getOrDefault(this, null);
            }
        } catch (IllegalAccessException e) {
            logger.warn("This should not ever happen", e);
        }
        return sync == null ? new HashMap() : sync;
    }

    @Override
    public void set(Object object, Object value) {
        if (value == null) {
            return;
        }
        MapRelationship relationship = (MapRelationship) ((Pair) value).getValue();
        if (inverted) {
            Object key = relationship.key;
            relationship.key = relationship.end;
            relationship.end = key;
        }

        // synchronize the relationship id so we can update it on GET
        try {
            HashMap<Object, HashMap<Object, Long>> syncMap = (HashMap) relationshipSyncField.get(object);
            if (syncMap == null) {
                syncMap = new HashMap<>();
                relationshipSyncField.set(object, syncMap);
            }
            HashMap<Object, Long> sync = syncMap.getOrDefault(this, null);
            if (sync == null) {
                sync = new HashMap<>();
                syncMap.put(this, sync);
            }
            sync.put(relationship.key, relationship.getId());
        } catch (IllegalAccessException e) {
            logger.warn("This should not ever happen", e);
        }


        if (converter != null) {
            value = converter.toJavaValue(super.get(object), new Pair<>(relationship.key, relationship.end));
        }
        try {
            field.set(object, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Note: this OR the addManager is required!
     * @param managers sets transaction managers to be used
     */
    public static void setManagers(List<TransactionManager> managers) {
        KeyRelationshipInformation.managers = managers;
    }

    /**
     * Note: this OR the setManagers is required!
     * @param manager used to handle transactions
     */
    public static void addManager(TransactionManager manager) {
        managers.add(manager);
    }
}

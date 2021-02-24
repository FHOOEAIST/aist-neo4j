package science.aist.neo4j.reflective;

import science.aist.neo4j.repository.AbstractNeo4JNodeRepositoyImpl;
import science.aist.neo4j.repository.AbstractNeo4JRepository;
import science.aist.neo4j.transaction.TransactionManager;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link AbstractNeo4JNodeRepositoyImpl} using java reflection
 * TODO #8 compile repository classes to improve performance
 *
 * @param <S> Type of node
 * @author Oliver Krauss
 * @since 1.0
 */
public class ReflectiveNeo4JNodeRepositoryImpl<S> extends AbstractNeo4JNodeRepositoyImpl<S> {

    /**
     * Class that this repository is managing
     */
    protected Class<S> clazz;

    /**
     * Metadata for storage purposes
     */
    protected ClassInformation info;

    /**
     * Repositories for relationships
     */
    protected Map<Class, AbstractNeo4JRepository> repositories;

    /**
     * Internal constructor for pushing through the manager to the top level
     * @param manager used to handle transactions
     */
    protected ReflectiveNeo4JNodeRepositoryImpl(TransactionManager manager) {
        super(manager);
    }

    /**
     * Generates the repository by a Generic Type
     *
     * @param manager used to handle transactions
     * @param type type of the handled class
     */
    public ReflectiveNeo4JNodeRepositoryImpl(TransactionManager manager, ParameterizedType type) {
        this(manager);
        provideRepository(this.getTransactionManager(), type.getTypeName(), this);
        this.clazz = (Class<S>) type.getRawType();
        this.info = ClassInformation.constructClassInformation(type, null);
        this.init(info.getName(), info.getLabels());
    }

    public ReflectiveNeo4JNodeRepositoryImpl(TransactionManager manager, Class<S> clazz) {
        this(manager);
        provideRepository(this.getTransactionManager(), clazz.getName(), this);
        try {
            this.clazz = clazz;
            this.info = ClassInformation.constructClassInformation(clazz);
            this.init(info.getName(), info.getLabels());
        } catch (Exception e) {
            logger.error("Initialization of repository failed", e);
            throw e;
        }
    }

    @Override
    protected S cast(Value value, Value rels, Value nodes) {
        List<Relationship> relationships = new ArrayList<>();
        List<Node> children = new ArrayList<>();

        // for self references we must add ourselves:
        children.add(value.asNode());

        if (rels != null && !rels.isNull()) {
            rels.values().forEach(x -> {
                relationships.add(x.asRelationship());
            });
        }
        if (nodes != null && !nodes.isNull()) {
            nodes.values().forEach(x -> children.add(x.asNode()));
        }

        return (S) info.cast(value.asNode(), relationships, children, new HashMap<>());
    }

    protected AbstractNeo4JRepository findRepository(RelationshipInformation information) {
        if (repositories.containsKey(information.getTargetClassInformation().clazz)) {
            return repositories.get(information.getTargetClassInformation().clazz);
        } else {
            return AbstractNeo4JRepository.getProvidedRepository(this.getTransactionManager(), information.getTargetClassInformation().signature);
        }
    }

    @Override
    protected S setId(S node, Long id) {
        this.info.id.set(node, id);
        return node;
    }

    @Override
    protected Long getId(S node) {
        return (Long) info.id.get(node);
    }

    @Override
    protected <T extends S> T handleRelationships(T node) {
        if (node == null) {
            return null;
        }

        // overload info
        ClassInformation info = this.info;
        if (!node.getClass().equals(this.clazz)) {
            // try to find a better class to objectify this node
            info = ClassInformation.constructClassInformation(node.getClass(), this.info.isNamespaceaware());
        }


        for (RelationshipInformation information : info.relationships.values()) {
            if (information.getTargetClassInformation() == null) {
                Object snapDecision = information.get(node);
                if (snapDecision == null) {
                    continue;
                }
            }

            boolean relationship = information.getTargetClassInformation(node).getType().equals(ClassInformation.Neo4JType.RELATIONSHIP);

            if (information.isBulk()) {
                Collection<?> nodes = (Collection<?>) information.get(node);
                if (nodes != null) {
                    // create all target science.neo4j.nodes not yet in the class
                    List<?> unsynced = nodes
                        .stream()
                        .filter(x -> information.getTargetClassInformation().getId().get(x) == null)
                        .collect(Collectors.toList());
                    if (!unsynced.isEmpty()) {
                        findRepository(information).saveAll(unsynced);
                    }
                    // sync the relationships // TODO #2 handle delete of relationship node (currently only create)
                    if (!relationship && !nodes.isEmpty()) {
                        long[] targetIds = nodes.stream().mapToLong(x -> (Long) information.getTargetClassInformation().getId().get(x)).toArray();
                        ClassInformation targetClassInformation = information.getTargetClassInformation();
                        String name = targetClassInformation.isNamespaceaware() ? targetClassInformation.getNsName() : targetClassInformation.getName();
                        saveRelationshipBulk(name, information.getType(), (Long) info.id.get(node), targetIds);
                    }
                }
            } else if (relationship) {
                Object target = information.get(node);
                if (target != null) {
                    findRepository(information).save(target);
                }
            } else {
                Object target = information.get(node);
                Long targetId = target != null ? (Long) information.getTargetClassInformation().getId().get(target) : null;
                if (targetId == null && target != null) {
                    targetId = (Long) information.getTargetClassInformation().getId().get(findRepository(information).save(target));
                }

                ClassInformation targetClassInformation = information.getTargetClassInformation();
                String name = targetClassInformation.isNamespaceaware() ? targetClassInformation.getNsName() : targetClassInformation.getName();
                saveRelationship(name, information.getType(), (Long) info.id.get(node), targetId);
            }
        }
        return node;
    }

    @Override
    protected <T extends S> Collection<T> handleRelationships(Collection<T> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return nodes;
        }

        for (RelationshipInformation information : info.relationships.values()) {
            if (information.getTargetClassInformation() == null) {
                Optional snapDecision = nodes.stream().map(information::get).filter(Objects::nonNull).findFirst();
                if (!snapDecision.isPresent()) {
                    continue;
                }
                information.getTargetClassInformation(snapDecision.get());
            }

            boolean relationship = information.getTargetClassInformation().getType().equals(ClassInformation.Neo4JType.RELATIONSHIP);
            if (relationship && information.isBulk()) {
                // TODO #2 handle relationship deletes
                List<?> collect = (List<?>) nodes
                    .stream()
                    .map(information::get)
                    .map(Collection.class::cast)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

                findRepository(information).saveAll(collect);
            } else if (information.isBulk()) {
                // handle 1..* relationships that are node classes
                List<?> unsynced = (List<?>) nodes.stream()
                    .map(information::get)
                    .map(Collection.class::cast)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .filter(target -> information.getTargetClassInformation().getId().get(target) == null)
                    .collect(Collectors.toList());

                if (!unsynced.isEmpty()) {
                    // create all target science.neo4j.nodes not yet in the class
                    findRepository(information).saveAll(unsynced);
                }
                // sync the relationships
                if (!nodes.isEmpty()) {
                    List<Value> sources = new LinkedList<>();
                    nodes.forEach(x -> {
                        List<Value> targets = new LinkedList<>();
                        Collection<?> targetCollection = (Collection<?>) information.get(x);
                        if (targetCollection != null && !targetCollection.isEmpty()) {
                            targetCollection.forEach(target -> {
                                Long targetId = target != null ? (Long) information.getTargetClassInformation().getId().get(target) : null;
                                if (targetId != null) {
                                    targets.add(Values.value(targetId));
                                }
                            });
                            HashMap<Object, Object> map = new HashMap<>();
                            map.put("id", info.id.get(x));
                            map.put("targets", Values.value(targets));
                            sources.add(Values.value(map));
                        }

                    });
                    ClassInformation targetClassInformation = information.getTargetClassInformation();
                    String name = targetClassInformation.isNamespaceaware() ? targetClassInformation.getNsName() : targetClassInformation.getName();
                    saveBulkRelationshipBulk(name, information.getType(), Values.value(sources));
                }
            } else if (relationship) {
                // handle relationship classes // TODO #2 handle relationship deletes
                repositories.get(information.getTargetClassInformation().clazz).saveAll(nodes.stream().map(information::get).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
            } else {
                // handle 1..1 relationships that are node classes

                List<?> unsynced = nodes
                    .stream()
                    .map(information::get)
                    .distinct()
                    .filter(Objects::nonNull)
                    .filter(x -> information.getTargetClassInformation().getId().get(x) == null)
                    .collect(Collectors.toList());
                if (!unsynced.isEmpty()) {
                    repositories.get(information.getTargetClassInformation().clazz).saveAll(unsynced);
                }

                List<Value> tuples = new LinkedList<>();
                nodes.forEach(x -> {
                    Object target = information.get(x);
                    if (target != null) {
                        Long targetId = (Long) information.getTargetClassInformation().getId().get(target);
                        HashMap<Object, Object> map = new HashMap<>();
                        map.put("source", info.id.get(x));
                        map.put("target", targetId);
                        tuples.add(Values.value(map));
                    }
                });
                ClassInformation targetClassInformation = information.getTargetClassInformation();
                String name = targetClassInformation.isNamespaceaware() ? targetClassInformation.getNsName() : targetClassInformation.getName();
                saveBulkRelationship(name, information.getType(), Values.value(tuples));
            }
        }
        return nodes;
    }

    @Override
    protected Map<String, Object> objectify(S node) {
        ClassInformation info = this.info;
        Map<String, Object> map = new HashMap<>();

        if (!node.getClass().equals(this.clazz)) {
            // try to find a better class to objectify this node
            info = ClassInformation.constructClassInformation(node.getClass(), this.info.isNamespaceaware());
        }

        map.put("classNames", info.namespaceaware ? info.nsLabels : info.labels);
        map.put("properties", info.objectifyProperties(node));
        map.put("id", info.id.get(node));

        return map;
    }

    @Override
    public <T> T query(String query, Value parameters, Class<T> clazz) {
        return queryNormalOrTyped(query, parameters, clazz, repositories);
    }

    @Override
    public <T> Stream<T> queryAll(String query, Value parameters, Class<T> clazz) {
        return queryAllNormalOrTyped(query, parameters, clazz, repositories);
    }

    /**
     * Not required as not all Nodes have relationships to other science.neo4j.nodes.
     *
     * @param repositories All related repositories are required
     */
    public void setRepositories(Map<Class, AbstractNeo4JRepository> repositories) {
        this.repositories = repositories;
    }

    public void setRelationshipOverrides(Map<String, Class> overrides) {
        // enable overriding class usage in relationships
        overrides.forEach((key, value) -> {
            if (this.info.getRelationships().containsKey(key) && this.info.getRelationships().get(key).getTargetClassInformation().getClazz().isAssignableFrom(value)) {
                this.info.getRelationships().get(key).setTargetClassInformation(ClassInformation.constructClassInformation(value));
            }
        });
    }
}

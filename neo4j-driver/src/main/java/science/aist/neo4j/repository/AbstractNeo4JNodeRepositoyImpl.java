package science.aist.neo4j.repository;

import science.aist.neo4j.Neo4jQueryRepository;
import science.aist.neo4j.transaction.ResultClone;
import science.aist.neo4j.transaction.TransactionManager;
import science.aist.neo4j.util.ValueCast;
import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Quick notes about usage:
 * <ul>
 * <li>This class should handle pretty much all the default stuff</li>
 * <li>On a save all nodes without an id will be saved (propagating to ALL nodes through EVERY relationship!)</li>
 * <li>We are currently only capable of handling (source)-[DIRECTED]-&gt;(target) unidirectional relationships from S to T.</li>
 * <li>On a load the node plus all related nodes are returned.</li>
 * </ul>
 * @param <S> Type of class
 * @author Oliver Krauss
 * @since 1.0
 */
public abstract class AbstractNeo4JNodeRepositoyImpl<S> extends AbstractNeo4JRepository<S, Long> implements Neo4jQueryRepository {

    protected final static String CLASSNAME = "CLASSNAME";

    //region CYPHER Statements
    /**
     * Create an object with all properties set
     */
    protected String CREATE_STATEMENT = "CREATE (n $properties) WITH n, $classNames as labels CALL apoc.create.addLabels(n, labels) YIELD node RETURN id(n)";

    /**
     * Create a list of objects with all properties set
     */
    protected String CREATE_BULK_STATEMENT = "UNWIND $nodes as node CREATE (n) SET n = node.properties WITH n, node.classNames as labels CALL apoc.create.addLabels(n, labels) YIELD node RETURN id(n)";

    /**
     * Update an existing object
     */
    protected String UPDATE_STATEMENT = "MATCH (n) WHERE ID(n) = $id WITH n, $classNames as labels CALL apoc.create.addLabels(n, labels) YIELD node SET n = $properties";

    /**
     * Updates all given nodes
     */
    protected String UPDATE_BULK_STATEMENT = "UNWIND $nodes as node MATCH (n) WHERE id(n) = node.id WITH n, $classNames as labels CALL apoc.create.addLabels(n, labels) YIELD node SET n = node.properties";

    /**
     * Searches object by id
     */
    protected String FIND_BY_ID_STATEMENT = "MATCH (n:CLASSNAME) WHERE ID(n) = $id OPTIONAL MATCH (n)-[r]->(c) RETURN {root: n, relationships: collect(distinct r), nodes: collect(distinct c)}";

    /**
     * Searches for a given _WHERE_.
     * The _WHERE_ should be replaced with the specific statement, such as "WHERE n.fieldThatIsString CONTAINS $substring
     */
    protected String FIND_BY_WHERE_STATEMENT = "MATCH (n:CLASSNAME) WHERE _WHERE_ OPTIONAL MATCH (n)-[r]->(c) RETURN {root: n, relationships: collect(distinct r), nodes: collect(distinct c)}";

    /**
     * Searches for all objects of this class
     */
    protected String FIND_ALL_STATEMENT = "MATCH (n:CLASSNAME) OPTIONAL MATCH (n)-[r]->(c) RETURN {root: n, relationships: collect(distinct r), nodes: collect(distinct c)}";
    /**
     * deletes all objects of this class
     */
    protected String DELETE_ALL_STATEMENT = "MATCH (n:CLASSNAME) DETACH DELETE n";

    // TODO #6 We might actually be able to update all relationships in a single statment. For that we may just have to source the relationship and targetclass into a variable. -> faster? yes no maybe so?

    /**
     * Finds or creates the relationship between two existing nodes
     */
    protected String RELATIONSHIP_STATEMENT = "MATCH (a), (b) WHERE id(a) = $id1 and id(b) = $id2 MERGE (a)-[r:RELATIONSHIP]->(b)";

    /**
     * Finds or creates relationships from one node to 1..* nodes
     */
    protected String RELATIONSHIP_BULK_STATEMENT = "MATCH (a) WHERE id(a) = $id UNWIND $targets as target MATCH (b) WHERE id(b) = target MERGE (a)-[r:RELATIONSHIP]->(b)";

    /**
     * Handles relationships in save all that are between two nodes
     */
    protected String BULK_RELATIONSHIP_STATEMENT = "UNWIND $tuples as tuple MATCH (a), (b) WHERE id(a) = tuple.source and id(b) = tuple.target MERGE (a)-[r:RELATIONSHIP]->(b)";

    /**
     * Handles relationships in save all that are 1..*
     */
    protected String BULK_RELATIONSHIP_BULK_STATEMENT = "UNWIND $sources as source MATCH (a) WHERE id(a) = source.id UNWIND source.targets as target MATCH (b) WHERE id(b) = target MERGE (a)-[r:RELATIONSHIP]->(b)";
    private String className;

    /**
     * Loads a node and all outgoing relationships from that node and child nodes up to DEPTH
     * Note: RELATIONSHIPS and DEPTH must be replaced on the fly, as neo4j does not support either search
     */
    protected String FIND_SUBTREE_STATEMENT = "MATCH (n:CLASSNAME) WHERE ID(n) = $id OPTIONAL MATCH (n)-[rRELATIONSHIPS*..DEPTH]->(c) UNWIND r as row RETURN {root: n, relationships: collect(distinct row), nodes: collect(distinct c)}";

    protected static Logger logger = LoggerFactory.getLogger(AbstractNeo4JNodeRepositoyImpl.class);

    //endregion


    //region General

    /**
     * empty constructor for child-implementations.
     * If you use this one, you MUST call the init function
     * @param manager used to handle transactions
     */
    protected AbstractNeo4JNodeRepositoyImpl(TransactionManager manager) {
        super(manager);
    }

    protected AbstractNeo4JNodeRepositoyImpl(TransactionManager manager, String className) {
        this(manager);
        init(className, className);
    }


    protected void init(String className, String labels) {
        this.className = className;
        FIND_BY_ID_STATEMENT = FIND_BY_ID_STATEMENT.replace(CLASSNAME, className);
        FIND_ALL_STATEMENT = FIND_ALL_STATEMENT.replace(CLASSNAME, className);
        DELETE_ALL_STATEMENT = DELETE_ALL_STATEMENT.replace(CLASSNAME, className);
        FIND_BY_WHERE_STATEMENT = FIND_BY_WHERE_STATEMENT.replace(CLASSNAME, className);
        FIND_SUBTREE_STATEMENT = FIND_SUBTREE_STATEMENT.replace(CLASSNAME, className);
    }

    //endregion

    @SuppressWarnings({"unchecked"})
    protected <X> X queryNormalOrTyped(String query, Value parameters, Class<X> clazz, Map<Class, AbstractNeo4JRepository> repositories) {
        if (repositories.containsKey(clazz)) {
            AbstractNeo4JRepository abstractNeo4JRepository = repositories.get(clazz);
            if (abstractNeo4JRepository instanceof AbstractNeo4JNodeRepositoyImpl) {
                return (X) ((AbstractNeo4JNodeRepositoyImpl) abstractNeo4JRepository).queryTyped(query, parameters);
            }
        }

        return queryHelp(query, parameters, clazz);
    }

    @SuppressWarnings({"unchecked"})
    protected <X> Stream<X> queryAllNormalOrTyped(String query, Value parameters, Class<X> clazz, Map<Class, AbstractNeo4JRepository> repositories) {
        if (repositories.containsKey(clazz)) {
            AbstractNeo4JRepository abstractNeo4JRepository = repositories.get(clazz);
            if (abstractNeo4JRepository instanceof AbstractNeo4JNodeRepositoyImpl) {
                Iterable iterable = ((AbstractNeo4JNodeRepositoyImpl) abstractNeo4JRepository).queryAllTyped(query, parameters);
                return StreamSupport.stream(iterable.spliterator(), false);
            }
        }

        return queryAllHelp(query, parameters, clazz);
    }

    @Override
    public <T> T query(String query, Value parameters, Class<T> clazz) {
        return queryHelp(query, parameters, clazz);
    }

    protected <T> T queryHelp(String query, Value parameters, Class<T> clazz) {
        if (clazz.getSimpleName().equals(className)) {
            return clazz.cast(queryTyped(query, parameters));
        } else {
            Result statementResult = getTransactionManager().executeWrite(transaction -> transaction.run(query, parameters));
            if (!statementResult.hasNext())
                return null;
            return ValueCast.castToJavaLang(statementResult.next().get(0), clazz);
        }
    }

    @Override
    public <T> Stream<T> queryAll(String query, Value parameters, Class<T> clazz) {
        return queryAllHelp(query, parameters, clazz);
    }

    protected <T> Stream<T> queryAllHelp(String query, Value parameters, Class<T> clazz) {
        if (clazz.getSimpleName().equals(className)) {
            return ValueCast.cast(StreamSupport.stream(queryAllTyped(query, parameters).spliterator(), false));
        } else {
            return getTransactionManager().executeWrite(transaction -> {
                Result result = new ResultClone(transaction.run(query, parameters));
                Iterable<T> it = () -> new Iterator<T>() {
                    public boolean hasNext() {
                        return result.hasNext();
                    }

                    public T next() {
                        return ValueCast.castToJavaLang(result.next().get(0), clazz);
                    }
                };
                return StreamSupport.stream(it.spliterator(), false);
            });
        }
    }


    //region Abstract functions to implement

    /**
     * Casts the result of the queryTyped into a DomainObject
     *
     * @param value         the value to be casted as root
     * @param relationships the relationships in the subgraph
     * @param nodes         the relationships are pointing to
     * @return the casted result
     */
    protected abstract S cast(Value value, Value relationships, Value nodes);

    /**
     * Helper method for setting the id when a node was stored to the database
     *
     * @param node to be assigned an id
     * @param id id to be assigned
     * @return node with id
     */
    protected abstract S setId(S node, Long id);

    /**
     * Helper method for finding the id of a node
     *
     * @param node to return the id from
     * @return id of node or null
     */
    protected abstract Long getId(S node);

    /**
     * Helper method that is responsible for storing all relationships.
     * Use "saveRelationship" and "saveRelationshipBulk" when implementing this method!
     *
     * @param node to be saved
     * @param <T>  extension of S
     * @return node
     */
    protected abstract <T extends S> T handleRelationships(T node);

    /**
     * Helper method that is responsible for storing all relationships in a multitude of nodes
     * Use "saveBulkRelationship" and "saveBulkRelationshipBulk" when implementing this method
     *
     * @param nodes the nodes with relations which should be handled
     * @param <T>   extension of S
     * @return the stored nodes
     */
    protected abstract <T extends S> Collection<T> handleRelationships(Collection<T> nodes);

    /**
     * Turns all properties of a Node into something to be used by the queries
     *
     * @param node to be transformed
     * @return a map containing the id, and another map that contains all properties
     */
    protected abstract Map<String, Object> objectify(S node);

    //endregion

    //region Helper functions


    /**
     * Helper function that simply executes a given queryTyped
     *
     * @param query      to be run
     * @param parameters to be added to the queryTyped
     * @param accessMode mode to execute the query
     * @return result
     */
    protected S execute(String query, Value parameters, AccessMode accessMode) {
        return getTransactionManager().execute(transaction -> {
            Result result = transaction.run(query, parameters);
            if (result.hasNext()) {
                Value r = result.next().get(0);
                return cast(r.get("root"), r.get("relationships"), r.get("nodes"));
            }
            return null;
        }, accessMode);
    }

    /**
     * Helper function that simply executes a given queryTyped
     *
     * @param query      to be run
     * @param parameters to be added to the queryTyped
     * @param accessMode mode to execute the query
     * @return result
     */
    protected Iterable<S> executeAll(String query, Value parameters, AccessMode accessMode) {
        return getTransactionManager().execute(transaction -> {
            Result result = new ResultClone(transaction.run(query, parameters));
            return () -> new Iterator<S>() {
                @Override
                public boolean hasNext() {
                    return result.hasNext();
                }

                @Override
                public S next() {
                    Value r = result.next().get(0);
                    return cast(r.get("root"), r.get("relationships"), r.get("nodes"));
                }
            };
        }, accessMode);
    }

    //endregion

    //region Relationships

    /**
     * Creates or deletes a 0..1 relationship between two nodes
     *
     * @param targetClass      Label that the target must have
     * @param relationshipName Name of the relationship that will be created/deleted
     * @param sourceId         Id of source node (the node THIS repository is responsible for)
     * @param targetId         Id of the target node (the target of the relationship)
     */
    protected void saveRelationship(String targetClass, String relationshipName, Long sourceId, Long targetId) {
        if (targetClass == null || relationshipName == null || sourceId == null) {
            throw new InvalidParameterException("targetClass, relationShipName and sourceId MUST be set");
        }
        getTransactionManager().runWrite(transaction -> transaction.run(RELATIONSHIP_STATEMENT.replace("RELATIONSHIP", relationshipName),
            Values.parameters("id1", sourceId, "id2", targetId)).consume());
    }

    /**
     * Creates or deletes a 0..* relationship between two nodes
     *
     * @param targetClass      Label that the target must have
     * @param relationshipName Name of the relationship that will be created/deleted
     * @param sourceId         Id of source node (the node THIS repository is responsible for)
     * @param targetIds        Id of all target nodes (the target of the relationship)
     */
    protected void saveRelationshipBulk(String targetClass, String relationshipName, Long sourceId, long[] targetIds) {
        if (targetClass == null || relationshipName == null || sourceId == null) {
            throw new InvalidParameterException("targetClass, relationShipName and sourceId MUST be set");
        }
        getTransactionManager().runWrite(transaction -> transaction.run(RELATIONSHIP_BULK_STATEMENT.replace("RELATIONSHIP", relationshipName),
            Values.parameters("id", sourceId, "targets", targetIds)).consume());
        // TODO #2 Delete all Relationships that are in the DB but NOT in the relationship. -> low priority as we do not delete while logging
    }

    /**
     * Creates or deletes a 1..1 relationship for a multitude of nodes
     *
     * @param targetClass      Label that the target must have
     * @param relationshipName Name of the relationship that will be created/deleted
     * @param tuples           MUST be a List of values with a "source" -&gt; id and "target" -&gt; id for EVERY tuple
     */
    protected void saveBulkRelationship(String targetClass, String relationshipName, Value tuples) {
        if (targetClass == null || relationshipName == null || tuples == null) {
            throw new InvalidParameterException("targetClass, relationShipName and tuples MUST be set");
        }

        getTransactionManager().runWrite(transaction -> transaction.run(BULK_RELATIONSHIP_STATEMENT.replace("RELATIONSHIP", relationshipName),
            Values.parameters("tuples", tuples)).consume());
        // TODO #2 Handle Deletes
    }

    /**
     * Creates or deletes a 1..* relationship for a multitude of nodes
     *
     * @param targetClass      Label that target must have
     * @param relationshipName Name of the relationship that will be created/deleted
     * @param sources          MUST be a List ov values with a "source" -&gt; id and "targets" -&gt; list[id] for EVERY node
     */
    protected void saveBulkRelationshipBulk(String targetClass, String relationshipName, Value sources) {
        if (targetClass == null || relationshipName == null || sources == null) {
            throw new InvalidParameterException("targetClass, relationShipName and sources MUST be set");
        }
        getTransactionManager().runWrite(transaction -> transaction.run(BULK_RELATIONSHIP_BULK_STATEMENT.replace("RELATIONSHIP", relationshipName),
            Values.parameters("sources", sources)).consume());
        // TODO #2 Delete all Relationships that are in the DB but NOT in the relationship. -> low priority as we do not delete while logging
    }

    // endregion

    // region Neo4JRepository

    @Override
    public <T extends S> T save(final T node) {
        return getTransactionManager().executeWrite(transaction -> {
            Map<String, Object> properties = objectify(node);
            if (properties.get("id") == null) {
                setId(node, transaction.run(CREATE_STATEMENT, properties).single().get(0).asLong());
            } else {
                transaction.run(UPDATE_STATEMENT, properties).consume();
            }
            T res = handleRelationships(node);

            return res;
        });
    }

    @Override
    public <T extends S> Iterable<T> saveAll(Iterable<T> nodes) {
        return getTransactionManager().executeWrite(transaction -> {
            List<T> create = StreamSupport
                .stream(nodes.spliterator(), false)
                .filter(x -> getId(x) == null)
                .collect(Collectors.toList());

            List<T> update = StreamSupport
                .stream(nodes.spliterator(), false)
                .filter(x -> getId(x) != null)
                .collect(Collectors.toList());

            if (!update.isEmpty()) {
                transaction.run(UPDATE_BULK_STATEMENT, Values.parameters("nodes", update.stream().map(this::objectify).toArray())).consume();
            }
            if (!create.isEmpty()) {
                Result result = transaction.run(CREATE_BULK_STATEMENT, Values.parameters("nodes", create.stream().map(this::objectify).toArray()));
                create.forEach(x -> setId(x, result.next().get(0).asLong()));
            }
            update.addAll(create);
            handleRelationships(update);


            return update;
        });
    }

    @Override
    public void deleteAll() {
        getTransactionManager().runWrite(transaction -> transaction.run(DELETE_ALL_STATEMENT).consume());
    }

    @Override
    public Iterable<S> findAll() {
        return executeAll(FIND_ALL_STATEMENT, null, AccessMode.READ);
    }

    @Override
    public S findById(Long id) {
        return execute(FIND_BY_ID_STATEMENT, Values.parameters("id", id), AccessMode.READ);
    }

    @Override
    public S findSubtree(Long id) {
        return findSubtree(id, -1);
    }

    @Override
    public S findSubtree(Long id, int depth) {
        return findSubtree(id, depth, null);
    }

    @Override
    public S findSubtree(Long id, int depth, List<String> relationships) {
        String relationshipString = relationships == null || relationships.isEmpty() ? "" : ":" + relationships.stream().collect(Collectors.joining("|"));
        String depthString = depth > 0 ? String.valueOf(depth) : "";
        String query = FIND_SUBTREE_STATEMENT.replace("RELATIONSHIPS", relationshipString).replace("DEPTH", depthString);
        if (depth == 0) {
            query = query.substring(0, query.indexOf("OPTIONAL")) + "RETURN {root:n}";
        }
        return execute(query, Values.parameters("id", id), AccessMode.READ);
    }

    // endregion

    // region FindByCondition

    @Override
    public S findBy(String condition, Value parameters) {
        return execute(FIND_BY_WHERE_STATEMENT.replace("_WHERE_", condition), parameters, AccessMode.READ);
    }

    @Override
    public Iterable<S> findAllBy(String condition, Value parameters) {
        return executeAll(FIND_BY_WHERE_STATEMENT.replace("_WHERE_", condition), parameters, AccessMode.READ);
    }

    // endregion

    // region Custom queryTyped

    /**
     * Convenience function that allows calling any user defined queryTyped.
     * Parameters should be defined as "$parametername"
     * The value-list should be defined as Map of parametername (without the $) to value
     *
     * @param query      to be run
     * @param parameters to be set
     * @return node that matches the query
     */
    public S queryTyped(String query, Value parameters) {
        return getTransactionManager().executeRead(transaction -> {
            Result result = transaction.run(query, parameters);
            if (result.hasNext()) {
                return cast(result.next().get(0), null, null);
            }
            return null;
        });
    }

    /**
     * Convenience function that allows calling any user defined queryTyped.
     * Parameters should be defined as "$parametername"
     * The value-list should be defined as Map of parametername (without the $) to value
     *
     * @param query      to be run
     * @param parameters to be set
     * @return node that matches the query
     */
    public Iterable<S> queryAllTyped(String query, Value parameters) {
        return getTransactionManager().executeRead(transaction -> {
            Result result = new ResultClone(transaction.run(query, parameters));

            return () -> new Iterator<S>() {

                @Override
                public boolean hasNext() {
                    return result.hasNext();
                }

                @Override
                public S next() {
                    Value r = result.next().get(0);
                    return cast(r, null, null);
                }
            };
        });
    }

    // endregion
}

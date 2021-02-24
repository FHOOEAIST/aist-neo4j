package science.aist.neo4j.repository;

import science.aist.neo4j.transaction.TransactionManager;
import org.neo4j.driver.*;

import java.util.*;

/**
 * Quick notes about usage:
 * <ul>
 * <li>This class should handle pretty much all the default stuff</li>
 * <li>On a save all science.neo4j.nodes without an id will be saved (propagating to ALL science.neo4j.nodes through EVERY relationship!)</li>
 * <li>We are currently only capable of handling (source)-[DIRECTED]-&gt;(target) unidirectional relationships from S to T.</li>
 * <li>On a load the relationship + its source and target science.neo4j.nodes are loaded</li>
 * </ul>
 * @param <S> Type of class
 * @author Oliver Krauss
 * @since 1.0
 */
public abstract class AbstractNeo4JRelationshipRepositoyImpl<S> extends AbstractNeo4JRepository<S, Long> {

    //region CYPHER Statements

    /**
     * Create an object with all properties set
     */
    private String CREATE_STATEMENT = "MATCH (s), (t) where id(s) = $sourceId and id(t) = $targetId CREATE (s)-[r:TYPE $properties]->(t) return id(r)";

    /**
     * Create a list of objects with all properties set
     */
    private String CREATE_BULK_STATEMENT = "UNWIND $relationships as relationship MATCH (s), (t) where id(s) = relationship.sourceId and id(t) = relationship.targetId CREATE (s)-[r:TYPE]->(t) SET r = relationship.properties return id(r)";

    /**
     * Update an existing object
     */
    private String UPDATE_STATEMENT = "MATCH ()-[r:TYPE]->() WHERE id(r) = $id SET r = $properties";

    /**
     * Updates all given relationships
     */
    private String UPDATE_BULK_STATEMENT = "UNWIND $relationships as relationship MATCH ()-[r:TYPE]->() WHERE id(r) = relationship.id SET r = relationship.properties";

    /**
     * deletes all objects of this class
     */
    private String DELETE_ALL_STATEMENT = "MATCH ()-[r:TYPE]->() DELETE r";

    /**
     * Searches for all objects of this class
     */
    private String FIND_ALL_STATEMENT = "MATCH (s)-[r:TYPE]->(t) RETURN r, s, t";

    /**
     * Searches object by id
     */
    private String FIND_BY_ID_STATEMENT = "MATCH (s)-[r:TYPE]->(t) WHERE id(r) = $id RETURN r, s, t";

    /**
     * Searches for a given _WHERE_.
     * The _WHERE_ should be replaced with the specific statement, such as "WHERE n.fieldThatIsString CONTAINS $substring
     */
    private String FIND_BY_WHERE_STATEMENT = "MATCH (s)-[n:TYPE]-(t) WHERE _WHERE_ return n as r, s, t";

    // endregion

    //region General

    /**
     * empty constructor for child-implementations.
     * If you use this one, you MUST call the init function
     * @param manager used to handle transactions
     */
    protected AbstractNeo4JRelationshipRepositoyImpl(TransactionManager manager) {
        super(manager);
    }

    protected AbstractNeo4JRelationshipRepositoyImpl(TransactionManager manager, String className) {
        this(manager);
        init(className, className);
    }

    protected void init(String className, String labels) {
        CREATE_STATEMENT = CREATE_STATEMENT.replace("TYPE", labels);
        CREATE_BULK_STATEMENT = CREATE_BULK_STATEMENT.replace("TYPE", labels);
        UPDATE_STATEMENT = UPDATE_STATEMENT.replace("TYPE", className);
        UPDATE_BULK_STATEMENT = UPDATE_BULK_STATEMENT.replace("TYPE", className);
        FIND_BY_ID_STATEMENT = FIND_BY_ID_STATEMENT.replace("TYPE", className);
        FIND_ALL_STATEMENT = FIND_ALL_STATEMENT.replace("TYPE", className);
        DELETE_ALL_STATEMENT = DELETE_ALL_STATEMENT.replace("TYPE", className);
        FIND_BY_WHERE_STATEMENT = FIND_BY_WHERE_STATEMENT.replace("TYPE", className);
    }

    // endregion

    //region Abstract functions to implement

    /**
     * Casts the result of the queryTyped into a relationship class
     *
     * @param relationship to be cast
     * @param source       source node of relationship
     * @param target       target node of relationship
     * @return relationship object
     */
    protected abstract S cast(Value relationship, Value source, Value target);

    /**
     * Helper method for setting the id when a node was stored to the database
     *
     * @param relationship to be assigned an id
     * @param id of the relationsip
     * @return node with id
     */
    protected abstract S setId(S relationship, Long id);

    /**
     * Helper method for finding the id of a node
     *
     * @param relationship to return the id from
     * @return id of node or null
     */
    protected abstract Long getId(S relationship);

    /**
     * Helper method that is responsible for storing source or target science.neo4j.nodes.
     *
     * @param node to be saved
     * @param <T>  extension of S
     * @return node
     */
    protected abstract <T extends S> T handleSourceAndTarget(T node);

    /**
     * Helper method that is responsible for storing all source or target science.neo4j.nodes in a multitude of relationships
     *
     * @param nodes to be saved
     * @param <T>   extension of S
     * @return science.neo4j.nodes
     */
    protected abstract <T extends S> Collection<T> handleSourceAndTarget(Collection<T> nodes);

    /**
     * Turns all properties of a Relationship into something to be used by the queries
     *
     * @param relationship to be transformed
     * @return a map containing the id, and another map that contains all properties
     */
    protected abstract Map<String, Object> objectify(S relationship);

    // endregion

    //region Helper functions

    /**
     * Helper function that simply executes a given queryTyped
     *
     * @param query      to be run
     * @param parameters to be added to the queryTyped
     * @param accessMode of the query that should be executed
     * @return result
     */
    protected S execute(String query, Value parameters, AccessMode accessMode) {
        Record r = getTransactionManager().execute(transaction -> transaction.run(query, parameters).next(), accessMode);
        return cast(r.get("r"), r.get("s"), r.get("t"));
    }

    /**
     * Helper function that simply executes a given queryTyped
     *
     * @param query      to be run
     * @param parameters to be added to the queryTyped
     * @param accessMode of the query that should be executed
     * @return result
     */
    protected Iterable<S> executeAll(String query, Value parameters, AccessMode accessMode) {
        Result result = getTransactionManager().execute(transaction -> transaction.run(query, parameters), accessMode);
        return () -> new Iterator<S>() {
            @Override
            public boolean hasNext() {
                return result.hasNext();
            }

            @Override
            public S next() {
                Record r = result.next();
                return cast(r.get("r"), r.get("s"), r.get("t"));
            }
        };
    }
    //endregion

    // region Nodes

    // endregion

    // region Neo4JRepository

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
    public S findSubtree(Long aLong) {
        throw new UnsupportedOperationException("Relationships don't support infinite loading");
    }

    @Override
    public S findSubtree(Long aLong, int depth) {
        throw new UnsupportedOperationException("Relationships don't support infinite loading");
    }

    @Override
    public S findSubtree(Long aLong, int depth, List<String> relationships) {
        throw new UnsupportedOperationException("Relationships don't support infinite loading");
    }

    @Override
    public <T extends S> T save(T relationship) {
        if (getId(relationship) == null) {
            relationship = handleSourceAndTarget(relationship);
            Map<String, Object> properties = objectify(relationship);
            setId(relationship, getTransactionManager().executeWrite(transaction -> transaction.run(CREATE_STATEMENT, properties).single().get(0).asLong()));
        } else {
            Map<String, Object> properties = objectify(relationship);
            getTransactionManager().runWrite(transaction -> transaction.run(UPDATE_STATEMENT, properties).consume());
        }

        return relationship;
    }

    @Override
    public <T extends S> Iterable<T> saveAll(Iterable<T> relationships) {
        List<T> update = new LinkedList<>();
        List<T> create = new LinkedList<>();
        relationships.forEach(x -> {
            if (getId(x) == null) {
                create.add(x);
            } else {
                update.add(x);
            }
        });

        if (!update.isEmpty()) {
            getTransactionManager().runWrite(transaction -> transaction.run(UPDATE_BULK_STATEMENT, Values.parameters("relationships", update.stream().map(this::objectify).toArray())).consume());
        }
        if (!create.isEmpty()) {
            //
            handleSourceAndTarget(create);

            Result result = getTransactionManager().executeWrite(transaction -> transaction.run(CREATE_BULK_STATEMENT, Values.parameters("relationships", create.stream().map(this::objectify).toArray())));
            create.forEach(x -> setId(x, result.next().get(0).asLong()));
        }
        update.addAll(create);

        return update;
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
     * @return node that matches the queryTyped
     */
    public S query(String query, Value parameters) {
        return getTransactionManager().executeRead(transaction -> {
            // an unfortunate fix because we must enforce that source and target are returned as well, and can't force the user to write the queryTyped accordingly
            Long id = getId(cast(transaction.run(query, parameters).next().get(0), null, null));
            return findById(id);
        });
    }

    /**
     * Convenience function that allows calling any user defined queryTyped.
     * Parameters should be defined as "$parametername"
     * The value-list should be defined as Map of parametername (without the $) to value
     *
     * @param query      to be run
     * @param parameters to be set
     * @return node that matches the queryTyped
     */
    public Iterable<S> queryAll(String query, Value parameters) {
        Result result = getTransactionManager().executeRead(transaction -> transaction.run(query, parameters));
        return () -> new Iterator<S>() {
            @Override
            public boolean hasNext() {
                return result.hasNext();
            }

            @Override
            public S next() {
                Value r = result.next().get(0);
                Long id = getId(cast(r, null, null));
                return findById(id);
            }
        };
    }

    // endregion
}

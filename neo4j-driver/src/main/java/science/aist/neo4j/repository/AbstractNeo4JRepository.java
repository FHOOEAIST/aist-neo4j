package science.aist.neo4j.repository;

import science.aist.neo4j.Neo4jRepository;
import science.aist.neo4j.transaction.TransactionManager;
import science.aist.neo4j.util.Pair;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Intermediate class combining features both the node and relationship repositories both have
 *
 * @param <S>  Type of object to be stored to database
 * @param <ID> Simple Datatype of object to be stored to database (simple datatype = field type and not node type)
 * @author Oliver Krauss
 * @since 1.0
 */
public abstract class AbstractNeo4JRepository<S, ID> implements Neo4jRepository<S, ID> {

    /**
     * Repositories that are publicly provided for use Map<SignatureOfClass, RepositoryForIt>
     */
    private static Map<TransactionManager, Map<String, AbstractNeo4JRepository>> providedRepositories = new HashMap<>();

    public static AbstractNeo4JRepository getProvidedRepository(TransactionManager manager, String signature) {
        return providedRepositories.getOrDefault(manager, new HashMap<>()).getOrDefault(signature, new FutureRepository(manager, signature));
    }

    public static void provideRepository(TransactionManager manager, String signature, AbstractNeo4JRepository repository) {
        if (!providedRepositories.containsKey(manager)) {
            providedRepositories.put(manager, new HashMap<>());
        }
        providedRepositories.get(manager).put(signature, repository);
    }

    private TransactionManager transactionManager;

    public AbstractNeo4JRepository(TransactionManager manager) {
        this.transactionManager = manager;
    }

    /**
     * gets value of field {@link AbstractNeo4JRepository#transactionManager}
     *
     * @return value of field transactionManager
     * @see AbstractNeo4JRepository#transactionManager
     */
    protected TransactionManager getTransactionManager() {
        return transactionManager;
    }

    // region Helper functions

    /**
     * Helper function that builds the WHERE statement for findByCondition
     *
     * @param tuples to be transformed into "n.key = n.value AND ..."
     * @return String for build
     */
    protected String buildWhere(List<Pair<String, Object>> tuples) {
        if (tuples.isEmpty()) {
            return "1 = 1";
        }

        StringBuilder where = new StringBuilder();
        for (Pair<String, Object> tuple : tuples) {
            where.append("n.").append(tuple.getKey()).append(" = $").append(tuple.getKey()).append(" AND ");
        }
        return where.substring(0, where.length() - " AND ".length());
    }

    /**
     * Helper function that turns the key value pairs into a Values object for the parameterized queryTyped
     *
     * @param tuples to be transformed into a Value for the DB
     * @return Value to be used in cypher queryTyped
     */
    protected Value buildParameters(List<Pair<String, Object>> tuples) {
        Object[] params = new Object[tuples.size() * 2];
        int i = 0;
        for (Pair<String, Object> tuple : tuples) {
            params[i] = tuple.getKey();
            params[i + 1] = tuple.getValue();
            i += 2;
        }
        return Values.parameters(params);
    }

    // endregion


    // region FindByCondition

    /**
     * Returns the first node that satisfies the given condition. The condition may access the node with "n"
     * Example: findByCondition("n.name = $name", Values.parameters("name", "Dijkstra"))
     *
     * @param condition  any condition that would be after the "WHERE" in a cypher queryTyped
     * @param parameters all parameters defined in the condition (parameters optional)
     * @return first node that satisifes condition with parameters
     */
    public abstract S findBy(String condition, Value parameters);

    /**
     * Returns ALL science.neo4j.nodes that satisfies the given condition. The condition may access the node with "n"
     * Example: findByCondition("n.name in $names", Values.parameters("names", Values.parameters("Dijkstra", "Knuth", "Turing")))
     *
     * @param condition  any condition that would be after the "WHERE" in a cypher queryTyped
     * @param parameters all parameters defined in the condition (parameters optional)
     * @return first node that satisifes condition with parameters
     */
    public abstract Iterable<S> findAllBy(String condition, Value parameters);

    /**
     * Find node by a field with a given value
     *
     * @param field name of field that should match
     * @param value value that should be matched to
     * @return first node that has the given value in the field
     */
    public S findBy(String field, String value) {
        return findBy("n." + field + " = $value", Values.parameters("value", value));
    }


    /**
     * Find all science.neo4j.nodes by a field with a given value (eg. n.amount = 3)
     *
     * @param field name of field that should match
     * @param value value that should be matched to
     * @return all science.neo4j.nodes that have the given value in the field
     */
    public Iterable<S> findAllBy(String field, String value) {
        return findAllBy("n." + field + " = $value", Values.parameters("value", value));
    }

    /**
     * Find node by a list of fields that should match a given value
     *
     * @param values Key Value pairs where the key is the fieldname and the value is the value the field should match
     * @return first node that matches on all fields
     */
    public S findBy(List<Pair<String, Object>> values) {
        return findBy(buildWhere(values), buildParameters(values));
    }


    /**
     * Find science.neo4j.nodes by a list of fields that should match a given value
     *
     * @param values Key Value pairs where the key is the fieldname and the value is the value the field should match
     * @return all science.neo4j.nodes that match fields
     */
    public Iterable<S> findAllBy(List<Pair<String, Object>> values) {
        return findAllBy(buildWhere(values), buildParameters(values));
    }

    // endregion
}

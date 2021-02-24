package science.aist.neo4j;

import org.neo4j.driver.Value;

import java.util.stream.Stream;

/**
 * <p>Repository Interface for executing queries on a neo4j database</p>
 *
 * @author Andreas Pointner
 * @author Christoph Praschl
 * @since 1.0
 */
public interface Neo4jQueryRepository {
    /**
     * Method for executing a query with a single result in a own transaction
     *
     * @param query      The query text
     * @param parameters The query parameters
     * @param clazz      The result clazz type
     * @param <T>        Result type
     * @return Single query result
     */
    <T> T query(String query, Value parameters, Class<T> clazz);

    /**
     * Method for executing a query with multiple results in a own transaction
     *
     * @param query      The query text
     * @param parameters The query parameters
     * @param clazz      The result clazz type
     * @param <T>        Result type
     * @return Stream of results
     */
    <T> Stream<T> queryAll(String query, Value parameters, Class<T> clazz);
}

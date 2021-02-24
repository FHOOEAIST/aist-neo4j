package science.aist.neo4j;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Interface for our Neo4j repositories
 *
 * @param <S>  Node class that we want to store
 * @param <ID> ID of the node in the database
 * @author Oliver Krauss
 * @since 1.0
 */
public interface Neo4jRepository<S, ID> {

    // TODO #4 long count()

    // TODO #4  void delete (S)

    /**
     * Deletes all science.neo4j.nodes in neo4j that are of type S
     */
    void deleteAll();

    // TODO #4 void deleteAll(Iterable<? extends T) entities)

    // TODO #4 void deleteById(ID id)

    // TODO #4 boolean existsById(ID id)

    /**
     * Returns all node in neo4j that are of type S
     *
     * @return iterator over all S
     */
    Iterable<S> findAll();

    /**
     * Returns all nodes as stream of type S
     *
     * @return stream of all S
     */
    default Stream<S> findAllAsStream() {
        return StreamSupport.stream(findAll().spliterator(), false);
    }

    // TODO #4 Iterable<T> findAllById(Iterable<ID> ids)

    /**
     * Returns the node with the given id, or null if no such node exists
     *
     * @param id id of node
     * @return node or null
     */
    S findById(ID id);

    /**
     * Returns the entire subtree of the node with the given id, or null if no such node exists
     *
     * @param id id of node
     * @return node with children (infinite depth!) or null
     */
    S findSubtree(ID id);

    /**
     * Returns the entire subtree of the node with the given id, or null if no such node exists
     *
     * @param id    id of node
     * @param depth depth until the subtree will be loaded (-1 is infinite, 0 is node only)
     * @return node with children up to depth or null
     */
    S findSubtree(ID id, int depth);

    /**
     * Returns the entire subtree of the node with the given id, or null if no such node exists
     *
     * @param id            id of node
     * @param depth         depth until the subtree will be loaded (-1 is infinite, 0 is node only)
     * @param relationships that will be loaded (all non mentioned will be omitted)
     * @return node with children up to depth or null
     */
    S findSubtree(ID id, int depth, List<String> relationships);

    /**
     * Stores the node in the database. If the node has an ID a merge is conducted, if the ID is empty a new node is always created
     *
     * @param node node to be stored
     * @param <T>  subtype of S
     * @return node with ID
     */
    <T extends S>
    T save(T node);

    /**
     * Saves all given science.neo4j.nodes (create or update)
     *
     * @param nodes to be saved
     * @param <T>   subtype of S
     * @return science.neo4j.nodes with id
     */
    <T extends S> Iterable<T> saveAll(Iterable<T> nodes);


}

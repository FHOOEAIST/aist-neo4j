package science.aist.neo4j.reflective;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;


/**
 * RelationshipInformation handles the metadata for the {@link ReflectiveNeo4JNodeRepositoryImpl}
 * It contains specific information on relationships between science.neo4j.nodes
 *
 * @param <S> Type of start node
 * @param <E> Type of end node
 * @param <K> Type of key in Map from {@link MapRelationshipInformation}
 * @author Oliver Krauss
 * @since 1.0
 */
@RelationshipEntity(type = "MAP_REL")
public class MapRelationship<S, E, K> {

    @Id
    private Long id;

    @StartNode
    S start;

    @EndNode
    E end;

    /**
     * Key of map is assigned to this field
     */
    K key;

    public MapRelationship() {
    }

    protected MapRelationship(Long id, S start, E end, K key) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.key = key;
    }

    public MapRelationship(S start, E end, K key) {
        this.start = start;
        this.end = end;
        this.key = key;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public S getStart() {
        return start;
    }

    public void setStart(S start) {
        this.start = start;
    }

    public E getEnd() {
        return end;
    }

    public void setEnd(E end) {
        this.end = end;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }
}

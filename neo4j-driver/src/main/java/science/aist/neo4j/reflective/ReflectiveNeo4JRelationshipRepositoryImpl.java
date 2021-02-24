package science.aist.neo4j.reflective;

import science.aist.neo4j.repository.AbstractNeo4JRelationshipRepositoyImpl;
import science.aist.neo4j.repository.AbstractNeo4JRepository;
import science.aist.neo4j.transaction.TransactionManager;
import org.neo4j.driver.Value;
import org.springframework.beans.factory.annotation.Required;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link AbstractNeo4JRelationshipRepositoyImpl} using java reflection
 *
 * @param <S> Type of node
 * @author Oliver Krauss
 * @since 1.0
 */
public class ReflectiveNeo4JRelationshipRepositoryImpl<S> extends AbstractNeo4JRelationshipRepositoyImpl<S> {

    // TODO #8 compile repository classes to improve performance

    /**
     * Class that this repository is managing
     */
    protected Class<S> clazz;

    /**
     * Metadata for storage purposes
     */
    protected ClassInformation info;

    /**
     * Repository for the source science.neo4j.nodes
     */
    protected AbstractNeo4JRepository sourceRepository;

    /**
     * Repository for the taret science.neo4j.nodes
     */
    protected AbstractNeo4JRepository targetRepository;

    /**
     * Empty constructor for child implementations.
     * If you use this one, you must call init() yourself.
     *
     * @param manager used to handle transactions
     */
    protected ReflectiveNeo4JRelationshipRepositoryImpl(TransactionManager manager) {
        super(manager);
    }


    /**
     * Generates the repository by a Generic Type
     *
     * @param type java generic type
     * @param name of relationship in DB as this is part of the identity
     * @param manager used to handle transactions
     */
    public ReflectiveNeo4JRelationshipRepositoryImpl(TransactionManager manager, ParameterizedType type, String name) {
        this(manager);
        provideRepository(this.getTransactionManager(), name + type.getTypeName(), this);
        this.clazz = (Class<S>) type.getRawType();
        this.info = ClassInformation.constructClassInformation(type, name);
        this.init(info.getName(), info.getLabels());
    }

    public ReflectiveNeo4JRelationshipRepositoryImpl(TransactionManager manager, Class<S> clazz) {
        this(manager);
        provideRepository(this.getTransactionManager(), clazz.getName(), this);
        this.clazz = clazz;
        this.info = ClassInformation.constructClassInformation(clazz);
        this.init(info.getName(), info.getLabels());
    }

    @Override
    protected S cast(Value value, Value source, Value target) {
        return (S) info.cast(value.asRelationship(), source == null ? null : source.asNode(), target == null ? null : target.asNode(), new HashMap<>());
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
    protected <T extends S> T handleSourceAndTarget(T node) {
        if (getSourceId(node) == null) {
            sourceRepository.save(getSource(node));
        }
        if (getTargetId(node) == null) {
            targetRepository.save(getTarget(node));
        }
        return node;
    }

    @Override
    protected <T extends S> Collection<T> handleSourceAndTarget(Collection<T> nodes) {

        sourceRepository.saveAll(nodes.stream().filter(x -> getSourceId(x) == null).map(this::getSource).distinct().collect(Collectors.toList()));

        targetRepository.saveAll(nodes.stream().filter(x -> getTargetId(x) == null).map(this::getTarget).distinct().collect(Collectors.toList()));

        return nodes;
    }

    @Override
    protected Map<String, Object> objectify(S node) {
        Map<String, Object> map = new HashMap<>();
        map.put("properties", info.objectifyProperties(node));
        map.put("sourceId", getSourceId(node));
        map.put("targetId", getTargetId(node));
        map.put("id", info.id.get(node));
        return map;
    }

    protected Object getSource(S node) {
        return info.relationships.get("source").get(node);
    }

    protected Object getTarget(S node) {
        return info.relationships.get("target").get(node);
    }

    protected Object getSourceId(S node) {
        return info.relationships.get("source").getTargetClassInformation(node).id.get(getSource(node));
    }

    protected Object getTargetId(S node) {
        return info.relationships.get("target").getTargetClassInformation(node).id.get(getTarget(node));
    }


    @Required
    public void setSourceRepository(AbstractNeo4JRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @Required
    public void setTargetRepository(AbstractNeo4JRepository targetRepository) {
        this.targetRepository = targetRepository;
    }


}

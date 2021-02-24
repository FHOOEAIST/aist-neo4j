package science.aist.neo4j.repository;

import science.aist.neo4j.transaction.TransactionManager;
import org.neo4j.driver.Value;

import java.util.List;

/**
 * In case of cyclic dependencies between repositories we return a future repository that attempts to load when needed.
 * The implementing classes, or another repository mechanism may choose to replace the future repository with the real one later.
 * @author Oliver Krauss
 * @since 1.0
 */
public class FutureRepository extends AbstractNeo4JRepository {

    /**
     * Signature the real repository must have
     */
    private String signature;

    private AbstractNeo4JRepository getRepository() {
        return AbstractNeo4JRepository.getProvidedRepository(getTransactionManager(), signature);
    }

    public FutureRepository(TransactionManager manager, String signature) {
        super(manager);
        this.signature = signature;
    }

    @Override
    public Object findBy(String condition, Value parameters) {
        return getRepository().findBy(condition, parameters);
    }

    @Override
    public Iterable findAllBy(String condition, Value parameters) {
        return getRepository().findAllBy(condition, parameters);
    }

    @Override
    public void deleteAll() {
        getRepository().deleteAll();
    }

    @Override
    public Iterable findAll() {
        return getRepository().findAll();
    }

    @Override
    public Object findById(Object o) {
        return getRepository().findById(o);
    }

    @Override
    public Object findSubtree(Object o) {
        return getRepository().findSubtree(o);
    }

    @Override
    public Object findSubtree(Object o, int depth) {
        return getRepository().findSubtree(o, depth);
    }

    @Override
    public Iterable saveAll(Iterable nodes) {
        return getRepository().saveAll(nodes);
    }

    @Override
    public Object save(Object node) {
        return getRepository().save(node);
    }

    @Override
    public Object findSubtree(Object o, int depth, List relationships) {
        return getRepository().findSubtree(o, depth, relationships);
    }
}

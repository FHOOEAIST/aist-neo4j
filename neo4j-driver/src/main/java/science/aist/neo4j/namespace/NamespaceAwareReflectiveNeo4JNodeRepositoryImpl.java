package science.aist.neo4j.namespace;

import science.aist.neo4j.reflective.ClassInformation;
import science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl;
import science.aist.neo4j.reflective.RelationshipInformation;
import science.aist.neo4j.transaction.TransactionManager;
import science.aist.neo4j.util.Pair;

import java.util.*;

/**
 * Repository that also considers namespaces while handling Neo4J.
 * The functionality is identical to the ReflectiveNeo4JNodeRepository, most of the search methods are EXACT meaning that a node has to have all labels EXCEPT:
 * <p>
 * findById -&gt; also works up the hierarchy and not just for the exact match of Labels.
 * findBy -&gt; auto-qualifies fields. Ex. Namespace "example" field "field" the full qualifier
 * would be "example_field". If you use ".findBy("field"...) it will automatically
 * replace it with "example_field", but if you use "xxx_field" it WONT be changed.
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class NamespaceAwareReflectiveNeo4JNodeRepositoryImpl<S> extends ReflectiveNeo4JNodeRepositoryImpl<S> {

    public NamespaceAwareReflectiveNeo4JNodeRepositoryImpl(TransactionManager manager, Class<S> clazz) {
        super(manager);
        this.clazz = clazz;

        // load class infos
        this.info = ClassInformation.constructClassInformation(this.clazz, true);

        // loosen the requirements on find by id to find anything starting from the root in the hierarchy if there is a hierarchy
        int hierarchy = this.info.getNsLabels().lastIndexOf(':');
        if (hierarchy > -1) {
            this.FIND_BY_ID_STATEMENT = "MATCH (n:" + this.info.getNsLabels().substring(hierarchy + 1) + ") WHERE ID(n) = $id OPTIONAL MATCH (n)-[r]->(c) RETURN {root: n, relationships: collect(distinct r), nodes: collect(distinct c)}";
        }

        // initialize repository -> this should happen last, as we might need to modify statements
        this.init(info.getNsName(), info.getNsLabels());
    }

    @Override
    public S findBy(String field, String value) {
        field = qualify(field);
        return super.findBy(field, value);
    }

    @Override
    public Iterable<S> findAllBy(String field, String value) {
        field = qualify(field);
        return super.findAllBy(field, value);
    }

    @Override
    public S findBy(List<Pair<String, Object>> values) {
        values.forEach(x -> x.setKey(qualify(x.getKey())));
        return super.findBy(values);
    }

    @Override
    public Iterable<S> findAllBy(List<Pair<String, Object>> values) {
        values.forEach(x -> x.setKey(qualify(x.getKey())));
        return super.findAllBy(values);
    }

    @Override
    public S findSubtree(Long id, int depth, List<String> relationships) {
        List<String> qualifiedRelationships = new ArrayList<>();
        if (relationships != null) {
            relationships.forEach(x -> qualifiedRelationships.add(qualifyLabel(x)));
        }
        return super.findSubtree(id, depth, relationships == null ? null : qualifiedRelationships);
    }

    protected String qualifyLabel(String label) {
        // if qualified leave it alone
        if (label.contains("_")) {
            return label;
        }
        return qualifyRecursive(label, this.info, new LinkedList<>());
    }

    private String qualifyRecursive(String label, ClassInformation classInformation, List<ClassInformation> alreadyTried) {
        String maybel = classInformation.getRelationships().keySet().stream().filter(x -> x.endsWith(label)).findFirst().orElse(null);
        if (maybel != null) {
            return maybel;
        }

        for (RelationshipInformation information : classInformation.getRelationships().values()) {
            ClassInformation tci = information.getTargetClassInformation();
            if (!alreadyTried.contains(tci)) {
                alreadyTried.add(tci);
                String sublabel = qualifyRecursive(label, tci, alreadyTried);
                if (!sublabel.equals(label)) {
                    return sublabel;
                }
            }
        }

        return label;
    }

    private String qualify(String field) {
        // if qualified leave it alone
        if (field.contains("_")) {
            return field;
        }
        // otherwise select by name
        return info.getFields().keySet().stream().filter(x -> x.endsWith("_" + field)).findFirst().orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setRelationshipOverrides(Map<String, Class> overrides) {
        Map<String, Class> nsOverrides = new HashMap<>();

        // add namespace if user didn't
        overrides.forEach((key, value) -> {
            if (!key.contains("_")) {
                nsOverrides.put(this.info.getNamespace() + "_" + key, value);
            } else {
                nsOverrides.put(key, value);
            }
        });

        super.setRelationshipOverrides(nsOverrides);
    }
}

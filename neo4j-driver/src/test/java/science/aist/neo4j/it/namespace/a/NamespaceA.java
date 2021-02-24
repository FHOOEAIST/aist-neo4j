package science.aist.neo4j.it.namespace.a;

import science.aist.neo4j.it.namespace.RootNode;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a class in a namespace extending the root namespace from {@link RootNode}
 * <p>
 * As there is no namespace annotation this class has the labels "test-RootNode"
 * and "science.machinelearning.analytics.graph.it.namespace.a-NamespaceA" in the Database
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class NamespaceA extends RootNode {

    /**
     * Field that exists only in namespace a
     */
    private String a;

    /**
     * Field that exists in multiple namespaces but should NOT lead to collisions
     */
    private String general;

    private Map<String, Object> additiveProperties = new HashMap<>();

    public NamespaceA() {
    }

    public NamespaceA(String rootField, String a, String general) {
        super(rootField);
        this.a = a;
        this.general = general;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getGeneral() {
        return general;
    }

    public void setGeneral(String general) {
        this.general = general;
    }

    public Map<String, Object> getAdditiveProperties() {
        return additiveProperties;
    }

    public void setAdditiveProperties(Map<String, Object> additiveProperties) {
        this.additiveProperties = additiveProperties;
    }
}

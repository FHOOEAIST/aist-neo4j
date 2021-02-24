package science.aist.neo4j.it.diffspace.c;

import science.aist.neo4j.it.namespace.RootNode;

/**
 * This class represents a class in a namespace extending the root namespace from {@link RootNode}
 * Unlike Namespace A it lies in a completely different namespace that still extends the original namespace
 * <p>
 * As there is no namespace annotation this class has the labels "test-RootNode"
 * and "science.machinelearning.analytics.graph.it.diffspace.c-NamespaceC" in the Database
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class NamespaceC extends RootNode {

    /**
     * Field that exists only in namespace c
     */
    private String c;

    /**
     * Field that exists in multiple namespaces but should NOT lead to collisions
     */
    private String general;

    public NamespaceC() {
    }

    public NamespaceC(String rootField, String c, String general) {
        super(rootField);
        this.c = c;
        this.general = general;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getGeneral() {
        return general;
    }

    public void setGeneral(String general) {
        this.general = general;
    }
}

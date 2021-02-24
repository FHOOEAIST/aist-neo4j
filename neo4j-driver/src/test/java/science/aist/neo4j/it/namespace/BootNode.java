package science.aist.neo4j.it.namespace;


import science.aist.neo4j.it.nodes.BnalyticsNode;

/**
 * Child of Root Node that uses BNalyticsNodes instead of AnalyticsNodes
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class BootNode extends RootNode {

    public BootNode() {
    }

    public BootNode(String rootField) {
        super(rootField);
    }

    public BnalyticsNode getAnalytics() {
        return (BnalyticsNode) super.getAnalytics();
    }

    public void setAnalytics(BnalyticsNode analytics) {
        super.setAnalytics(analytics);
    }

    public void addDynamicTypeChecker(BnalyticsNode bnalyticsNode) {
        super.getDynamicTypeChecker().add(bnalyticsNode);
    }

}

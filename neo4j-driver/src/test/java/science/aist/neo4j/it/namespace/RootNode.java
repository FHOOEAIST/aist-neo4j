package science.aist.neo4j.it.namespace;

import science.aist.neo4j.it.nodes.AnalyticsNode;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a class in a root namespace.
 * As the package-info has a Namespace annotation this class is "test-RootNode" in the Database
 *
 * @author Oliver Krauss
 * @since 1.0
 */
@NodeEntity
public class RootNode {

    /**
     * Id generated by database
     */
    private Long id;

    /**
     * Field that exists in the root class
     */
    private String rootField;

    @Relationship // no override
    private AnalyticsNode analytics;

    @Relationship(type = "ANALYTICS") // override
    private AnalyticsNode theOtherAnalytics;

    @Relationship(type = "DYNAMICTYPETEST")
    private List<AnalyticsNode> dynamicTypeChecker = new ArrayList<>();

    public RootNode() {
    }

    public RootNode(String rootField) {
        this.rootField = rootField;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRootField() {
        return rootField;
    }

    public void setRootField(String rootField) {
        this.rootField = rootField;
    }

    public AnalyticsNode getAnalytics() {
        return analytics;
    }

    public void setAnalytics(AnalyticsNode analytics) {
        this.analytics = analytics;
    }

    public AnalyticsNode getTheOtherAnalytics() {
        return theOtherAnalytics;
    }

    public void setTheOtherAnalytics(AnalyticsNode theOtherAnalytics) {
        this.theOtherAnalytics = theOtherAnalytics;
    }

    public List<AnalyticsNode> getDynamicTypeChecker() {
        return dynamicTypeChecker;
    }

    public void setDynamicTypeChecker(List<AnalyticsNode> dynamicTypeChecker) {
        this.dynamicTypeChecker = dynamicTypeChecker;
    }
}
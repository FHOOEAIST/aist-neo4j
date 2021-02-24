package science.aist.neo4j.it.nodes;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Base node that is the parent of an execution
 *
 * @author Oliver Krauss
 * @since 1.0
 */
@NodeEntity
public class AnalyticsNode {

    /**
     * Id generated by database
     */
    @Id
    private Long id;

    /**
     * Human readable title of Analytics run
     */
    private String title;

    /**
     * Parameters how an algorithm was configured, dependent on algorithm used during run
     */
    private Map<String, Object> parameters = new HashMap<>();

    public AnalyticsNode() {
    }

    public AnalyticsNode(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String name, Object value) {
        parameters.put(name, value);
    }
}

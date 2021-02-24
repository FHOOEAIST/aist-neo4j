package science.aist.neo4j.reflective;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * RelationshipInformation handles the metadata for the {@link ReflectiveNeo4JNodeRepositoryImpl}
 * It contains specific information on relationships between science.neo4j.nodes
 *
 * @author Oliver Krauss
 * @since 1.0
 */
public class RelationshipInformation extends FieldInformation {

    /**
     * The name of the relationship for the database
     */
    protected String type;

    /**
     * the direction of the relationship // TODO #9 and #10 USE THIS
     */
    protected Direction direction;

    /**
     * True = 0..*
     * False = 0..1
     * If it is a Collection is a bulk relationship!
     */
    protected boolean bulk;

    protected ClassInformation targetClassInformation;

    protected RelationshipInformation(String name, Field field, Method getter, Method setter, Class genericClass, String type, String direction) {
        super(name, field, getter, setter, genericClass);

        // per default the fieldname is equal to the fieldClass, if not specified by the user
        if (type == null || type.isEmpty()) {
            type = name;
        }

        this.type = type;
        this.direction = Direction.valueOf(direction);

        if (Collection.class.isAssignableFrom(field.getType())) {
            this.bulk = true;
        }
    }

    public RelationshipInformation(String name, Field field, Method getter, Method setter, Class genericClass, String type, String direction, Type genericType) throws ClassNotFoundException {
        this(name, field, getter, setter, genericClass, type, direction);

        if (genericClass != null) {
            this.targetClassInformation = ClassInformation.constructClassInformation(genericClass);
            return;
        }
        if (field.getGenericType() != null && field.getType().equals(Object.class)) {
            // generic type. make a snap decision at runtime
            return;
        }

        if (bulk) {
            String typeName = ((ParameterizedType) genericType).getActualTypeArguments()[0].getTypeName();
            if (typeName.contains("<")) {
                typeName = typeName.substring(0, typeName.indexOf('<'));
            }
            this.targetClassInformation = ClassInformation.constructClassInformation(Class.forName(typeName));
        } else {
            this.targetClassInformation = ClassInformation.constructClassInformation(this.fieldClass);
        }
    }

    /**
     * The direction of the relationship.
     * Note that Neo4J only supports directed relationships. Undirected will default to OUTGOING from the class that is defining the field.
     *
     * @author Oliver Krauss .
     */
    public enum Direction {
        INCOMING("INCOMING"),
        OUTGOING("OUTGOING"),
        UNDIRECTED("UNDIRECTED");

        /**
         * numeric value of enum
         */
        private String val;

        Direction(String val) {
            this.val = val;
        }

        /**
         * @return the numeric value of the enumerable
         */
        public String getVal() {
            return val;
        }
    }

    public String getType() {
        return type;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isBulk() {
        return bulk;
    }

    public ClassInformation getTargetClassInformation() {
        return targetClassInformation;
    }

    /**
     * For runtime decision making -&gt; hand it an object, and the system will get the class info from it
     *
     * @param o the object for which the class information should be constructed
     * @return the class information for the class of object o
     */
    public ClassInformation getTargetClassInformation(Object o) {
        if (targetClassInformation == null) {
            this.targetClassInformation = ClassInformation.constructClassInformation(o.getClass());
        }
        return targetClassInformation;
    }


    protected void setTargetClassInformation(ClassInformation targetClassInformation) {
        this.targetClassInformation = targetClassInformation;
    }
}

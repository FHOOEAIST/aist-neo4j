package science.aist.neo4j.it.dynamic.domain;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Objects;

/**
 * <p>Dummy testclass</p>
 *
 * @author Christoph Praschl
 * @since 1.0
 */
@NodeEntity
public class B {
    @Id
    protected Long id;

    /**
     * gets value of field {@link B#id}
     *
     * @return value of field id
     * @see B#id
     */
    public Long getId() {
        return id;
    }

    protected int someInt;

    protected String someString;

    public B() {
    }

    public B(int someInt, String someString) {
        this.someInt = someInt;
        this.someString = someString;
    }

    /**
     * Generated Code
     *
     * @param o value to compare
     * @return true if values are equals else false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        B b = (B) o;
        return someInt == b.someInt &&
            Objects.equals(someString, b.someString);
    }

    /**
     * Generated Code
     *
     * @return hashCode for the object
     */
    @Override
    public int hashCode() {

        return Objects.hash(someInt, someString);
    }

    /**
     * Generated Code
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "B{" +
            "id=" + id +
            ", someInt=" + someInt +
            ", someString='" + someString + '\'' +
            '}';
    }
}

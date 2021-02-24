package science.aist.neo4j.it.dynamic.domain;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Objects;

/**
 * <p>Dummy testclass</p>
 *
 * @author Christoph Praschl
 * @since 1.0
 */
@NodeEntity
public class D extends B {
    @Relationship
    private E e;

    public D() {
    }

    public D(int someInt, String someString) {
        super(someInt, someString);
    }

    public D(int someInt, String someString, E e) {
        super(someInt, someString);
        this.e = e;
    }

    /**
     * gets value of field {@link D#e}
     *
     * @return value of field e
     * @see D#e
     */
    public E getE() {
        return e;
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
        if (!super.equals(o)) return false;
        D d = (D) o;
        return Objects.equals(e, d.e);
    }

    /**
     * Generated Code
     *
     * @return hashCode for the object
     */
    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), e);
    }

    /**
     * Generated Code
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "D{" +
            "e=" + e +
            ", id=" + id +
            ", someInt=" + someInt +
            ", someString='" + someString + '\'' +
            '}';
    }
}

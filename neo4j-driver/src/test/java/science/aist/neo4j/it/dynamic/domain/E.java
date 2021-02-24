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
public class E {
    @Id
    private Long eId;

    private int eInt;

    public E(int eInt) {
        this.eInt = eInt;
    }

    public E() {
    }

    /**
     * gets value of field {@link E#eId}
     *
     * @return value of field eId
     * @see E#eId
     */
    public Long geteId() {
        return eId;
    }

    /**
     * sets value of field {@link E#eId}
     *
     * @param eId value of field eId
     * @see E#eId
     */
    public void seteId(Long eId) {
        this.eId = eId;
    }

    /**
     * gets value of field {@link E#eInt}
     *
     * @return value of field eInt
     * @see E#eInt
     */
    public int geteInt() {
        return eInt;
    }

    /**
     * sets value of field {@link E#eInt}
     *
     * @param eInt value of field eInt
     * @see E#eInt
     */
    public void seteInt(int eInt) {
        this.eInt = eInt;
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
        E e = (E) o;
        return eInt == e.eInt;
    }

    /**
     * Generated Code
     *
     * @return hashCode for the object
     */
    @Override
    public int hashCode() {

        return Objects.hash(eInt);
    }

    /**
     * Generated Code
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "E{" +
            "eId=" + eId +
            ", eInt=" + eInt +
            '}';
    }
}

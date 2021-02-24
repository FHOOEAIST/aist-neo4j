package science.aist.neo4j.it.dynamic.domain;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;
import java.util.Objects;

/**
 * <p>Dummy testclass</p>
 *
 * @author Christoph Praschl
 * @since 1.0
 */
@NodeEntity
public class A {
    @Id
    protected Long id;

    @Relationship
    private List<B> bs;

    public A() {
    }

    /**
     * gets value of field {@link A#id}
     *
     * @return value of field id
     * @see A#id
     */
    public Long getId() {
        return id;
    }

    /**
     * gets value of field {@link A#bs}
     *
     * @return value of field bs
     * @see A#bs
     */
    public List<B> getBs() {
        return bs;
    }

    /**
     * sets value of field {@link A#bs}
     *
     * @param bs value of field bs
     * @see A#bs
     */
    public void setBs(List<B> bs) {
        this.bs = bs;
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
        A a = (A) o;
        return Objects.equals(bs, a.bs);
    }

    /**
     * Generated Code
     *
     * @return hashCode for the object
     */
    @Override
    public int hashCode() {

        return Objects.hash(bs);
    }

    /**
     * Generated Code
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "A{" +
            "id=" + id +
            ", bs=" + bs +
            '}';
    }
}

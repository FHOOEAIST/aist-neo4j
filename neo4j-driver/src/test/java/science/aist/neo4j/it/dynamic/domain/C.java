package science.aist.neo4j.it.dynamic.domain;

import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Objects;

/**
 * <p>Dummy testclass</p>
 *
 * @author Christoph Praschl
 * @since 1.0
 */
@NodeEntity
public class C extends B {

    public C() {
    }

    public C(int someInt, String someString) {
        super(someInt, someString);
    }

    public C(int someInt, String someString, String cString) {
        super(someInt, someString);
        this.cString = cString;
    }

    private String cString;

    /**
     * gets value of field {@link C#cString}
     *
     * @return value of field cString
     * @see C#cString
     */
    public String getcString() {
        return cString;
    }

    /**
     * sets value of field {@link C#cString}
     *
     * @param cString value of field cString
     * @see C#cString
     */
    public void setcString(String cString) {
        this.cString = cString;
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
        C c = (C) o;
        return Objects.equals(cString, c.cString);
    }

    /**
     * Generated Code
     *
     * @return hashCode for the object
     */
    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), cString);
    }

    /**
     * Generated Code
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "C{" +
            "cString='" + cString + '\'' +
            ", id=" + id +
            ", someInt=" + someInt +
            ", someString='" + someString + '\'' +
            '}';
    }
}

package science.aist.neo4j.it.namespace.a;

import science.aist.neo4j.namespace.annotations.ExtensionField;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.List;

/**
 * @author Oliver Krauss
 * @since 1.0
 */
@NodeEntity
public class NodeWithOtherExtensions {

    /**
     * Id generated by database
     */
    private Long id;

    /**
     * Field that exists in the root class
     */
    private String fieldInClass;

    @ExtensionField(name = "singular")
    private String singular;

    /**
     * Field that exists in the root class
     */
    @ExtensionField(name = "random_extension_sameNameDifferentExtension")
    private String extendingField;

    /**
     * Field that exists in the root class
     */
    @ExtensionField(name = "random_extension_collection")
    private List<String> extendingCollectionWithDifferentFieldname;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFieldInClass() {
        return fieldInClass;
    }

    public void setFieldInClass(String fieldInClass) {
        this.fieldInClass = fieldInClass;
    }

    public String getExtendingField() {
        return extendingField;
    }

    public void setExtendingField(String extendingField) {
        this.extendingField = extendingField;
    }

    public List<String> getExtendingCollectionWithDifferentFieldname() {
        return extendingCollectionWithDifferentFieldname;
    }

    public void setExtendingCollectionWithDifferentFieldname(List<String> extendingCollectionWithDifferentFieldname) {
        this.extendingCollectionWithDifferentFieldname = extendingCollectionWithDifferentFieldname;
    }

    public String getSingular() {
        return singular;
    }

    public void setSingular(String singular) {
        this.singular = singular;
    }

}
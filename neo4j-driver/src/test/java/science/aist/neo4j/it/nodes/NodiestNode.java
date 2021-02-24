package science.aist.neo4j.it.nodes;

import science.aist.neo4j.annotations.Converter;
import science.aist.neo4j.it.converter.OtherPointConverter;
import science.aist.neo4j.it.converter.PointListConverter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Krauss
 * @since 1.0
 */
public class NodiestNode {

    private Long id;

    private String description;

    private int integerTest;

    private Integer integerComplexTest;

    private Map<String, String> mapString;

    private Map<Class, List<List<Class>>> hardMap;

    private List<Class>[] typedArray;

    private LinkedList<String> listString;

    private String[] arrayString;

    private float[] simpleDatatypeArray;

    // We override all possible classes
    @Converter(overrides = List.class, converter = PointListConverter.class)
    @Converter(overrides = Point.class, converter = OtherPointConverter.class)
    private List<Point> points;

    // We don't override list here, just point. This selects the regular converter for list, but still injects PointConverter
    @Converter(overrides = Point.class, converter = OtherPointConverter.class)
    private List<Point> pointsOnlyChildConveter;

    @Converter(converter = OtherPointConverter.class)
    private Point point;

    public NodiestNode() {
    }

    public NodiestNode(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIntegerTest() {
        return integerTest;
    }

    public void setIntegerTest(int integerTest) {
        this.integerTest = integerTest;
    }

    public Integer getIntegerComplexTest() {
        return integerComplexTest;
    }

    public void setIntegerComplexTest(Integer integerComplexTest) {
        this.integerComplexTest = integerComplexTest;
    }

    public List<String> getListString() {
        return listString;
    }

    public void setListString(LinkedList<String> listString) {
        this.listString = listString;
    }

    public String[] getArrayString() {
        return arrayString;
    }

    public void setArrayString(String[] arrayString) {
        this.arrayString = arrayString;
    }

    public Map<String, String> getMapString() {
        return mapString;
    }

    public void setMapString(Map<String, String> mapString) {
        this.mapString = mapString;
    }

    public Map<Class, List<List<Class>>> getHardMap() {
        return hardMap;
    }

    public void setHardMap(Map<Class, List<List<Class>>> hardMap) {
        this.hardMap = hardMap;
    }

    public List<Class>[] getTypedArray() {
        return typedArray;
    }

    public void setTypedArray(List<Class>[] typedArray) {
        this.typedArray = typedArray;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public List<Point> getPointsOnlyChildConveter() {
        return pointsOnlyChildConveter;
    }

    public void setPointsOnlyChildConveter(List<Point> pointsOnlyChildConveter) {
        this.pointsOnlyChildConveter = pointsOnlyChildConveter;
    }

    public float[] getSimpleDatatypeArray() {
        return simpleDatatypeArray;
    }

    public void setSimpleDatatypeArray(float[] simpleDatatypeArray) {
        this.simpleDatatypeArray = simpleDatatypeArray;
    }
}

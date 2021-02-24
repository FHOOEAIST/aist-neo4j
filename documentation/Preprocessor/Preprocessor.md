# Neo4J Preprocessor

Notice: The preprocessor is only relevant for the Namespace related parts of the neo4j driver. If you don't use namespaces you don't need to read this.

Warning: Several issues address optimizations that afford storing non-synced relationships. As soon as this is implemented the preprocessor is a requirement for ALL projects using our driver.

## Usage

The preprocessor is a maven plugin that is intended for usage in the commpile process of any project using it. It modifies your compiled files with mmeta-fields required by the namespace aware driver. ALL classes that are NamespaceAware Node entitites MUST be modified by this preproessor.

Copy the following into your mvn build:

```xml
<plugin>
    <groupId>science.aist.neo4j</groupId>
    <artifactId>neo4j-preprocessor</artifactId>
    <version>1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <id>processNeo4JClasses</id>
            <phase>process-test-classes</phase>
            <goals>
                <goal>namespace</goal>
            </goals>
            <configuration>
                <packageList>YOURPACKAGE1,YOURPACKAGE2</packageList>
            </configuration>
        </execution>
    </executions>
</plugin>
```

the following options for a configuration exist:
**packages REQUIRED** -> All packages that contain node classes. Separated by commas. Autoamtically contains all sub-packages.

**outputDir OPTIONAL** -> Comma separated list of all directories that contain .class files. Per default takes the reular compile the folder from maven as well as the test-classes folder, so your unittests work automatically.   

## Info for Implementers

There is only one class which handles all the javassist magic. In the far future this will probably use a more generic javassist compiler plugin, for now this handles everything we need though.

The Mojo collects all class files, looks if they are in the correct namespace(s), and adds the two Neo4J synchronization fields to the class. It does not currently check if the class actually is a neo4j entity, as the annotations are optional, and we can't find out which classes are entities.

## Testing

Testing the preprocessor without the namespace aware repository implementation is not really possible, aside from just checking if the classes have the generated fields.

Thus, there are no tests in this module. The tests happen implicitly in the Neo4J Driver, with the namespace related tests.

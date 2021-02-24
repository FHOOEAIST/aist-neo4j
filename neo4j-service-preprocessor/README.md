# Neo4ServicePreprocessor
The Neo4j Preprocessor can be used to automatically add transaction management to service methods.
Therefore, a few requirements are necessary.

## Downloading the Maven Plugin

The Preprocessor is a maven plugin. If you have built it on your PC -> nice. If you just want to use it you have to update your settings XML, as maven plugins are loaded from a separate repository (`<pluginRepositories>`) than regular java projects `<repositories>`).


Add the `<pluginRepositories>` which can be found in [the current Nexus documentation in the collaboration conventions](https://aist.fh-hagenberg.at/git/Internal/collaborationconventions/-/blob/master/docs/DevelopmentNexus.md) to your settings.xml, below the `<repositories>` tag.

## Configuring the Pom of the Service-Module
At first there is the need to configure the pom-file of the module where the service classes are located.
Therefore, you need to add the following plugin to your pom-file and replace the `$$$ Enter Package Name here $$$`
part with the package name where the service-files are. e.g. `science.aist.neo4j.service`. You can also
specify multiple package-names by separating them with a comma.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>science.aist.neo4j</groupId>
            <artifactId>neo4j-service-preprocessor</artifactId>
            <version>1.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <id>processNeo4JClasses</id>
                    <phase>process-classes</phase>
                    <goals>
                        <goal>transaction-service</goal>
                    </goals>
                    <configuration>
                        <packageList>
                           $$$ Enter Package Name here $$$
                        </packageList>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

It is sometime also required to add the plugin as a dependency first (I could not figure out, when this is required,
in some scenarios it worked for me by only adding the plugin configuration).

```xml
<dependency>
    <groupId>science.aist.neo4j</groupId>
    <artifactId>neo4j-service-preprocessor</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Configure the service classes
Once you configured your module, you are ready to add the transaction management to the service classes.
Therefore, you have to make sure, to have an injected TransactionManager in your service class.

Example:
```java
public class DummyService {
  private TransactionManager txManager;

  @Transaction(mode = Transaction.Mode.WRITE, transactionManager = "txManager")
  public void runInTransaction() {
    // code which should run in a write transaction goes here.
  }

  @Transaction(mode = Transaction.Mode.READ, transactionManager = "txManager")
  public Object runInTransaction2() {
    // code which should run in a read transaction goes here.
  }
}
```

If all your repositories are using the same instance of the TransactionManager (make sure to have
it correctly configured in your spring configuration).

The  has two properties that can be defined. The first one is the `mode` where
you can configure if you want to use Write or Read-Mode. The default value for the mode is "Write". The second one `transactionManager` specifies
the name of the transaction manager that got injected into the service. The default value for transactionManager is
"transactionManager".

Once you have configured everything, you are ready to go. As everything only works via Maven as the plugin goes
executed at a specific life-cycle phase, you only can use it after running a `mvn install` as this recompiles
the file to the transaction managed code.

## What happens in the background
What happens when you execute the code, is that the maven plugins runs a Mojo which recompile the classes
using javassist and adds the transaction management code to it.

The body of the code, that was written by yourself, will be moved to another method called $OldMethodName$Body
and a new method with $OldMethodName$ will be created and have all the transaction code in it, and finally calls
the $OldMethodName$Body, where the code, that was implemented by yourself gets executed.

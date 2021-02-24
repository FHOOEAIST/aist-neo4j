# Testing

Neo4J can only be tested with a running Neo4J DB (integrationtests only). If you have your own locally running, you can just run the tests out of IntelliJ or with mvn from the commandline.

Notice: IF you run the tests in IntelliJ you MUST add "clean install -DskipTests=true" to the run configuration, so the preprocessor runs, and the namespaceaware tests don't fail.

If you don't want to start your own Neo4J server, just run from the console with the **localdocker** profile active.

## Tips and Tricks

For resetting the DB between tests there is an easy trick. The Query function allows you to run any cypher query, including cleaning the db:
```java
repo.query("MATCH (n) detach delete n", null);
```

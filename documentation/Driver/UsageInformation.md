# Using the Neo4J driver

## SetUp

To use the driver simply include it as a maven dependency:
```xml
<dependency>
  <groupId>science.aist.neo4j</groupId>
  <artifactId>neo4j-driver</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

## (Spring) Configuration

The simple thing you need to remember is: **One NodeClass == One Repository**, whereas a NodeClass is a java class that should be represented as nodes in the Neo4J Database.

First of all you need a connection to your database:
```xml
<bean id="authToken" class="org.neo4j.driver.v1.AuthTokens" factory-method="basic">
    <constructor-arg name="username" value="USER"/>
    <constructor-arg name="password" value="PASSWORD"/>
</bean>

<bean id="driver" class="org.neo4j.driver.v1.GraphDatabase" factory-method="driver" destroy-method="close">
    <constructor-arg type="java.lang.String" value="bolt://IP:PORT"/>
    <constructor-arg ref="authToken"/>
</bean>
```

If you aren't using the provided repository implementation (see [here](TechnicalOverview.md) on how to make your own), you can skip the rest of this section.

For each of your classes that you want to handle you MUST create the following configuration:
```xml
<bean id="REPONAME"
      class="science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl">>
    <qualifier type="NODECLASS"/>
    <constructor-arg value="NODECLASS"/>
    <property name="driver" ref="driver"/>
    <property name="repositories">
        <util:map map-class="java.util.HashMap" key-type="java.lang.Class"
                  value-type="science.aist.neo4j.reflective.ReflectiveNeo4JNodeRepositoryImpl">
            <entry key="REFCLASS"
                   value-ref="REFREPOSITOROY"/>
            <entry key="REFCLASS2"
                   value-ref="REFREPOSITORY2"/>
        </util:map>
    </property>
    <property name="relationshipOverrides">
        <util:map map-class="java.util.HashMap" key-type="java.lang.String"
                  value-type="java.lang.Class">
            <entry key="RELATIONSHIPNAME" value="OVERRIDDENCLASS"/>
        </util:map>
    </property>
</bean>
```

**NODECLASS** is the fully qualified name (namespace + classname) of the class the repository handles. Technically you only need the constructor-arg for this to work. But, you definitely want to also set the qualifier to this, as spring as injects a Neo4JNodeRepository<NODECLASS> instead of an unqualified one where everything is Object.

The **repositories** property references all other repositories this repository needs. For every **relationship** the **NODECLASS** contains you MUST reference a repositoroy that can handle the target class of this relationship. The **REFCLASS** is once again the fully qualified name of the class.

The **relationshipOverrides** are an optional configuration for odd relationships. It is mostly relevant for the following use cases:
- You have a Relationship of java.lang.Object as you handle multiple classes in different contexts.
- You have a subclass that overrides the parent-class field with a more specific version of the relationship.

With the **relationshipOverrides** you tell the repository -> Use this class for this relationship. If you need to handle multiple classes at once, you need to create multiple repositories for each one.  

## Transaction Management

Transactions "per default" work out of the box. Any write/read is transactional. If you need to chain multiple Transactions together you can do one of the following:

Manual Management:
```java
try(Transaction tx = transactionManager.beginTransaction()) {
  ...
  tx.commit();
}
```

Automated with Annotation:
```java
@Transaction // provided by neo4j-service-preprocessor
public void myTransactionalFN() {
    // everything in here is in one transaction
}
```

### Results are NOT available outside of a transaction scope

Starting with Neo4J 4 results are discarded outside of a transaction. IF you need the results outside a transaction you need to copy them over. This can be achieved with the class `ResultClone`.

## DataModel and Annotations

The data model is built so that any java class can be a neo4j class. It only has one formal requirement **The class MUST have an ID**

There exist only two different kinds of Neo4J Entities. **Node Classes** and **Relationship Classes**

### ID

The id of a class has the following reqirements:
- It MUST be a **long** or **java.lang.Long**
- It MUST NOT be static
- It MUST NOT be final
- It MUST be one of the follwing:
  - Named **id**
  - Have an **@Id** Annotation

Examples of valid ids:
```java
private long id;

public Long id;

@Id
private long iDontWantToNameThisFieldIdBeccauseIHaveToBeComplicated;
```

### Fields

Any regular field will be included in the database. However, fields **only support simple types**. A field that references a java class must use the @Relationship annotation.

#### Supported Datatypes & Collections

Because there are tons of simple datatypes in java we simply assume that all datatypes that do not have the @Relationship annotation are a simple datatype. Thus, we support ALL simple and boxed datatypes that exist in Java.

In addition to the simple datatypes the driver supports:
- Arrays
- Maps
- Collections

The only requirements for them are that you **use simple datatypes** and do **not use the @Relationship** annotation.

#### Excluding fields @Transient

Any field with the @Transient annotation will NOT be synced to the DB.

Static fields won't be synced at all.

#### Including static fields @StaticField

Static fields will only be syced if they have the @StaticField or the @Relationship annotation.

### Relationships @Relationship

Any field can be a relationship field, as long as the field references a **NodeClass** OR a **RelationshipEntity**.

**0..1 or 1..1** - field with NON SIMPLE CLASS.
**0..\* or 1..*** - Collection or Array with NON SIMPLE CLASS

#### Relationships with Fields @RelationshipEntity

WARNING: AVOID Relationships with fields at all cost. They are computationally expensive, and a bitch to maintain.

Any class that is marked as @RelationshipEntity is automatically a Relationship and NOT a NodeClass. It can be used just like any other class in NodeClasses, but you must still use the @Relationship annotation for them.

You **cannot use @Relationship** in these entities. Neo4J does not support relationships between more than 2 nodes.

You MUST define a **@StartNode and @EndNode** field. Both of them must reference a NodeClass (NOT a RelationshipEntity), cannot be null and cannot be simple types.

Otherwise, RelationshipEntity classes handle exactly like NodeClasses, so you can define fields of simple types (also with Map, Array, ...).

### Specifying Labels, Multi-Labelling

You can optionally annotate any NodeClass with @NodeEntity (which is generally good behaviour). This allows you to **override the label**:

```Java
@NodeEntity(label = "LABEL")
public class SomeNodeClass {
...
}
```

If you don't set the label, it will automatically be the classname (also when you don't use the annotation).

If you have a class hierarchy, the driver will use labels for ALL classes that have the annotation, but skip all that don't.

Ex.:
```java
@NodeEntity
public class A ...

public class B extends A ...

@NodeEntity(label = "COOL")
public class C extends B ...
```

For the three classes above they will be stored by their respective repositories with the following labels:
- A -> A (A is used as the label is not overridden)
- B -> A,B (B is used automatically as it is the repository class)
- C -> A,COOL (note that B is skipped)

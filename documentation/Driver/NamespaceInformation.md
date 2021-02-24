# Using the Driver with Namespaces

Warning: The driver supports mixing Namespaeaware repositories with regular repositories that don't understand them. In some edge-cases that may be useful, however I STRONGLY suggest you don't mix and match, as this can screw up your data model irreparably / is extremely hard to debug. Also, the namespace unaware repositories can't handle the other scopes, so if you load a node with an unaware repository and store it to the db again it will LOSE all namespace related information.

Note: this section expands upon [the regular driver](UsageInformation.md). Anything not mentioned here stays the same in usage.

## Setup and Configuration

The Namespace aware driver implementation is intentionally similar to the regular implementation, the only difference is that you have to use a different repository class:

```
NamespaceAwareReflectiveNeo4JNodeRepositoryImpl
```

## Namespaces

You may be familira with the concept of a namespace from XML, and in this project it is fairly similar. A namespace denotes one scope of an usecase / application / project, etc. Like that you may require that this namespace is in the same database as different namespaces. You may also need the capability of modifying a namespace, referencing it or expanding upon it.

For our driver the idea is: **one java package == one namespace**. This is not 100% accurate as you may require your DB / namespace to be different from the actual implementation or just map to differently named concepts. This actually makes the golden rule for namespaces: **one unique identification == one namespace** which SHOULD also be the java package.

In Neo4J the idea of a namespace is represented through the labels. Any node that has a label without the **_ delimiter** is NOT in a namespace. Any node with a label that has the delimiter is automatically in a namespace. EACH of these delimiters describes a sub-namespace, which in Java packages often means that the parent namespaces are empty. What this means for you is that you **MUST NOT use _ in the name of any label in the database** as this denotes a namespace.

Otherwise, namespaces are an implicit concept, only grouped by these labels. The repositories capable of loading namespaces will only load the parts of the nodes that are defined in the namespace you configured them with. Essentially this means that our Neo4J DB implements polymorphism, while the Java Driver will only give you the view of one namespace.

### Defining a namespaces

If you do nothing, every package is automatically a namespace, while every sub-package is a sub-namespace. You can override this and combine packages into one namespace, or just rename it to something nicer with the **package-info.java**

```java
@Namespace(ns="OVERRIDE")
package science.aist.neo4j.it.namespace;

import Namespace;
```

This automatically forces all classes in this package into the namespace OVERRIDE.

What is important to note about namespaces is that all fields in a namespace are automatically unique. You can define one field of class X in Namespace A and the same field in Namespace B and store them on the same node, and it will work.

### Dealing with nodes that are in more than one namespace

Easy. Just write different classes that both have the same root. Any fields that should be shared between namespaces need to be in the root class.

### Dealing with a namespace structure where DB and Code don't match

Use the annotation **@ExtendedNode(parent= "NAMESPACE_CLASS")**. This automatically defines this class as a subclass of another namespace. No superclass in Java required.

## Using the repositories

Works the same as the non-namespace aware ones. The repository fills in all namespace related information for you autoatically. However sometimes you may want to do something in Namespace A based on the information in Namespace B. All the queries actually work with ALL fields in ALL namespaces the nodes implement.

Ex. You want to find all Nodes of Namespace B that share a value in Namespace A:

```java
bRepository.findBy("A_field", "VALUE")
```

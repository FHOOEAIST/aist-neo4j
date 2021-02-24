# Technical Overview of the driver

This page contains a rough outline of the "driver" module for anyone who contributes in it.

## Architecture & Core Concepts

The project handles two core concerns of a db driver:
- Defining the data model.
- Creating the repository for database operations

## The data model

The neo4j datamodel is built on the concept of an OR mapper. The idea is that java classes should be useable for Neo4J with as little effort as possible.

Both for confiburability and edge cases we define annotations which should always be in the annotation package.

In addition to our own annotations we support:
- @Id to mark a field as the DB id (always long)
- @Transient to exclude a field from the DB
- @NodeEntity to override the label

### OR Mapping

Is handled by ClassInformation, which looks through the classes with reflection, and collects all fields (also of parent classes). Fields with the @RelationshipAnnotation are marked as relationships, and get a RelationshipInformation that handles Directionality and Labels. Regular fields get the @FieldAnnotation.

The ClassInformation also contains functionality to map from DB objects to Java objects and reverse.

## The Repositories

There base implementation of the Repository are the Abstract repositories. The intent is that this base implementation contains all Cypher Statements you need with Labels where the implementing classes need to modify the statements for themselves.

The Abstract Repository should do as much as possible WITHOUT knowing anything about the nodes it maintains.

The reflective repositories utiize our OR Mapping approach to automatically load the equired information and modify the statements of the abstract repository. 

### Creating your own Repository Implementation

Don't do that. But, if you absolutely must, the entire hierarchy of the repositories are intended for reusability.

If you simply need to add additional features you are best of extending the Reflective Repositories or (even better) the NamespaceAware Reflective Repositories.

If in doubt always extend the NamespaeAware ones. They have additional functionality, but they are less performant than the ones without namespace handling.

Note: if you totally go off the rails and just implement the interface you will also have to handle the OR mapping. Good luck.

![logo](./documentation/logo.png)

# Aist Neo4J

Aist Neo4j is an object to graph mapper (OGM) Java library for the [Neo4j graph database](https://neo4j.com/).
It  allows reading and writing objects from/to a Neo4j graph database and for this it supports extended features as namespaces.

## Getting Started

The base information on how to get started can be found [here](./documentation/Driver/UsageInformation.md)

### Driver

The complete technical documentation is [here](./documentation/Readme.md)

### Preprocessor - for Namespaces and Extended functionality

Note: to use extended functionality such as the following you need to include the preprocessor.
```
@Relationship
Map<List<String>,Map<String,OtherNodeClass>> crazyRelationship;
```

This preprocessor also requires an additional configuration of your settings.xml file as detailed in the documentation [here](./neo4j-service-preprocessor/README.md).

## FAQ

- What is the difference to [Spring Data Neo4j](https://spring.io/projects/spring-data-neo4j)?
  - Spring Data Neo4j is as well an object to graph mapper for accessing a Neo4j database from Spring applications. The Spring Data connector executes a lot of sanity checks and is for this far slower then our implementation. In addition to that we support additional features as namespaces.
- What are namespaces?
  - Namespaces are a concept to group information. You may know this concept from XML. We introduced this concept to graph databases to dynamically extend persistet information. Like this we also support multiple inheritance in our database model and are so able to only load the required information in the application with minimal memory consumption in the database.

## Contributing

**First make sure to read our [general contribution guidelines](https://fhooeaist.github.io/CONTRIBUTING.html).**
   
## Licence

Copyright (c) 2020 the original author or authors.
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES.

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at https://mozilla.org/MPL/2.0/.

## Research

If you are going to use this project as part of a research paper, we would ask you to reference this project by citing
it. 

--TODO zenodo doi--
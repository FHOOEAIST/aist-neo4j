# Neo4J Driver

The neo4j driver is our own proprietary implementation, including a rudimentary OR mapping for convenience. Our own benchmarks show massive improvements over the Spring implementation, primarily because we don't do security or sanity checks at all.

The documentation is split in the following parts:
- [Usage Information](UsageInformation.md) for the driver itself
- [Namespace Information](NamespaceInformation.md) for when you need to apply namespaces to the data model
- [Technical Info](./TechnicalOverview.md) for developers, which is a minimalistic overview in addition to the in-code documentation
- [Testing](./Testing.md) contains info about the db tests.

package cz.matfyz.tests.example.common;

import cz.matfyz.abstractwrappers.database.Database.DatabaseType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.Config;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrappermongodb.MongoDBSettings;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.matfyz.wrapperneo4j.Neo4jProvider;
import cz.matfyz.wrapperneo4j.Neo4jSettings;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLSettings;
import cz.matfyz.wrappercassandradb.CassandraControlWrapper;
import cz.matfyz.wrappercassandradb.CassandraProvider;
import cz.matfyz.wrappercassandradb.CassandraSettings;

public class DatabaseProvider {

    private final Config CONFIG;

    public DatabaseProvider(String namespace) {
        this.CONFIG = new Config(namespace);
    }

    // PostgreSQL

    private PostgreSQLProvider postgreSQLProvider;

    public PostgreSQLProvider getPostgreSQLProvider() {
        if (postgreSQLProvider == null) {
            postgreSQLProvider = new PostgreSQLProvider(new PostgreSQLSettings(
                CONFIG.getBool("isInDocker") ? "mmcat-postgresql" : "localhost",
                CONFIG.getBool("isInDocker") ? "5432" : "3204",
                CONFIG.get("database"),
                CONFIG.get("username"),
                CONFIG.get("password")
            ));
        }

        return postgreSQLProvider;
    }

    public TestDatabase<PostgreSQLControlWrapper> createPostgreSQL(SchemaCategory schema, String setupFileName) {
        final var wrapper = new PostgreSQLControlWrapper(getPostgreSQLProvider());
        return new TestDatabase<>(DatabaseType.postgresql, wrapper, schema, setupFileName);
    }

    // MongoDB

    private MongoDBProvider mongoDBProvider;

    public MongoDBProvider getMongoDBProvider() {
        if (mongoDBProvider == null) {
            mongoDBProvider = new MongoDBProvider(new MongoDBSettings(
                CONFIG.getBool("isInDocker") ? "mmcat-mongodb" : "localhost",
                CONFIG.getBool("isInDocker") ? "27017" : "3205",
                "admin",
                CONFIG.get("database"),
                CONFIG.get("username"),
                CONFIG.get("password")
            ));
        }

        return mongoDBProvider;
    }

    public TestDatabase<MongoDBControlWrapper> createMongoDB(SchemaCategory schema, String setupFileName) {
        final var wrapper = new MongoDBControlWrapper(getMongoDBProvider());
        return new TestDatabase<>(DatabaseType.mongodb, wrapper, schema, setupFileName);
    }

    // Neo4j

    private Neo4jProvider neo4jProvider;

    public Neo4jProvider getNeo4jProvider() {
        if (neo4jProvider == null) {
            neo4jProvider = new Neo4jProvider(new Neo4jSettings(
                CONFIG.getBool("isInDocker") ? "mmcat-neo4j" : "localhost",
                CONFIG.getBool("isInDocker") ? "7687" : "3206",
                "neo4j",
                "neo4j",
                CONFIG.get("password")
            ));
        }

        return neo4jProvider;
    }

    public TestDatabase<Neo4jControlWrapper> createNeo4j(SchemaCategory schema, String setupFileName) {
        final var wrapper = new Neo4jControlWrapper(getNeo4jProvider());
        return new TestDatabase<>(DatabaseType.neo4j, wrapper, schema, setupFileName);
    }

    // Cassandra

    private CassandraProvider cassandraProvider;

    public CassandraProvider getCassandraProvider() {
        if (cassandraProvider == null) {
            cassandraProvider = new CassandraProvider(new CassandraSettings(
                CONFIG.getBool("isInDocker") ? "mmcat-cassandra" : "localhost",
                CONFIG.getBool("isInDocker") ? 9042 : 3208,
                CONFIG.get("database"),
                CONFIG.get("username"),
                CONFIG.get("password"),
                "datacenter1"
            ));
        }

        return cassandraProvider;
    }

    public TestDatabase<CassandraControlWrapper> createCassandra(SchemaCategory schema, String setupFileName) {
        final var wrapper = new CassandraControlWrapper(getCassandraProvider());
        return new TestDatabase<>(DatabaseType.cassandra, wrapper, schema, setupFileName);
    }
}

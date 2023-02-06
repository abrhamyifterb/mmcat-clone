package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.cuni.matfyz.wrappermongodb.MongoDBDatabaseProvider;
import cz.cuni.matfyz.wrappermongodb.MongoDBSettings;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLConnectionProvider;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLSettings;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
@Service
public class WrapperService {

    public AbstractControlWrapper getControlWrapper(Database database) {
        try {
            return switch (database.type) {
                case mongodb -> getMongoDBControlWrapper(database);
                case postgresql -> getPostgreSQLControlWrapper(database);
                default -> throw new WrapperNotFoundException(wrapperNotFoundText("Pull", database));
            };
        }
        catch (Exception exception) {
            throw new WrapperCreationErrorException("Pull wrapper for database " + database.id + " with JSON settings: " + database.settings + " can't be created due to following exception: " + exception.getMessage());
        }
    }

    private String wrapperNotFoundText(String name, Database database) {
        return name + "wrapper for database " + database.id + " with JSON settings: " + database.settings + " not found.";
    }

    private Map<Id, MongoDBDatabaseProvider> mongoDBCache = new TreeMap<>();

    private MongoDBControlWrapper getMongoDBControlWrapper(Database database) throws IllegalArgumentException, JsonProcessingException {
        if (!mongoDBCache.containsKey(database.id))
            mongoDBCache.put(database.id, createMongoDBProvider(database));

        final var provider = mongoDBCache.get(database.id);
        return new MongoDBControlWrapper(provider);
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    private static MongoDBDatabaseProvider createMongoDBProvider(Database database) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(database.settings, MongoDBSettings.class);

        return new MongoDBDatabaseProvider(settings);
    }

    private Map<Id, PostgreSQLConnectionProvider> postgreSQLCache = new TreeMap<>();

    private PostgreSQLControlWrapper getPostgreSQLControlWrapper(Database database) throws IllegalArgumentException, JsonProcessingException {
        if (!postgreSQLCache.containsKey(database.id))
            postgreSQLCache.put(database.id, createPostgreSQLProvider(database));

        final var provider = postgreSQLCache.get(database.id);
        return new PostgreSQLControlWrapper(provider);
    }

    private static PostgreSQLConnectionProvider createPostgreSQLProvider(Database database) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(database.settings, PostgreSQLSettings.class);

        return new PostgreSQLConnectionProvider(settings);
    }

    public static class WrapperCreationErrorException extends RuntimeException {

        public WrapperCreationErrorException(String errorMessage) {
            super(errorMessage);
        }

    }

    public static class WrapperNotFoundException extends RuntimeException {

        public WrapperNotFoundException(String errorMessage) {
            super(errorMessage);
        }

    }

}

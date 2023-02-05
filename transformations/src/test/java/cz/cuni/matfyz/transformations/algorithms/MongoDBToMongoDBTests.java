package cz.cuni.matfyz.transformations.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.wrappermongodb.MongoDBDDLWrapper;
import cz.cuni.matfyz.wrappermongodb.MongoDBDMLWrapper;
import cz.cuni.matfyz.wrappermongodb.MongoDBDatabaseProvider;
import cz.cuni.matfyz.wrappermongodb.MongoDBPullWrapper;

import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class MongoDBToMongoDBTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBToMongoDBTests.class);

    private static final MongoDBDatabaseProvider mongodbProvider = DatabaseSetup.createMongoDBDatabaseProvider();

    @BeforeAll
    public static void setupMongoDB() {
        try {
            var url = ClassLoader.getSystemResource("setupMongodb.js");
            String pathToFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            DatabaseSetup.executeMongoDBScript(pathToFile);
        }
        catch (Exception exception) {
            LOGGER.error("MongoDB setup error: ", exception);
        }
    }

    private static MongoDBPullWrapper createPullWrapper() {
        return new MongoDBPullWrapper(mongodbProvider);
    }

    private PullToDDLAndDMLTestBase testBase;

    @BeforeEach
    public void setupTestBase() {
        testBase = new PullToDDLAndDMLTestBase(createPullWrapper(), new MongoDBDDLWrapper(), new MongoDBDMLWrapper());
    }

    @Test
    public void basicTest() {
        var data = new TestData();
        var schema = data.createDefaultSchemaCategory();
        var order = schema.getObject(data.orderKey);
        
        testBase.setAll(
            "TODO",
            schema,
            order,
            "basic",
            data.path_orderRoot()
        );

        testBase.testAlgorithm();
    }

    @Test
    public void test() throws Exception {
        final var data = new TestData();
        final ComplexProperty path = data.path_orderRoot();
        LOGGER.trace(path.toString());

        final var jsonString = new ObjectMapper().writer().writeValueAsString(path);
        LOGGER.trace(jsonString);

        final AccessPath parsedPath = new ObjectMapper().readerFor(AccessPath.class).readValue(jsonString);
        LOGGER.trace(parsedPath.toString());

        assertEquals(path.toString(), parsedPath.toString());
    }

    @Test
    public void complex_arrayTest() {
        var data = new TestData();
        var schema = data.createDefaultSchemaCategory();
        var order = schema.getObject(data.orderKey);
        
        testBase.setAll(
            "TODO",
            schema,
            order,
            "complex_array",
            data.path_items()
        );

        testBase.testAlgorithm();
    }
}

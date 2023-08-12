package cz.matfyz.tests.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import cz.matfyz.core.instance.InstanceCategoryBuilder;
import cz.matfyz.core.utils.io.FileInputStreamProvider;
import cz.matfyz.core.utils.io.UrlInputStreamProvider;
import cz.matfyz.integration.algorithms.JsonLdToRDF;
import cz.matfyz.integration.algorithms.RDFToInstance;
import cz.matfyz.server.builder.SchemaCategoryContext;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.service.SchemaCategoryService;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author jachymb.bartik
 */
@SpringBootTest
public class IntegrationTests {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTests.class);
    
    static final String JSON_LD_FILE_NAME = "test2.jsonld";
    //static final String JSON_LD_FILE_NAME = "test1.jsonld";
    static final String JSON_LD_URL = "http://localhost/data-sources/test2.jsonld";
    //static final String JSON_LD_URL = "http://localhost/data-sources/test1.jsonld";
    static final Id CATEGORY_ID = new Id("4");
    //static final Id CATEGORY_ID = new Id("7");

    @Autowired
    private SchemaCategoryService categoryService;

    @Test
    public void jsonLdToRdfFromLocalFile_DoesNotThrow() {
        final var jsonToRDF = new JsonLdToRDF();
        jsonToRDF.input(new FileInputStreamProvider(JSON_LD_FILE_NAME));

        assertDoesNotThrow(() -> {
            final var dataset = jsonToRDF.algorithm();
            printDataset(dataset);
        });
    }

    @Test
    public void jsonLdToRdfFromRemoteUrl_DoesNotThrow() {
        final var jsonToRDF = new JsonLdToRDF();
        jsonToRDF.input(new UrlInputStreamProvider(JSON_LD_URL));

        assertDoesNotThrow(() -> {
            final var dataset = jsonToRDF.algorithm();
            printDataset(dataset);
        });
    }

    @Test
    public void RDFToInstance_DoesNotThrow() {
        final var rdfToInstance = new RDFToInstance();

        final var jsonToRDF = new JsonLdToRDF();
        jsonToRDF.input(new FileInputStreamProvider(JSON_LD_FILE_NAME));

        final var schemaCategoryWrapper = categoryService.find(CATEGORY_ID);
        final var context = new SchemaCategoryContext();
        final var schemaCategory = schemaCategoryWrapper.toSchemaCategory(context);
        final var instanceCategory = new InstanceCategoryBuilder().setSchemaCategory(schemaCategory).build();

        assertDoesNotThrow(() -> {
            final var dataset = jsonToRDF.algorithm();
            rdfToInstance.input(dataset, instanceCategory);
            rdfToInstance.algorithm();
        });

        System.out.println(instanceCategory);
    }

    private void printDataset(Dataset dataset) {
        final var builder = new StringBuilder();

        addModel(builder, dataset.getDefaultModel());
        dataset.listModelNames().forEachRemaining(resource -> addModel(builder, dataset.getNamedModel(resource.getURI())));

        System.out.println(builder.toString());
    }

    private void addModel(StringBuilder builder, Model model) {
        model.listStatements().forEach(statement -> builder.append(statement).append("\n"));
    }

}
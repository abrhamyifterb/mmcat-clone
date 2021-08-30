package de.hda.fbi.modules.schemaextraction.configuration;

import de.hda.fbi.modules.schemaextraction.common.LastExtractedSchema;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionUseCase;

import java.util.List;

public class SchemaExtractionConfiguration {

    private String databaseName;

    private DatabaseConfiguration databaseConfiguration;

    private List<String> entityTypes;

    private SchemaExtractionUseCase useCase;

    private String timestampIdentifier;

    private Object lastExtractedTimestamp;

    private List<LastExtractedSchema> lastExtractedSchemas;

    public SchemaExtractionConfiguration(DatabaseConfiguration databaseConfiguration, String databaseName,
                                         List<String> entityTypes, SchemaExtractionUseCase useCase, String timestampIdentifier,
                                         Object lastExtractedTimestamp, List<LastExtractedSchema> lastExtractedSchemas) {
        this.setDatabaseConfiguration(databaseConfiguration);
        this.setDatabaseName(databaseName);
        this.setEntityTypes(entityTypes);
        this.setUseCase(useCase);
        this.setTimestampIdentifier(timestampIdentifier);
        this.setLastExtractedTimestamp(lastExtractedTimestamp);
        this.setLastExtractedSchemas(lastExtractedSchemas);
    }

    public DatabaseConfiguration getDatabaseConfiguration() {
        return databaseConfiguration;
    }

    public void setDatabaseConfiguration(DatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public List<String> getEntityTypes() {
        return entityTypes;
    }

    public void setEntityTypes(List<String> entityTypes) {
        this.entityTypes = entityTypes;
    }

    public SchemaExtractionUseCase getUseCase() {
        return useCase;
    }

    public void setUseCase(SchemaExtractionUseCase useCase) {
        this.useCase = useCase;
    }

    public String getTimestampIdentifier() {
        return timestampIdentifier;
    }

    public void setTimestampIdentifier(String timestampIdentifier) {
        this.timestampIdentifier = timestampIdentifier;
    }

    public Object getLastExtractedTimestamp() {
        return lastExtractedTimestamp;
    }

    public void setLastExtractedTimestamp(Object lastExtractedTimestamp) {
        this.lastExtractedTimestamp = lastExtractedTimestamp;
    }

    public List<LastExtractedSchema> getLastExtractedSchemas() {
        return lastExtractedSchemas;
    }

    public void setLastExtractedSchemas(List<LastExtractedSchema> lastExtractedSchemas) {
        this.lastExtractedSchemas = lastExtractedSchemas;
    }

}
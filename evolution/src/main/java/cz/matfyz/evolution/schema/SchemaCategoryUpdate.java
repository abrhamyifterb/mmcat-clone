package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.Version;

import java.util.List;

public class SchemaCategoryUpdate {

    private final Version prevVersion;

    public Version getPrevVersion() {
        return prevVersion;
    }

    private final List<SchemaModificationOperation> operations;

    public SchemaCategoryUpdate(Version prevVersion, List<SchemaModificationOperation> operations) {
        this.prevVersion = prevVersion;
        this.operations = operations;
    }

    public SchemaCategory apply(SchemaCategory category) {
        for (final var operation : operations)
            operation.apply(category);

        return category;
    }

}
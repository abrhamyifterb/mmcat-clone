package cz.matfyz.server.example.queryevolution;

import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.mapping.MappingInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.service.MappingService;
import cz.matfyz.tests.example.queryevolution.MongoDB;
import cz.matfyz.tests.example.queryevolution.PostgreSQL;
import cz.matfyz.server.example.common.MappingBuilder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("queryEvolutionMappingSetup")
class MappingSetup {

    @Autowired
    private MappingService mappingService;

    List<MappingInfo> createMappings(List<LogicalModel> models, SchemaCategoryWrapper schemaWrapper, int version) {
        final var builder = new MappingBuilder(models, schemaWrapper);

        if (version == 3) {
            return builder
                .add(1, MongoDB::orders)
                .build(mappingService::createNew);
        }

        builder
            .add(0, PostgreSQL::customer)
            .add(0, PostgreSQL::knows)
            .add(0, PostgreSQL::product);

        if (version == 1) {
            builder.add(0, PostgreSQL::orders);
        }
        else if (version == 2) {
            builder
                .add(0, PostgreSQL::order)
                .add(0, PostgreSQL::ordered)
                .add(0, PostgreSQL::item);
        }
        else if (version == 4) {
            builder.add(1, MongoDB::order);
        }

        return builder.build(mappingService::createNew);
    }

}

package cz.matfyz.server.service;

import cz.matfyz.server.builder.SchemaCategoryContext;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.evolution.SchemaUpdate;
import cz.matfyz.server.entity.evolution.SchemaUpdateInit;
import cz.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.MetadataUpdate;
import cz.matfyz.server.repository.SchemaCategoryRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
@Service
public class SchemaCategoryService {

    @Autowired
    private SchemaCategoryRepository repository;

    public List<SchemaCategoryInfo> findAllInfos() {
        return repository.findAllInfos();
    }

    public SchemaCategoryInfo createNewInfo(SchemaCategoryInit init) {
        if (init.label() == null)
            return null;
        
        final var newWrapper = SchemaCategoryWrapper.createNew(init.label());
        final Id generatedId = repository.add(newWrapper);

        return generatedId == null ? null : new SchemaCategoryInfo(generatedId, init.label(), newWrapper.version);
    }

    public SchemaCategoryInfo findInfo(Id id) {
        return repository.findInfo(new Id("" + id));
    }

    public SchemaCategoryWrapper find(Id id) {
        return repository.find(id);
    }

    public SchemaCategoryWrapper update(Id id, SchemaUpdateInit updateInit) {
        final var wrapper = repository.find(id);
        final var update = SchemaUpdate.fromInit(updateInit, id);

        if (!update.prevVersion.equals(wrapper.version))
            return null;

        final var context = new SchemaCategoryContext();
        final var evolutionUpdate = update.toEvolution(context);
        final var originalCategory = wrapper.toSchemaCategory(context);
        updateInit.metadata().forEach(m -> context.setPosition(m.key(), m.position()));

        final var newCategory = evolutionUpdate.apply(originalCategory);

        final var nextVersion = context.getVersion().generateNext();
        context.setVersion(nextVersion);
        update.nextVersion = nextVersion;

        final var newWrapper = SchemaCategoryWrapper.fromSchemaCategory(newCategory, context);

        if (!repository.update(newWrapper, update))
            return null;

        return newWrapper;
    }

    public boolean updateMetadata(Id id, List<MetadataUpdate> metadataUpdates) {
        final var wrapper = repository.find(id);

        final var context = new SchemaCategoryContext();
        final var category = wrapper.toSchemaCategory(context);
        metadataUpdates.forEach(m -> context.setPosition(m.key(), m.position()));

        final var newWrapper = SchemaCategoryWrapper.fromSchemaCategory(category, context);

        return repository.updateMetadata(newWrapper);
    }
    
    public List<SchemaUpdate> findAllUpdates(Id id) {
        return repository.findAllUpdates(id);
    }
}
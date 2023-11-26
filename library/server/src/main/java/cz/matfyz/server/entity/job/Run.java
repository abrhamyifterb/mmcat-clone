package cz.matfyz.server.entity.job;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author jachym.bartik
 */
public class Run extends Entity {

    public final Id categoryId;
    /** The action that was used to generate this run. It might be null because not all runs are generated by user actions. */
    @Nullable
    public final Id actionId;

    private Run(Id id, Id categoryId, Id actionId) {
        super(id);
        this.categoryId = categoryId;
        this.actionId = actionId;
    }
    
    public static Run createNew(Id categoryId, Id actionId) {
        return new Run(
            Id.createNewUUID(),
            categoryId,
            actionId
        );
    }
        
    public static Run fromDatabase(Id id, Id categoryId, Id actionId) {
        return new Run(
            id,
            categoryId,
            actionId
        );
    }
        
}

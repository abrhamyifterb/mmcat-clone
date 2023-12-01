package cz.matfyz.server.entity.job;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.ActionPayload;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author jachym.bartik
 */
public class Job extends Entity {

    public enum State {
        /** The job can be started now. */
        Ready,
        /** The job is currently being processed. */
        Running,
        /** The job is finished, either with a success or with an error. */
        Finished,
        /** The job was canceled while being in one of the previous states. It can never be started (again). */
        Canceled,
        /** The job failed. */
        Failed,
    }

    public final Id runId;
    public final String label;
    /** The job contains all information needed to execute it. */
    public final ActionPayload payload;
    public State state;
    public @Nullable Serializable data;
    public final Date createdAt;

    private Job(Id id, Id runId, String label, Date createdAt, ActionPayload payload, State state, Serializable data) {
        super(id);
        this.runId = runId;
        this.label = label;
        this.createdAt = createdAt;
        this.payload = payload;
        this.state = state;
        this.data = data;
    }

    public static Job createNew(Id runId, String label, ActionPayload payload) {
        return new Job(
            Id.createNewUUID(),
            runId,
            label,
            new Date(),
            payload,
            State.Ready,
            null
        );
    }

    private static record JsonValue(
        String label,
        Date createdAt,
        ActionPayload payload,
        State state,
        @Nullable Serializable data
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static Job fromJsonValue(Id id, Id runId, String jsonValueString) throws JsonProcessingException {
        final JsonValue jsonValue = jsonValueReader.readValue(jsonValueString);

        return new Job(
            id,
            runId,
            jsonValue.label,
            jsonValue.createdAt,
            jsonValue.payload,
            jsonValue.state,
            jsonValue.data
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(
            label,
            createdAt,
            payload,
            state,
            data
        ));
    }
    
}
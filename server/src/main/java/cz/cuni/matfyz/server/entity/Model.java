package cz.cuni.matfyz.server.entity;

import java.io.Serializable;

/**
 * @author jachym.bartik
 */
public record Model(
    int jobId,
    int categoryId,
    String jobLabel,
    String commands
) implements Serializable {}

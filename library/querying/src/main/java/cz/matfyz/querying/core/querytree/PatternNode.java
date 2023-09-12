package cz.matfyz.querying.core.querytree;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.JoinCandidate;

import java.util.List;
import java.util.Set;

/**
 * This is the pattern part of a query part (GroupNode).
 * It contains triples and other data that are used by other elements of the query part (i.e., by filters).
 */
public class PatternNode extends QueryNode {
    
    // All kinds used in this pattern.
    public final Set<Kind> kinds;
    public final SchemaCategory schema;
    public final List<JoinCandidate> joinCandidates;

    public PatternNode(Set<Kind> kinds, SchemaCategory schema, List<JoinCandidate> joinCandidates) {
        this.kinds = kinds;
        this.schema = schema;
        this.joinCandidates = joinCandidates;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }

}

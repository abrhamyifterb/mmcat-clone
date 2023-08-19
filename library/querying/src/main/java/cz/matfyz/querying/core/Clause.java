package cz.matfyz.querying.core;

import cz.matfyz.core.mapping.Kind;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.parsing.WhereTriple;

import java.util.List;
import java.util.Set;

public class Clause {

    public static enum ClauseType {
        WHERE,
        OPTIONAL,
        UNION,
        MINUS,
    }

    public final ClauseType type;
    public final List<Clause> nestedClauses;
    public final List<WhereTriple> pattern;

    public Clause(ClauseType type, List<WhereTriple> pattern, List<Clause> nestedClauses) {
        this.type = type;
        this.pattern = pattern;
        this.nestedClauses = nestedClauses;
    }

    // These properties are gradually added as the query gets processed.
    public SchemaCategory schema = null;
    public List<KindDefinition> kinds = null;
    public Set<Kind> patternPlan;

}
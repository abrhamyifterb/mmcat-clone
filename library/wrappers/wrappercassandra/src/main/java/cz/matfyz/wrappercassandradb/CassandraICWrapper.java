package cz.matfyz.wrappercassandradb;

import java.util.Set;

import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.mapping.IdentifierStructure;
import cz.matfyz.core.utils.ComparablePair;

public class CassandraICWrapper implements AbstractICWrapper {

    @Override
    public void appendIdentifier(String kindName, IdentifierStructure identifier) {
        // TODO Auto-generated method stub
    }

    @Override
    public void appendReference(String kindName, String kindName2, Set<ComparablePair<String, String>> attributePairs) {
        // TODO Auto-generated method stub
    }

    @Override
    public AbstractStatement createICStatement() {
        return CassandraCQLStatement.createEmpty();
    }

    @Override
    public AbstractStatement createICRemoveStatement() {
        return CassandraCQLStatement.createEmpty();
    }
}
package cz.matfyz.wrappercassandradb;

import cz.matfyz.abstractwrappers.AbstractStatement;

public class CassandraCQLStatement implements AbstractStatement {

    private String content;

    public CassandraCQLStatement(String content) {
        this.content = content;
    }

    public String getCql() {
        return content;
    }

    @Override
    public String getContent() {
        // TODO Auto-generated method stub
        return content;
    }

    private static final CassandraCQLStatement empty = new CassandraCQLStatement("");

    public static CassandraCQLStatement createEmpty() {
        return empty;
    }
}


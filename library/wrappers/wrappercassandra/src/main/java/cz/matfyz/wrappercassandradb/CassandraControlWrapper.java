package cz.matfyz.wrappercassandradb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlSession;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.exception.ExecuteException;

public class CassandraControlWrapper implements AbstractControlWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraControlWrapper.class);
    private final CassandraProvider provider;

    public CassandraControlWrapper(CassandraProvider provider) {
        this.provider = provider;
    }

    @Override
    public void execute(Collection<AbstractStatement> statements) {
        CqlSession session = provider.getSession();
        for (AbstractStatement statement : statements) {
            try {
                if (statement instanceof CassandraCQLStatement cqlStatement) {
                    LOGGER.info("Execute Cassandra CQL statement:\n{}", cqlStatement.getCql());
                    session.execute(cqlStatement.getCql());
                }
            } catch (Exception e) {
                throw new ExecuteException(e, List.of(statement));
            }
        }
    }

    @Override public void execute(Path path) {
        try {
            String scripts = Files.readString(path);
            // Split the queries by the ; character, followed by any number of whitespaces and newline.
            final var statements = Stream.of(scripts.split(";\\s*\n"))
                .map(String::strip)
                .filter(script -> !script.isBlank())
                .map(script -> (AbstractStatement) new CassandraCQLStatement(script))
                .toList();

            execute(statements);
        }
        catch (IOException e) {
            throw new ExecuteException(e, path);
        }
    }

    @Override
    public CassandraDDLWrapper getDDLWrapper() {
        return new CassandraDDLWrapper();
    }

    @Override
    public CassandraDMLWrapper getDMLWrapper() {
        return new CassandraDMLWrapper();
    }

    @Override
    public AbstractICWrapper getICWrapper() {
        return new CassandraICWrapper();
    }

    @Override
    public AbstractPullWrapper getPullWrapper() {
        return new CassandraPullWrapper(provider);
    }

    @Override
    public AbstractPathWrapper getPathWrapper() {
        return new CassandraPathWrapper();
    }

    @Override
    public AbstractQueryWrapper getQueryWrapper() {
        return new CassandraQueryWrapper();
    }

}


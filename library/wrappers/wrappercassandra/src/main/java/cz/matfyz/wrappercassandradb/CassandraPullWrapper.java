package cz.matfyz.wrappercassandradb;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

public class CassandraPullWrapper implements AbstractPullWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraPullWrapper.class);
    private CassandraProvider provider;

    public CassandraPullWrapper(CassandraProvider provider) {
        this.provider = provider;
    }

    private PreparedStatement prepareStatement(CqlSession session, QueryContent query) {
        if (query instanceof StringQuery stringQuery) {
            return session.prepare(stringQuery.content);
        }

        if (query instanceof KindNameQuery kindNameQuery) {
            return session.prepare(kindNameQueryToString(kindNameQuery));
        }

        throw PullForestException.invalidQuery(this, query);
    }

    private String kindNameQueryToString(KindNameQuery query) {
        // TODO escape all table names globally
        var command = "SELECT * FROM " + "\"" + query.kindName + "\"";
        if (query.hasPartitionKey())
            command += "\nWHERE " + query.getPartitionKey() + " = '" + query.getPartitionValue() + "'";
        if (query.hasLimit())
            command += "\nLIMIT " + query.getLimit();
        command += ";";

        return command;
    }

    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        try (CqlSession session = provider.getSession()) {
            PreparedStatement statement = prepareStatement(session, query);
            LOGGER.info("Execute Cassandra query:\n{}", statement.getQuery());

            ResultSet resultSet = session.execute(statement.bind());
            ForestOfRecords forest = new ForestOfRecords();
            
            resultSet.forEach(row ->  {
                var rootRecord = new RootRecord();

                for (AccessPath subpath : path.subpaths()) {
                    if (subpath instanceof SimpleProperty simpleProperty && simpleProperty.name() instanceof StaticName staticName) {
                        String name = staticName.getStringName();
                        String value = row.getString(name);
                        rootRecord.addSimpleValueRecord(staticName.toRecordName(), simpleProperty.signature(), value);
                    }
                }

                forest.addRecord(rootRecord);
            });

            return forest;
        } catch (Exception e) {
            throw PullForestException.innerException(e);
        }
    }

    @Override public QueryResult executeQuery(QueryStatement query) {
        final List<String> columns = query.structure().children.values().stream().map(child -> child.name).toList();
    
        try (CqlSession session = provider.getSession()) {
            PreparedStatement preparedStatement = prepareStatement(session, query.content());
            LOGGER.info("Execute Cassandra query:\n{}", preparedStatement.getQuery());
    
            ResultSet resultSet = session.execute(preparedStatement.bind());
    
            ResultList.TableBuilder builder = new ResultList.TableBuilder();
            builder.addColumns(columns);
    
            resultSet.forEach(row -> {
                final var values = new ArrayList<String>();
                for (final var column : columns)
                    values.add(row.getString(column));

                builder.addRow(values);
            });
    
            return new QueryResult(builder.build(), query.structure());
        } catch (Exception e) {
            throw PullForestException.innerException(e);
        }
    }
    

    public String readTableAsStringForTests(String kindName) {
        try (CqlSession session = provider.getSession()) {
            ResultSet resultSet = session.execute("SELECT * FROM " + kindName + ";");
            StringBuilder output = new StringBuilder();
            for (Row row : resultSet) {
                output.append(row.getString("number")).append("\n");
            }

            return output.toString();
        }
    }
}
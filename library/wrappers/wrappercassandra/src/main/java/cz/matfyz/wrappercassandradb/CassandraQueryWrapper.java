package cz.matfyz.wrappercassandradb;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.querying.QueryStructure;

public class CassandraQueryWrapper extends BaseQueryWrapper implements AbstractQueryWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isJoinSupported() { return false; }
    @Override public boolean isOptionalJoinSupported() { return false; }
    @Override public boolean isRecursiveJoinSupported() { return false; }
    @Override public boolean isFilteringSupported() { return true; }
    @Override public boolean IsFilteringNotIndexedSupported() { return false; }
    @Override public boolean isAggregationSupported() { return true; }
    // CHECKSTYLE:ON

    @Override
    protected Map<ComparisonOperator, String> defineComparisonOperators() {
        final var output = new TreeMap<ComparisonOperator, String>();
        output.put(ComparisonOperator.Equal, "=");
        output.put(ComparisonOperator.NotEqual, "!=");
        output.put(ComparisonOperator.Less, "<");
        output.put(ComparisonOperator.LessOrEqual, "<=");
        output.put(ComparisonOperator.Greater, ">");
        output.put(ComparisonOperator.GreaterOrEqual, ">=");
        return output;
    }

    @Override
    protected Map<AggregationOperator, String> defineAggregationOperators() {
        // TODO Auto-generated method stub
        final var output = new TreeMap<AggregationOperator, String>();
        output.put(AggregationOperator.Count, "COUNT");
        output.put(AggregationOperator.Sum, "SUM");
        output.put(AggregationOperator.Min, "MIN");
        output.put(AggregationOperator.Max, "MAX");
        output.put(AggregationOperator.Average, "AVG");
        return output;
    }
    
    private StringBuilder builder;

    @Override
    public QueryStatement createDSLStatement() {
        builder = new StringBuilder();

        addSelect();
        addFrom();
        addWhere();

        return new QueryStatement(new StringQuery(builder.toString()), createStructure());
    }
    
    private void addSelect() {
        final String projectionsString = projections.stream().map(this::getProjection).collect(Collectors.joining(", "));
        builder.append("SELECT ").append(projectionsString).append("\n");
    }
 
    private void addFrom() {
        if (projections.size() == 0)
            throw QueryException.message("No tables are selected in FROM clause.");
        
        final String kindName = projections.get(0).property().kind.mapping.kindName();
        builder
            .append("FROM ")
            .append(escapeName(kindName))
            .append(" ");
    }

    private String escapeName(String name) {
        return "\"" + name + "\"";
    }

    private void addWhere() {
        if (filters.isEmpty())
            return;

        builder.append("WHERE ");
        addFilter(filters.get(0));

        filters.stream().skip(1).forEach(filter -> {
            builder.append("\nAND ");
            addFilter(filter);
        });
    }

    private void addFilter(Filter filter) {
        if (filter instanceof UnaryFilter unaryFilter)
            addUnaryFilter(unaryFilter);
        else if (filter instanceof BinaryFilter binaryFilter)
            addBinaryFilter(binaryFilter);

        builder.append("\n");
    }

    private void addUnaryFilter(UnaryFilter filter) {
        builder.append(getPropertyName(filter.property()));

        final var values = filter.constant().values();
        if (values.size() == 1) {
            builder
                .append(" ")
                .append(getComparisonOperatorValue(filter.operator()))
                .append(" '")
                .append(values.get(0))
                .append("'");

            return;
        }

        if (filter.operator() != ComparisonOperator.Equal)
            throw QueryException.message("Operator " + filter.operator() + " can't be used for multiple values.");

        builder
            .append(" IN (")
            .append(values.get(0));

        values.stream().skip(1).forEach(value -> builder.append(", ").append(value));

        builder.append(")");
    }

    private void addBinaryFilter(BinaryFilter filter) {
        builder
            .append(getPropertyName(filter.property1()))
            .append(getComparisonOperatorValue(filter.operator()))
            .append(getPropertyName(filter.property2()));
    }

    private String getProjection(Projection projection) {
        return escapeName(getRawAttributeName(projection.property()));
    }

    private String getPropertyName(Property property) {
        return escapeName(getRawAttributeName(property));
    }

    private String getRawAttributeName(Property property) {
        // Direct subpath is ok since the postgresql mapping must be flat.
        final var subpath = property.kind.mapping.accessPath().getDirectSubpath(property.path);
        if (
            subpath == null ||
            !(subpath instanceof SimpleProperty simpleSubpath) ||
            !(simpleSubpath.name() instanceof StaticName staticName)
        )
            throw QueryException.propertyNotFoundInMapping(property);

        return staticName.getStringName();
    }
    
    private QueryStructure createStructure() {
        final var root = new QueryStructure(rootIdentifier, true);
        projections.forEach(p -> {
            boolean isArray = isProjectionArray(p.property());
            root.addChild(new QueryStructure(p.identifier(), isArray));
        });
    
        return root;
    }
    
    private boolean isProjectionArray(Property property) {
        return false;
    }
}

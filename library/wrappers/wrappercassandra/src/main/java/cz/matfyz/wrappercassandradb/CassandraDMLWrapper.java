package cz.matfyz.wrappercassandradb;

import java.util.ArrayList;
import java.util.List;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.exception.InvalidNameException;

public class CassandraDMLWrapper implements AbstractDMLWrapper {

    private String kindName = null;
    private List<PropertyValue> propertyValues = new ArrayList<>();

    private record PropertyValue(String name, Object value) {}

    @Override
    public void setKindName(String name) {
        if (!nameIsValid(name))
            throw InvalidNameException.kind(name);

        this.kindName = name;
    }

    @Override
    public void append(String name, Object value) {
        if (!nameIsValid(name))
            throw InvalidNameException.property(name);

        String stringValue = value == null ? null : value.toString();
        propertyValues.add(new PropertyValue(name, stringValue));
    }

    private boolean nameIsValid(String name) {
        return name.matches("^[\\w]+$");
    }

    @Override
    public CassandraCQLStatement createDMLStatement() {
        if (kindName == null)
            throw InvalidNameException.kind(null);

        List<String> columnNames = propertyValues.stream().map(propertyValue -> propertyValue.name).toList();
        List<String> columnValues = propertyValues.stream().map(propertyValue -> formatValue(propertyValue.value)).toList();

        String cql = String.format("INSERT INTO %s (%s) VALUES (%s);",
                kindName,
                String.join(", ", columnNames),
                String.join(", ", columnValues));

        return new CassandraCQLStatement(cql);
    }

    private String formatValue(Object value) {
        if (value instanceof String) {
            return "'" + ((String) value).replace("'", "''") + "'";
        } else if (value == null) {
            return "null";
        } else {
            return value.toString();
        }
    }

    @Override
    public void clear() {
        kindName = null;
        propertyValues.clear();
    }
}

package cz.matfyz.wrappercassandradb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.exception.UnsupportedException;

public class CassandraDDLWrapper implements AbstractDDLWrapper {

    private String tableName;
    private final List<Property> properties = new ArrayList<>();
    private Set<String> primaryKeyColumns = new HashSet<>();

    @Override
    public void setKindName(String name) {
        this.tableName = name;
    }

    @Override
    public boolean isSchemaLess() {
        return false;
    }

    @Override
    public boolean addSimpleProperty(Set<String> names, boolean required) {
        names.forEach(name -> {
            String command = "\"" + name + "\" text";
            properties.add(new Property(name, command));
        });
        
        return true;
    }

    @Override
    public boolean addSimpleArrayProperty(Set<String> names, boolean required) {
        names.forEach(name -> {
            String command = "\"" + name + "\" list<text>";
            properties.add(new Property(name, command));
        });
        
        return true;
    }

    @Override
    public boolean addComplexProperty(Set<String> names, boolean required) throws UnsupportedException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addComplexProperty'");
    }

    @Override
    public boolean addComplexArrayProperty(Set<String> names, boolean required) throws UnsupportedException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addComplexArrayProperty'");
    }
    
    @Override
    public CassandraCQLStatement createDDLStatement() {

        String commands = String.join(",\n", properties.stream().map(property -> AbstractDDLWrapper.INDENTATION + property.command).toList());

        String primaryKeys = !primaryKeyColumns.isEmpty() ? 
        ",\n    PRIMARY KEY ((" + String.join(", ", primaryKeyColumns) + "))" : "";

        String content = String.format("""
            CREATE TABLE \"%s\" (
            %s%s
            );
            """, tableName, commands, primaryKeys);
        
        return new CassandraCQLStatement(content);
    }

    private record Property(String name, String command) {}

    @Override
    public void setPrimaryKey(Set<String> keyColumns) {
        // TODO Auto-generated method stub
        this.primaryKeyColumns = keyColumns;
    }
}

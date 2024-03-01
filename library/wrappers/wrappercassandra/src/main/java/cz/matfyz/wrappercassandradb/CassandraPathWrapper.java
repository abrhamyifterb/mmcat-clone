package cz.matfyz.wrappercassandradb;

import java.util.ArrayList;
import java.util.List;

import cz.matfyz.abstractwrappers.AbstractPathWrapper;

public class CassandraPathWrapper implements AbstractPathWrapper {
    private final List<String> properties = new ArrayList<>();

    @Override public void addProperty(String hierarchy) {
        // TODO Auto-generated method stub
        this.properties.add(hierarchy);
    }

    @Override public boolean check() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    // CHECKSTYLE:OFF
    @Override public boolean isPropertyToOneAllowed() { return true; }
    @Override public boolean isPropertyToManyAllowed() { return true; }
    @Override public boolean isInliningToOneAllowed() { return true; }
    @Override public boolean isInliningToManyAllowed() { return false; }
    @Override public boolean isGroupingAllowed() { return false; }
    @Override public boolean isDynamicNamingAllowed() { return false; }
    @Override public boolean isAnonymousNamingAllowed() { return false; }
    @Override public boolean isReferenceAllowed() { return false; }
    @Override public boolean isComplexPropertyAllowed() { return true; }
    @Override public boolean isSchemaLess() { return false; }
    // CHECKSTYLE:ON    
}

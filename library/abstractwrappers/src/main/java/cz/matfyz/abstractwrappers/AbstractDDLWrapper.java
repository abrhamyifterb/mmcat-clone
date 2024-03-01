package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.exception.UnsupportedException;
import cz.matfyz.core.mapping.StaticName;

import java.util.Set;

/**
 * @author pavel.koupil
 */
public interface AbstractDDLWrapper {

    static final String PATH_SEPARATOR = "/";
    static final String EMPTY_NAME = StaticName.createAnonymous().getStringName();
    static final String INDENTATION = "    ";

    void setKindName(String name);

    void setPrimaryKey(Set<String> keyColumns);

    boolean isSchemaLess();

    boolean addSimpleProperty(Set<String> names, boolean required) throws UnsupportedException;

    boolean addSimpleArrayProperty(Set<String> names, boolean required) throws UnsupportedException;

    boolean addComplexProperty(Set<String> names, boolean required) throws UnsupportedException;

    boolean addComplexArrayProperty(Set<String> names, boolean required) throws UnsupportedException;

    AbstractStatement createDDLStatement();

}

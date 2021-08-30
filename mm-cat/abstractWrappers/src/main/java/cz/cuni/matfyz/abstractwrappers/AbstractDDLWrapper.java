/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.statements.DDLStatement;
import java.util.Set;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractDDLWrapper {

	public abstract void setKindName(String name);

	public abstract boolean isSchemaLess();

	public abstract boolean addSimpleProperty(Set<String> names, boolean optional) throws UnsupportedOperationException;

	public abstract boolean addSimpleArrayProperty(Set<String> names, boolean optional) throws UnsupportedOperationException;

	public abstract boolean addComplexProperty(Set<String> names, boolean optional) throws UnsupportedOperationException;

	public abstract boolean addComplexArrayProperty(Set<String> names, boolean optional) throws UnsupportedOperationException;

	public abstract DDLStatement createDDLStatement();

}

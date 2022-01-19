package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;

/**
 * Simple property cannot have children so it is a leaf node in the record tree.
 * However, it can have value.
 * @author jachymb.bartik
 * @param <DataType> a type of the value of this property.
 */
public abstract class SimpleRecord<DataType> extends DataRecord
{
//	private final DataType value;
    protected final Signature signature;
    
//    SimpleRecord(Name name, ComplexRecord parent, DataType value, Signature signature)
    SimpleRecord(RecordName name, ComplexRecord parent, Signature signature)
    {
        super(name, parent);
        this.signature = signature;
    }
	
    /*
    public DataType getValue()
    {
        return value;
    }
    */
    
    public Signature signature()
    {
        return signature;
    }
    
    /*
    @Override
    public Set<DataRecord> records()
    {
        return Set.of(this);
    }
    */
    
    /*
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("(Name: \"").append(name)
            .append("\",\tValue: \"").append(value).append("\")\n");
        
        return builder.toString();
    }
*/
}

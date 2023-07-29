package cz.cuni.matfyz.querying.parsing;

public class StringValue extends QueryNode implements ValueNode {

    @Override StringValue asStringValue() {
        return this;
    }

    @Override ValueNode asValueNode() {
        return this;
    }

    public final String value;

    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StringValue wrapper && wrapper.value.equals(value);
    }

}
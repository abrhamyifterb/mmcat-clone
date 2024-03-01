package cz.matfyz.abstractwrappers.querycontent;

/**
 * @author jachym.bartik
 */
public class KindNameQuery implements QueryContent {

    public final String kindName;

    private final Integer offset;

    public int getOffset() {
        return offset;
    }

    public boolean hasOffset() {
        return offset != null;
    }

    private final Integer limit;

    public int getLimit() {
        return limit;
    }

    public boolean hasLimit() {
        return limit != null;
    }

    public KindNameQuery(String kindName, Integer limit, Integer offset) {
        this.kindName = kindName;
        this.limit = limit;
        this.offset = offset;

        this.partitionKey = "";
        this.partitionValue = "";
    }

    public KindNameQuery(String kindName) {
        this(kindName, null, null);
    }

    private final String partitionKey;
    private final String partitionValue;

    public KindNameQuery(String kindName, String partitionKey, String partitionValue, Integer limit, Integer offset) {
        this.kindName = kindName;
        this.partitionKey = partitionKey;
        this.partitionValue = partitionValue;
        this.limit = limit;
        this.offset = offset;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public String getPartitionValue() {
        return partitionValue;
    }

    public boolean hasPartitionKey() {
        return partitionKey != null && !partitionKey.isEmpty();
    }
}

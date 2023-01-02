package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.ObjectIds;
import cz.cuni.matfyz.core.schema.SignatureId;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jachymb.bartik
 */
public class SuperIdWithValues implements Serializable, Comparable<SuperIdWithValues>, JSONConvertible {

    private final Map<Signature, String> tuples;

    public boolean hasSignature(Signature signature) {
        return tuples.containsKey(signature);
    }
    
    public Set<Signature> signatures() {
        return tuples.keySet();
    }

    public String getValue(Signature signature) {
        return tuples.get(signature);
    }
    
    private SignatureId cachedId;
    
    public SignatureId id() {
        if (cachedId == null)
            cachedId = new SignatureId(tuples.keySet());
        return cachedId;
        // Evolution extension
        //return new Id(map.keySet());
    }

    public Collection<String> values() {
        return tuples.values();
    }

    public int size() {
        return tuples.size();
    }

    public boolean isEmpty() {
        return tuples.isEmpty();
    }

    public boolean containsId(SignatureId id) {
        for (var signature : id.signatures())
            if (!hasSignature(signature))
                return false;
        return true;
    }

    public SuperIdWithValues findId(SignatureId id) {
        var builder = new Builder();

        for (var signature : id.signatures()) {
            var value = this.tuples.get(signature);
            if (value == null)
                return null;
            builder.add(signature, value);
        }

        return builder.build();
    }

    public SuperIdWithValues findFirstId(ObjectIds ids) {
        for (var id : ids.toSignatureIds())
            if (containsId(id))
                return findId(id);

        return null;
    }

    public record FindIdsResult(Set<SuperIdWithValues> foundIds, Set<SignatureId> notFoundIds) {}

    /**
     * Returns all ids that are contained there as a subset.
     * @param signatureIds The ids we want to find.
     * @return A set of found ids and also not found ids.
     */
    public FindIdsResult findAllIds(ObjectIds ids) {
        return findAllSignatureIds(ids.toSignatureIds());
    }

    public FindIdsResult findAllSignatureIds(Set<SignatureId> ids) {
        final var foundIds = new TreeSet<SuperIdWithValues>();
        final var notFoundIds = new TreeSet<SignatureId>();

        for (SignatureId id : ids) {
            var foundId = findId(id);

            if (foundId == null)
                notFoundIds.add(id);
            else
                foundIds.add(foundId);
        }

        return new FindIdsResult(foundIds, notFoundIds);
    }

    // Evolution extension
    public SuperIdWithValues copy() {
        var mapCopy = new TreeMap<Signature, String>();
        this.tuples.forEach((signature, string) -> mapCopy.put(signature, string));
        return new SuperIdWithValues(mapCopy);
    }

    public static SuperIdWithValues merge(SuperIdWithValues... ids) {
        var builder = new Builder();
        for (var id : ids)
            builder.add(id);
        
        return builder.build();
    }

    public static SuperIdWithValues createEmpty() {
        return merge();
    }

    private SuperIdWithValues(Map<Signature, String> map) {
        this.tuples = map;
    }

    public static class Builder {

        private Map<Signature, String> map = new TreeMap<>();

        public Builder add(Signature signature, String value) {
            map.put(signature, value);
            return this;
        }

        public Builder add(SuperIdWithValues idWithValues) {
            for (var tuple : idWithValues.tuples.entrySet())
                map.put(tuple.getKey(), tuple.getValue());
                
            return this;
        }

        public SuperIdWithValues build() {
            var output = new SuperIdWithValues(map);
            map = new TreeMap<>();
            return output;
        }

    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SuperIdWithValues idWithValues))
            return false;

        return Objects.equals(this.tuples, idWithValues.tuples);
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.tuples);
        return hash;
    }
    
    @Override
    public int compareTo(SuperIdWithValues idWithValues) {
        int idCompareResult = id().compareTo(idWithValues.id());
        if (idCompareResult != 0)
            return idCompareResult;
        
        for (Signature signature : signatures()) {
            int signatureCompareResult = tuples.get(signature).compareTo(idWithValues.tuples.get(signature));
            if (signatureCompareResult != 0)
                return signatureCompareResult;
        }
        
        return 0;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("{");
        boolean notFirst = false;
        for (var entry : tuples.entrySet()) {
            if (notFirst)
                builder.append(", ");
            else
                notFirst = true;
            
            builder.append("(").append(entry.getKey()).append(": \"").append(entry.getValue()).append("\")");
        }
        builder.append("}");
            
        return builder.toString();
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<SuperIdWithValues> {

        @Override
        protected JSONObject innerToJSON(SuperIdWithValues object) throws JSONException {
            var output = new JSONObject();

            var tuples = new ArrayList<JSONObject>();
            
            for (var entry : object.tuples.entrySet()) {
                var jsonTuple = new JSONObject();
                jsonTuple.put("signature", entry.getKey().toJSON());
                jsonTuple.put("value", entry.getValue());

                tuples.add(jsonTuple);
            }

            output.put("tuples", new JSONArray(tuples));
            
            return output;
        }
    
    }

}
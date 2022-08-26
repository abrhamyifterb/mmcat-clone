package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.category.Signature.Type;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.IdWithValues;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.instance.MappingRow;
import cz.cuni.matfyz.core.instance.Merger;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.DynamicName;
import cz.cuni.matfyz.core.mapping.IContext;
import cz.cuni.matfyz.core.mapping.IValue;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.mapping.Name;
import cz.cuni.matfyz.core.mapping.SimpleValue;
import cz.cuni.matfyz.core.record.DynamicRecordName;
import cz.cuni.matfyz.core.record.DynamicRecordWrapper;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.record.IComplexRecord;
import cz.cuni.matfyz.core.record.RootRecord;
import cz.cuni.matfyz.core.record.SimpleArrayRecord;
import cz.cuni.matfyz.core.record.SimpleRecord;
import cz.cuni.matfyz.core.record.SimpleValueRecord;
import cz.cuni.matfyz.core.schema.Id;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pavel.koupil, jachym.bartik
 */
public class MTCAlgorithm {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MTCAlgorithm.class);

    private ForestOfRecords forest;
    private Mapping mapping;
    private InstanceCategory instance;
    
    public void input(Mapping mapping, InstanceCategory instance, ForestOfRecords forest) {
        this.forest = forest;
        this.mapping = mapping;
        this.instance = instance;
    }
    
    public void algorithm() {
        LOGGER.debug("Model To Category algorithm");
        final ComplexProperty rootAccessPath = mapping.accessPath().copyWithoutAuxiliaryNodes();

        // Create notes for adding to superId. // TODO edit name and comment
        instance.createNotes();

        for (RootRecord rootRecord : forest)
            processRootRecord(rootRecord, rootAccessPath);
    }

    private void processRootRecord(RootRecord rootRecord, ComplexProperty rootAccessPath) {
        LOGGER.debug("Process a root record:\n{}", rootRecord);

        // preparation phase
        Deque<StackTriple> masterStack = mapping.hasRootMorphism()
            ? createStackWithMorphism(mapping.rootObject(), mapping.rootMorphism(), rootRecord, rootAccessPath) // K with root morphism
            : createStackWithObject(mapping.rootObject(), rootRecord, rootAccessPath); // K with root object

        // processing of the tree
        while (!masterStack.isEmpty())
            processTopOfStack(masterStack);
    }
    
    private Deque<StackTriple> createStackWithObject(SchemaObject object, RootRecord rootRecord, ComplexProperty rootAccessPath) {
        InstanceObject qI = instance.getObject(object);
        IdWithValues sid = fetchSid(object.superId(), rootRecord);
        Deque<StackTriple> masterStack = new LinkedList<>();
        
        DomainRow row = modifyActiveDomain(qI, sid);
        addPathChildrenToStack(masterStack, rootAccessPath, row, rootRecord);
        
        return masterStack;
    }
    
    private Deque<StackTriple> createStackWithMorphism(SchemaObject object, SchemaMorphism morphism, RootRecord rootRecord, ComplexProperty rootAccessPath) {
        Deque<StackTriple> masterStack = new LinkedList<>();
        
        InstanceObject qI_dom = instance.getObject(object);
        IdWithValues sids_dom = fetchSid(object.superId(), rootRecord);
        DomainRow sid_dom = modifyActiveDomain(qI_dom, sids_dom);

        SchemaObject qS_cod = morphism.cod();
        IdWithValues sids_cod = fetchSid(qS_cod.superId(), rootRecord);
        
        InstanceObject qI_cod = instance.getObject(qS_cod);
        DomainRow sid_cod = modifyActiveDomain(qI_cod, sids_cod);

        InstanceMorphism mI = instance.getMorphism(morphism);

        addRelation(mI, sid_dom, sid_cod, rootRecord);
        addRelation(mI.dual(), sid_cod, sid_dom, rootRecord);

        AccessPath t_dom = rootAccessPath.getSubpathBySignature(Signature.createEmpty());
        AccessPath t_cod = rootAccessPath.getSubpathBySignature(morphism.signature());

        AccessPath ap = rootAccessPath.minusSubpath(t_dom).minusSubpath(t_cod);

        addPathChildrenToStack(masterStack, ap, sid_dom, rootRecord);
        addPathChildrenToStack(masterStack, t_cod, sid_cod, rootRecord);
        
        return masterStack;
    }
    
    private void processTopOfStack(Deque<StackTriple> masterStack) {
        LOGGER.debug("Process Top of Stack:\n{}", masterStack);
        
        StackTriple triple = masterStack.pop();
        Iterable<FetchedSuperId> sids = fetchSids(triple.parentRecord, triple.parentRow, triple.parentToChildMorphism);

        SchemaObject childObject = triple.parentToChildMorphism.cod();
        InstanceObject childInstance = instance.getObject(childObject);
        InstanceMorphism morphismInstance = instance.getMorphism(triple.parentToChildMorphism);
        
        for (FetchedSuperId sid : sids) {
            DomainRow childRow = modifyActiveDomain(childInstance, sid.idWithValues);
            childRow = addRelation(morphismInstance, triple.parentRow, childRow, triple.parentRecord);
            /*
            addRelation(morphismInstance, triple.parentRow, childRow, sid.childRecord);
            addRelation(morphismInstance.dual(), childRow, triple.parentRow, sid.childRecord);
            */

            //childInstance.merge(childRow);
            
            addPathChildrenToStack(masterStack, triple.parentAccessPath, childRow, sid.childRecord);
        }
    }

    // Fetch id-with-values for given root record.
    private static IdWithValues fetchSid(Id superId, RootRecord rootRecord) {
        var builder = new IdWithValues.Builder();
        
        for (Signature signature : superId.signatures()) {
            /*
            Object value = rootRecord.values().get(signature).getValue();
            if (value instanceof String stringValue)
                builder.add(signature, stringValue);
            */
            SimpleRecord<?> simpleRecord = rootRecord.getSimpleRecord(signature);
            if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord) {
                builder.add(signature, simpleValueRecord.getValue().toString());
            }
            else {
                LOGGER.warn("A simple record with signature {} is an array record:\n{}\n", signature, simpleRecord);
                throw new UnsupportedOperationException("FetchSid doesn't support array values.");
            }
        }
        
        return builder.build();
    }
    
    /**
     * Fetch id-with-values for a schema object in given record / domain row.
     * The output is a set of (Signature, String) for each Signature in superId and its corresponding value from record. Actually, there can be multiple values in the record, so a list of these sets is returned.
     * For further processing, the child records associated with the values are needed (if they are complex), so they are added to the output as well.
     * @param parentRecord Record of the parent (in the access path) schema object.
     * @param parentRow Domain row of the parent schema object.
     * @param morphism Morphism from the parent schema object to the currently processed one.
     * @return
     */
    private static Iterable<FetchedSuperId> fetchSids(IComplexRecord parentRecord, DomainRow parentRow, SchemaMorphism morphism) {
        List<FetchedSuperId> output = new ArrayList<>();
        Id superId = morphism.cod().superId();
        Signature signature = morphism.signature();

        // If the id is empty, the output ids with values will have only one tuple: (<signature>, <value>).
        // This means they represent either singular values (SimpleValueRecord) or a nested document without identifier (not auxilliary).
        if (superId.equals(Id.createEmpty())) {
            // Value is in the parent domain row.
            if (parentRow.hasSignature(signature)) {
                String valueFromParentRow = parentRow.getValue(signature);
                addSimpleValueToOutput(output, valueFromParentRow);
            }
            // There is simple value/array record with given signature in the parent record.
            else if (parentRecord.hasSimpleRecord(signature)) {
                addSidsFromSimpleRecordToOutput(output, parentRecord.getSimpleRecord(signature), signature);
            }
            // There are complex records with given signature in the parent record.
            // They don't represent any (string) value so an unique identifier must be generated instead.
            // But their complex value will be processed later.
            else if (parentRecord.hasComplexRecords(signature)) {
                for (IComplexRecord childRecord : parentRecord.getComplexRecords(signature))
                    addSimpleValueWithChildRecordToOutput(output, UniqueIdProvider.getNext(), childRecord);
            }
        }
        // The superId isn't empty so we need to find value for each signature in superId and return the tuples (<signature>, <value>).
        // Because there are multiple signatures in the superId, we are dealing with a complex property (resp. properties, i.e., children of given parentRecord).
        else if (parentRecord.hasComplexRecords(signature)) {
            for (IComplexRecord childRecord : parentRecord.getComplexRecords(signature)) {
                // If the record has children/values with dynamic names for a signature, it is not allowed to have any other children/values (even with static names) for any other signature.
                // So there are two different complex records - one with static children/values (with possibly different signatures) and the other with only dynamic ones (with the same signature).
                if (childRecord.hasDynamicNameChildren())
                    processComplexRecordWithDynamicChildren(output, superId, parentRow, signature, childRecord);
                else if (childRecord.hasDynamicNameValues())
                    processComplexRecordWithDynamicValues(output, superId, parentRow, signature, childRecord);
                else
                    processStaticComplexRecord(output, superId, parentRow, signature, childRecord);
            }
        }
        
        return output;
    }

    private static void addSidsFromSimpleRecordToOutput(List<FetchedSuperId> output, SimpleRecord<?> simpleRecord, Signature signature) {
        if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
            addSimpleValueToOutput(output, simpleValueRecord.getValue().toString());
        else if (simpleRecord instanceof SimpleArrayRecord<?> simpleArrayRecord)
            simpleArrayRecord.getValues().stream()
                .forEach(valueObject -> addSimpleValueToOutput(output, valueObject.toString()));
    }

    private static void addSimpleValueToOutput(List<FetchedSuperId> output, String value) {
        // It doesn't matter if there is null because the accessPath is also null so it isn't further traversed
        addSimpleValueWithChildRecordToOutput(output, value, null);
    }

    private static void addSimpleValueWithChildRecordToOutput(List<FetchedSuperId> output, String value, IComplexRecord childRecord) {
        var builder = new IdWithValues.Builder();
        builder.add(Signature.createEmpty(), value);
        output.add(new FetchedSuperId(builder.build(), childRecord));
    }

    private static void processComplexRecordWithDynamicChildren(List<FetchedSuperId> output, Id superId, DomainRow parentRow, Signature pathSignature, IComplexRecord childRecord) {
        for (IComplexRecord dynamicNameChild : childRecord.getDynamicNameChildren()) {
            var builder = new IdWithValues.Builder();
            addStaticNameSignaturesToBuilder(superId.signatures(), builder, parentRow, pathSignature, dynamicNameChild);
            output.add(new FetchedSuperId(builder.build(), new DynamicRecordWrapper(childRecord, dynamicNameChild)));
        }
    }

    private static void processComplexRecordWithDynamicValues(List<FetchedSuperId> output, Id superId, DomainRow parentRow, Signature pathSignature, IComplexRecord childRecord) {
        for (SimpleValueRecord<?> dynamicNameValue : childRecord.getDynamicNameValues()) {
            var builder = new IdWithValues.Builder();
            Set<Signature> staticNameSignatures = new TreeSet<>();

            for (Signature signature : superId.signatures()) {
                if (dynamicNameValue.signature().equals(signature))
                    builder.add(signature, dynamicNameValue.getValue().toString());
                else if (dynamicNameValue.name() instanceof DynamicRecordName dynamicName && dynamicName.signature().equals(signature))
                    builder.add(signature, dynamicNameValue.name().value());
                // If the signature is not the dynamic value nor its dynamic name, it is static and we have to find it elsewhere.
                else
                    staticNameSignatures.add(signature);
            }

            addStaticNameSignaturesToBuilder(staticNameSignatures, builder, parentRow, pathSignature, childRecord);

            output.add(new FetchedSuperId(builder.build(), childRecord));
        }
    }
    
    private static void processStaticComplexRecord(List<FetchedSuperId> output, Id superId, DomainRow parentRow, Signature pathSignature, IComplexRecord childRecord) {
        var builder = new IdWithValues.Builder();
        addStaticNameSignaturesToBuilder(superId.signatures(), builder, parentRow, pathSignature, childRecord);
        output.add(new FetchedSuperId(builder.build(), childRecord));
    }

    private static void addStaticNameSignaturesToBuilder(Set<Signature> signatures, IdWithValues.Builder builder, DomainRow parentRow, Signature pathSignature, IComplexRecord childRecord) {
        for (Signature signature : signatures) {
            // How the signature looks like from the parent object.
            var signatureInParentRow = signature.traverseThrough(pathSignature);

            // Why java still doesn't support output arguments?
            String value;
            if (signatureInParentRow == null) {
                SimpleRecord<?> simpleRecord = childRecord.getSimpleRecord(signature);
                if (simpleRecord instanceof SimpleValueRecord<?> simpleValueRecord)
                    value = simpleValueRecord.getValue().toString();
                else if (childRecord.name() instanceof DynamicRecordName dynamicName && dynamicName.signature().equals(signature))
                    value = dynamicName.value();
                else
                    throw new UnsupportedOperationException("FetchSids doesn't support array values for complex records.");
            }
            else
                value = parentRow.getValue(signatureInParentRow);

            builder.add(signature, value);
        }
    }

    /**
     * Creates DomainRow from given IdWithValues, adds it to the instance object and merges it with other potentially duplicite rows.
     * @param instanceObject
     * @param superId
     * @return
     */
    private static DomainRow modifyActiveDomain(InstanceObject instanceObject, IdWithValues superId) {
        var row = new DomainRow(superId, instanceObject);
        instanceObject.addRow(row);
        var merger = new Merger();
        return merger.merge(row, instanceObject);
        //return instanceObject.merge(row);

        /*
        Set<DomainRow> rows = new TreeSet<>();
        Map<Id, IdWithValues> subsetIds = instanceObject.generateIdsFromSuperId(superId);
        
        // We find all rows that are identified by the superIdWithValues.
        for (IdWithValues idWithValues : subsetIds.values()) {
            Map<IdWithValues, DomainRow> map = instanceObject.domain().get(idWithValues.id());
            if (map == null)
                continue;
            
            DomainRow row = map.get(idWithValues);
            if (row != null && !rows.contains(row))
                rows.add(row);
        }

        // Teď přidat morfizmy
        // Následně merge

        // ----
        
        // We merge them together.
        var builder = new IdWithValues.Builder();
        for (DomainRow row : rows)
            for (Signature signature : row.signatures())
                builder.add(signature, row.getValue(signature));
        
        for (Signature signature : superId.signatures())
            builder.add(signature, superId.map().get(signature));
        
        DomainRow newRow = new DomainRow(builder.build(), subsetIds);
        for (IdWithValues subsetIdWithValues : subsetIds.values()) {
            Map<IdWithValues, DomainRow> map = instanceObject.domain().get(subsetIdWithValues.id());
            if (map == null) {
                map = new TreeMap<>();
                instanceObject.domain().put(subsetIdWithValues.id(), map);
            }
            map.put(subsetIdWithValues, newRow);
        }
        
        // TODO: The update of the already existing morphisms should be optimized.
        
        return newRow;
        */
    }

    private DomainRow addRelation(InstanceMorphism morphism, DomainRow parentRow, DomainRow childRow, IComplexRecord childRecord) {
        // First, create a domain row with technical id for each object between the domain and the codomain objects on the path of the morphism.
        var baseMorphisms = morphism.toBases();
        var currentDomainRow = parentRow;

        var parentToCurrent = Signature.createEmpty();
        var currentToChild = morphism.signature();

        for (var baseMorphism : baseMorphisms) {
            // If we are not at the end of the morphisms, we have to create a new row identified by a technical id.
            var instanceObject = baseMorphism.cod();

            parentToCurrent = parentToCurrent.concatenate(currentToChild.getFirst());
            currentToChild = currentToChild.cutFirst();

            var nextRow = childRow;
            if (!instanceObject.equals(morphism.cod())) {
                var superId = fetchSuperIdForTechnicalRow(instanceObject, parentRow, parentToCurrent.dual(), childRow, currentToChild, childRecord);

                //nextRow = new DomainRow(instanceObject.generateTechnicalId(), instanceObject);
                nextRow = new DomainRow(superId, instanceObject);
                instanceObject.addRow(nextRow);
            }

            addBaseRelation(baseMorphism, currentDomainRow, nextRow);
            currentDomainRow = nextRow;
        }

        // Now try merging them from the domain object and then from the codomain object.
        // TODO edit
        parentRow = morphism.dom().mergeAlongMorphism(parentRow, baseMorphisms.get(0));
        parentRow = morphism.cod().mergeAlongMorphism(childRow, baseMorphisms.get(baseMorphisms.size() - 1).dual());

        return parentRow;
    }

    private IdWithValues fetchSuperIdForTechnicalRow(InstanceObject instanceObject, DomainRow parentRow, Signature pathToParent, DomainRow childRow, Signature pathToChild, IComplexRecord parentRecord) {
        var builder = new IdWithValues.Builder();

        for (var signature : instanceObject.schemaObject().superId().signatures()) {
            // The value is in either the first row ...
            var signatureInFirstRow = signature.traverseAlong(pathToParent);
            if (parentRow.hasSignature(signatureInFirstRow)) {
                builder.add(signature, parentRow.getValue(signatureInFirstRow));
                continue;
            }
            
            var signatureInLastRow = signature.traverseAlong(pathToChild);
            if (childRow.hasSignature(signatureInLastRow)) {
                builder.add(signature, childRow.getValue(signatureInLastRow));
                continue;
            }

            // TODO find the value in parent record
        }

        return builder.build();
    }

    private void addBaseRelation(InstanceMorphism morphism, DomainRow domainRow, DomainRow codomainRow) {
        morphism.addMapping(new MappingRow(domainRow, codomainRow));
        morphism.dual().addMapping(new MappingRow(codomainRow, domainRow));
    }
    
    private void addPathChildrenToStack(Deque<StackTriple> stack, AccessPath path, DomainRow sid, IComplexRecord complexRecord) {
        //private static void addPathChildrenToStack(Deque<StackTriple> stack, AccessPath path, ActiveDomainRow sid, IComplexRecord record) {
        if (path instanceof ComplexProperty complexPath)
            for (Pair<Signature, ComplexProperty> child : children(complexPath)) {
                SchemaMorphism morphism = instance.getMorphism(child.getValue0()).schemaMorphism();
                stack.push(new StackTriple(sid, morphism, child.getValue1(), complexRecord));
            }
    }

    /**
     * Determine possible sub-paths to be traversed from this complex property (inner node of an access path).
     * According to the paper, this function should return pairs of (context, value). But value of such sub-path can only be an {@link ComplexProperty}.
     * Similarly, context must be a signature of a morphism.
     * @return set of pairs (morphism signature, complex property) of all possible sub-paths.
     */
    private static Collection<Pair<Signature, ComplexProperty>> children(ComplexProperty complexProperty) {
        final List<Pair<Signature, ComplexProperty>> output = new ArrayList<>();
        
        for (AccessPath subpath : complexProperty.subpaths()) {
            output.addAll(process(subpath.name()));
            output.addAll(process(subpath.context(), subpath.value()));
        }
        
        return output;
    }
    
    /**
     * Process (name, context and value) according to the "process" function from the paper.
     * This function is divided to two parts - one for name and other for context and value.
     * @param name
     * @return see {@link #children()}
     */
    private static Collection<Pair<Signature, ComplexProperty>> process(Name name) {
        if (name instanceof DynamicName dynamicName)
            return List.of(new Pair<>(dynamicName.signature(), ComplexProperty.createEmpty()));
        else // Static or anonymous (empty) name
            return Collections.<Pair<Signature, ComplexProperty>>emptyList();
    }
    
    private static Collection<Pair<Signature, ComplexProperty>> process(IContext context, IValue value) {
        if (value instanceof SimpleValue simpleValue) {
            final Signature contextSignature = context instanceof Signature signature ? signature : Signature.createEmpty();
            final Signature newSignature = simpleValue.signature().concatenate(contextSignature);
            
            return List.of(new Pair<>(newSignature, ComplexProperty.createEmpty()));
        }
        
        if (value instanceof ComplexProperty complexProperty) {
            if (context instanceof Signature signature)
                return List.of(new Pair<>(signature, complexProperty));
            else
                return children(complexProperty);
        }
        
        throw new UnsupportedOperationException("Process");
    }
}

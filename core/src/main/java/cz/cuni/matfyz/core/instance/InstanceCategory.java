package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Category;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.category.Signature.Type;
import cz.cuni.matfyz.core.schema.*;

import java.util.*;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class InstanceCategory implements Category {

	// Evolution extension
	public final SchemaCategory schema;
	private final Map<Key, InstanceObject> objects;
	private final Map<Signature, InstanceMorphism> morphisms;

    InstanceCategory(SchemaCategory schema, Map<Key, InstanceObject> objects, Map<Signature, InstanceMorphism> morphisms)
    {
		this.schema = schema;
		this.objects = objects;
        this.morphisms = morphisms;
	}
    
    public Map<Key, InstanceObject> objects()
    {
        return objects;
    }
    
	public Map<Signature, InstanceMorphism> morphisms()
    {
        return morphisms;
    }

	// Evolution extension
	public void deleteMorphism(InstanceMorphism morphism) {
        morphisms.remove(morphism.signature());
    }
    
	public InstanceObject getObject(Key key)
    {
        return objects.get(key);
	}

	public InstanceObject getObject(SchemaObject schemaObject)
	{
		return this.getObject(schemaObject.key());
	}
    
    public InstanceMorphism getMorphism(Signature signature)
    {
        InstanceMorphism morphism = morphisms.get(signature);
		if (morphism == null)
		{
			// This must be a composite morphism. These are created dynamically so we have to add it dynamically.
			SchemaMorphism schemaMorphism = schema.getMorphism(signature);
			InstanceObject dom = getObject(schemaMorphism.dom().key());
			InstanceObject cod = getObject(schemaMorphism.cod().key());

			morphism = new InstanceMorphism(schemaMorphism, dom, cod, this);
			morphisms.put(signature, morphism);
		}
        
        return morphism;
	}

    public InstanceMorphism getMorphism(SchemaMorphism schemaMorphism)
	{
		return this.getMorphism(schemaMorphism.signature());
	}
    
    public InstanceMorphism dual(Signature signatureOfOriginal)
    {
		return getMorphism(signatureOfOriginal.dual());
	}

	public void createNotes() {
		for (var object : objects.values())
			for (var signature : object.schemaObject().superId().signatures())
				createNotesForSignature(signature, object);
	}

	private void createNotesForSignature(Signature signature, InstanceObject sourceObject) {
		if (signature.getType() != Type.COMPOSITE)
			return;

		var baseSignatures = signature.toBasesReverse();
		var path = new LinkedList<InstanceMorphism>();

		for (int i = 0; i < baseSignatures.size() - 1; i++) {
			var signatureInTarget = Signature.concatenate(baseSignatures.subList(i + 1, baseSignatures.size()));
			var currentSignature = baseSignatures.get(i);
			var currentMorphism = getMorphism(currentSignature).dual();
			path = new LinkedList<>(path);
			path.addFirst(currentMorphism);
			var currentTarget = currentMorphism.dom();

			if (!currentTarget.schemaObject().superId().signatures().contains(signatureInTarget))
				continue;

			currentTarget.addPathToSuperId(signatureInTarget, path, signature);
		}
	}
	
	@Override
	public String toString()
    {
		StringBuilder builder = new StringBuilder();

		builder.append("Keys: ");
		for (Key key : objects.keySet())
			builder.append(key).append(", ");
		builder.append("\n");
        
        builder.append("Objects:\n");
		for (Key key : objects.keySet())
        {
			InstanceObject object = objects.get(key);
			builder.append(object).append("\n");
		}
		builder.append("\n");

        builder.append("Signatures: ");
		for (Signature signature : morphisms.keySet())
			builder.append(signature).append(", ");
		builder.append("\n");
        
        builder.append("Morphisms:\n");
		for (Signature signature : morphisms.keySet())
        {
			InstanceMorphism morphism = morphisms.get(signature);
			builder.append(morphism).append("\n");
		}
		builder.append("\n");

		return builder.toString();
	}

    @Override
    public boolean equals(Object object)
    {
        return object instanceof InstanceCategory instance
            && objects.equals(instance.objects)
            && morphisms.equals(instance.morphisms);
    }
}

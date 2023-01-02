package cz.cuni.matfyz.integration.utils;

import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author jachym.bartik
 */
public class MorphismFinder {

    private final InstanceCategory category;

    public MorphismFinder(InstanceCategory category) {
        this.category = category;
    }

    private Map<ObjectIriTuple, InstanceMorphism> fromDirectObjectCache = new TreeMap<>();

    public InstanceMorphism findDirectFromObject(InstanceObject object, String pimIri) {
        final var tuple = new ObjectIriTuple(object, pimIri);
        if (fromDirectObjectCache.containsKey(tuple))
            return fromDirectObjectCache.get(tuple);

        final var matchingMorphisms = category.morphisms().values().stream()
            .filter(InstanceMorphism::isBase) // TODO optimization
            .filter(morphism -> morphism.dom().equals(object))
            .filter(morphism -> morphism.schemaMorphism.pimIri.equals(pimIri))
            .toList();

        if (matchingMorphisms.size() > 1)
            throw new UnsupportedOperationException("Multiple direct morphisms found from object: " + object.key() + " with pim iri: " + pimIri + ".");
        
        final var result = matchingMorphisms.size() == 1 ? matchingMorphisms.get(0) : null;
        fromDirectObjectCache.put(tuple, result);

        return result;
    }

    private Map<ObjectIriTuple, InstanceMorphism> toDirectObjectCache = new TreeMap<>();

    public InstanceMorphism findDirectToObject(InstanceObject object, String pimIri) {
        final var tuple = new ObjectIriTuple(object, pimIri);
        if (toDirectObjectCache.containsKey(tuple))
            return toDirectObjectCache.get(tuple);

        final var matchingMorphisms = category.morphisms().values().stream()
            .filter(InstanceMorphism::isBase) // TODO optimization
            .filter(morphism -> morphism.cod().equals(object))
            .filter(morphism -> morphism.schemaMorphism.pimIri.equals(pimIri))
            .toList();

        if (matchingMorphisms.size() > 1)
            throw new UnsupportedOperationException("Multiple direct morphisms found to object: " + object.key() + " with pim iri: " + pimIri + ".");
        
        final var result = matchingMorphisms.size() == 1 ? matchingMorphisms.get(0) : null;
        toDirectObjectCache.put(tuple, result);

        return result;
    }

    private Map<ObjectIriTuple, InstanceMorphism> fromObjectCache = new TreeMap<>();

    public InstanceMorphism findFromObject(InstanceObject object, String pimIri) {
        final var tuple = new ObjectIriTuple(object, pimIri);
        if (fromObjectCache.containsKey(tuple))
            return fromObjectCache.get(tuple);

        final var algorithm = new FromObjectIsaSearch(category, object, pimIri);
        final var result = algorithm.process();
        fromObjectCache.put(tuple, result);

        return result;
    }

    private record ObjectIriTuple(
        InstanceObject object,
        String pimIri
    ) implements Comparable<ObjectIriTuple> {

        @Override
        public int compareTo(ObjectIriTuple tuple) {
            int objectResult = object.compareTo(tuple.object);
            if (objectResult != 0)
                return objectResult;

            return pimIri.compareTo(tuple.pimIri);
        }

    }

}

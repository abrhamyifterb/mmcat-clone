package cz.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.utils.PullQuery;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.wrapperdummy.DummyPullWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class PullForestTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PullForestTestBase.class);

    private final Mapping mapping;
    private final AbstractPullWrapper wrapper;

    public PullForestTestBase(TestMapping testMapping, AbstractPullWrapper wrapper) {
        this.mapping = testMapping.mapping();
        this.wrapper = wrapper;
    }

    private String expected;

    public PullForestTestBase expected(String expected) {
        this.expected = expected;

        return this;
    }

    public void run() {            
        var forest = wrapper.pullForest(mapping.accessPath(), PullQuery.fromKindName(mapping.kindName()));
        LOGGER.debug("Pulled forest:\n" + forest);

        var expectedForest = new DummyPullWrapper().pullForest(mapping.accessPath(), PullQuery.fromString(expected));
        LOGGER.debug("Expected forest:\n" + expectedForest);
        
        assertEquals(expectedForest.toString(), forest.toString());
    }

}

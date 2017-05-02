package ch.uzh.ifi.ce.mweiss.sats.core.model.gsvm;

import ch.uzh.ifi.ce.mweiss.sats.core.util.random.IntegerInterval;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.JavaUtilRNGSupplier;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael Weiss
 */
public class GSVMWorldTest {

    /**
     * Checks if standard-sized world is set up correctly
     */
    @Test
    public void standardWorldSetUpCorrectly() {
        // Directly create standard world through model
        GlobalSynergyValueModel model = new GlobalSynergyValueModel();
        GSVMWorld world1 = model.createWorld(new JavaUtilRNGSupplier(983742L));
        Assert.assertTrue(world1.getNationalCircle().getSize() == 12);
        Assert.assertTrue(world1.getRegionalCircle().getSize() == 6);
        Assert.assertTrue(world1.getLicenses().size() == 18);

        // Create standard world with a builder
        GSVMWorldSetup.GSVMWorldSetupBuilder builder = new GSVMWorldSetup.GSVMWorldSetupBuilder();
        GSVMWorld world2 = new GSVMWorld(builder.build(), new JavaUtilRNGSupplier(983742L));

        // Assert that this makes no difference
        Assert.assertEquals(world1, world2);
    }

    /**
     * Checks if non-standard-sized world is set up correctly
     */
    @Test
    public void nonStandardWorldSetUpCorrectly() {
        GSVMWorldSetup.GSVMWorldSetupBuilder builder = new GSVMWorldSetup.GSVMWorldSetupBuilder();

        // Single value as interval
        builder.setSizeInterval(new IntegerInterval(9));
        GSVMWorld world1 = new GSVMWorld(builder.build(), new JavaUtilRNGSupplier(983742L));
        Assert.assertTrue(world1.getSize() == 9);
        Assert.assertTrue(world1.getLicenses().size() == 27);

        // Actual interval
        builder.setSizeInterval(new IntegerInterval(3, 12));
        GSVMWorld world2 = new GSVMWorld(builder.build(), new JavaUtilRNGSupplier(983742L));
        Assert.assertTrue(world2.getSize() == 11);
        Assert.assertTrue(world2.getLicenses().size() == 33);

        // Another seed
        GSVMWorld world3 = new GSVMWorld(builder.build(), new JavaUtilRNGSupplier(963742L));
        Assert.assertTrue(world3.getSize() == 3);
        Assert.assertTrue(world3.getLicenses().size() == 9);
    }
}

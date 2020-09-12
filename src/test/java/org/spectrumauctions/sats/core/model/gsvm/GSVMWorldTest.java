package org.spectrumauctions.sats.core.model.gsvm;

import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

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
        Assert.assertEquals(12, world1.getNationalCircle().getSize());
        Assert.assertEquals(6, world1.getRegionalCircle().getSize());
        Assert.assertEquals(18, world1.getLicenses().size());

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
        Assert.assertEquals(9, world1.getSize());
        Assert.assertEquals(27, world1.getLicenses().size());

        // Actual interval
        builder.setSizeInterval(new IntegerInterval(3, 12));
        GSVMWorld world2 = new GSVMWorld(builder.build(), new JavaUtilRNGSupplier(983742L));
        Assert.assertEquals(11, world2.getSize());
        Assert.assertEquals(33, world2.getLicenses().size());

        // Another seed
        GSVMWorld world3 = new GSVMWorld(builder.build(), new JavaUtilRNGSupplier(963742L));
        Assert.assertEquals(3, world3.getSize());
        Assert.assertEquals(9, world3.getLicenses().size());
    }
}

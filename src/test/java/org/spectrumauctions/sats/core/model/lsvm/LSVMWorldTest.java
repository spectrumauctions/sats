package org.spectrumauctions.sats.core.model.lsvm;

import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

/**
 * @author Fabio Isler
 */
public class LSVMWorldTest {

    /**
     * Checks if standard-sized world is set up correctly
     */
    @Test
    public void standardWorldSetUpCorrectly() {
        // Directly create standard world through model
        LocalSynergyValueModel model = new LocalSynergyValueModel();
        LSVMWorld world1 = model.createWorld(new JavaUtilRNGSupplier(983742L));
        Assert.assertEquals(3, world1.getGrid().getNumberOfRows());
        Assert.assertEquals(6, world1.getGrid().getNumberOfColumns());
        Assert.assertEquals(18, world1.getLicenses().size());

        // Create standard world with a builder
        LSVMWorldSetup.LSVMWorldSetupBuilder builder = new LSVMWorldSetup.LSVMWorldSetupBuilder();
        LSVMWorld world2 = new LSVMWorld(builder.build(), new JavaUtilRNGSupplier(983742L));

        // Assert that this makes no difference
        Assert.assertEquals(world1, world2);
    }

    /**
     * Checks if non-standard-sized world is set up correctly
     */
    @Test
    public void nonStandardWorldSetUpCorrectly() {
        LSVMWorldSetup.LSVMWorldSetupBuilder builder = new LSVMWorldSetup.LSVMWorldSetupBuilder();

        // Single value as interval
        builder.createGridSizeRandomly(new IntegerInterval(5), new IntegerInterval(8));
        LSVMWorld world1 = new LSVMWorld(builder.build(), new JavaUtilRNGSupplier(983742L));
        Assert.assertEquals(5, world1.getGrid().getNumberOfRows());
        Assert.assertEquals(8, world1.getGrid().getNumberOfColumns());
        Assert.assertEquals(40, world1.getLicenses().size());

        // Actual interval
        builder.createGridSizeRandomly(new IntegerInterval(1, 5), new IntegerInterval(3, 10));
        LSVMWorld world2 = new LSVMWorld(builder.build(), new JavaUtilRNGSupplier(983742L));
        Assert.assertEquals(4, world2.getGrid().getNumberOfRows());
        Assert.assertEquals(4, world2.getGrid().getNumberOfColumns());
        Assert.assertEquals(16, world2.getLicenses().size());

        // Another seed
        LSVMWorld world3 = new LSVMWorld(builder.build(), new JavaUtilRNGSupplier(963742L));
        Assert.assertEquals(1, world3.getGrid().getNumberOfRows());
        Assert.assertEquals(6, world3.getGrid().getNumberOfColumns());
        Assert.assertEquals(6, world3.getLicenses().size());
    }
}

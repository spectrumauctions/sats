package ch.uzh.ifi.ce.mweiss.specval.model.cats;

import ch.uzh.ifi.ce.mweiss.specval.util.random.DoubleInterval;
import ch.uzh.ifi.ce.mweiss.specval.util.random.IntegerInterval;
import ch.uzh.ifi.ce.mweiss.specval.util.random.JavaUtilRNGSupplier;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * @author Fabio Isler
 */
public class CATSWorldTest {

    /**
     * Checks if standard-sized world is set up correctly
     */
    @Test
    public void standardWorldSetUpCorrectly() {
        // Directly create standard world through model
        CATSRegionModel model = new CATSRegionModel();
        CATSWorld world1 = model.createWorld(new JavaUtilRNGSupplier(983742L));

        // Create standard world with a builder
        CATSWorldSetup.Builder builder = new CATSWorldSetup.Builder();
        CATSWorld world2 = new CATSWorld(builder.build(), new JavaUtilRNGSupplier(983742L));

        // Assert that this makes no difference
        Assert.assertEquals(world1, world2);
    }

    /**
     * Checks if non-standard-sized world is set up correctly
     */
    @Test
    public void nonStandardWorldSetUpCorrectly() {
        CATSWorldSetup.Builder builder = new CATSWorldSetup.Builder();

        // Single value as interval
        builder.setNumberOfRowsInterval(new IntegerInterval(3));
        builder.setNumberOfColumnsInterval(new IntegerInterval(5));
        CATSWorld world1 = new CATSWorld(builder.build(), new JavaUtilRNGSupplier());
        Set<CATSLicense> licenses = world1.getLicenses();
        Assert.assertTrue(licenses.size() == 15);
    }

    /**
     * Checks if highly customized world is set up correctly
     */
    @Test
    public void customWorldSetUpCorrectly() {
        CATSWorldSetup.Builder builder = new CATSWorldSetup.Builder();

        // Single value as interval
        builder.setNumberOfRowsInterval(new IntegerInterval(3));
        builder.setNumberOfColumnsInterval(new IntegerInterval(2));
        builder.setAdditionalNeigh(5);
        builder.setAdditivity(0.5);
        builder.setCommonValueInterval(new DoubleInterval(0, 5));
        builder.setUseQuadraticPricingOption(true);
        CATSWorld world1 = new CATSWorld(builder.build(), new JavaUtilRNGSupplier());
        Set<CATSLicense> licenses = world1.getLicenses();
        Assert.assertEquals(licenses.size(), 6);
        Assert.assertEquals(world1.getAdditivity(), 0.5, 0);
        Assert.assertEquals(world1.getUseQuadraticPricingOption(), false);
    }
}

package org.spectrumauctions.sats.core.model.cats;

import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

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
        Assert.assertFalse(world1.getUseQuadraticPricingOption());
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
        Assert.assertTrue(world1.getUseQuadraticPricingOption());
    }

    /**
     * Checks if highly customized world with a defined number of licenses is set up correctly
     */
    @Test
    public void customWorldDefinedNumberOfLicensesSetUpCorrectly() {
        CATSWorldSetup.Builder builder = new CATSWorldSetup.Builder();

        // Single value as interval
        builder.setNumberOfRowsInterval(new IntegerInterval(3));
        builder.setNumberOfColumnsInterval(new IntegerInterval(2));
        builder.setAdditionalNeigh(5);
        builder.setAdditivity(0.5);
        builder.setCommonValueInterval(new DoubleInterval(0, 5));
        builder.setUseQuadraticPricingOption(true);
        builder.setNumberOfGoodsInterval(new IntegerInterval(25));
        CATSWorld world1 = new CATSWorld(builder.build(), new JavaUtilRNGSupplier());
        Set<CATSLicense> licenses = world1.getLicenses();
        Assert.assertEquals(licenses.size(), 25);
        Assert.assertEquals(world1.getAdditivity(), 0.5, 0);
        Assert.assertTrue(world1.getUseQuadraticPricingOption());
    }

    /**
     * Checks if standard world with a defined number of licenses is set up correctly
     */
    @Test
    public void standardWorldDefinedNumberOfLicensesSetUpCorrectly() {
        // Directly create standard world through model
        CATSRegionModel model = new CATSRegionModel();
        model.setNumberOfGoods(36);
        CATSWorld world1 = model.createWorld(new JavaUtilRNGSupplier(983742L));

        // Create standard world with a builder
        CATSWorldSetup.Builder builder = new CATSWorldSetup.Builder();
        builder.setNumberOfGoodsInterval(new IntegerInterval(36));
        CATSWorld world2 = new CATSWorld(builder.build(), new JavaUtilRNGSupplier(983742L));

        // Assert that this makes no difference
        Assert.assertEquals(world1, world2);
        Assert.assertEquals(36, world1.getNumberOfGoods());
    }
}

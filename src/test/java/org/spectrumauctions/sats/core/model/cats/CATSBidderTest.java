package org.spectrumauctions.sats.core.model.cats;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.LicenseBundle;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabio Isler
 */
public class CATSBidderTest {

    private static LicenseBundle<CATSLicense> completeBundle;

    @BeforeClass
    public static void setUpBeforeClass() {
        CATSRegionModel model = new CATSRegionModel();
        CATSWorld world = model.createWorld(983742L);
        completeBundle = new LicenseBundle<>();
        completeBundle.addAll(world.getLicenses());
    }

    /**
     * Tests the default bidder setup via the model
     */
    @Test
    public void testDefaultCustomBidderSetup() {
        CATSRegionModel model = new CATSRegionModel();
        CATSWorld world = model.createWorld(983742L);
        List<CATSBidder> defaultPopulation = model.createPopulation(world);
        Assert.assertEquals(defaultPopulation.size(), 1);
        checkBidder(defaultPopulation.get(0), "CATS Bidder Setup");
    }

    /**
     * Tests the custom bidder number via the model
     */
    @Test
    public void testMinimalCustomBidderSetup() {
        CATSRegionModel model2 = new CATSRegionModel();
        CATSWorld world2 = model2.createWorld(983742L);
        model2.setNumberOfBidders(2);
        List<CATSBidder> population2 = model2.createPopulation(world2);
        Assert.assertEquals(population2.size(), 2);
        checkBidder(population2.get(0), "CATS Bidder Setup");
        checkBidder(population2.get(1), "CATS Bidder Setup");
    }

    /**
     * Tests a highly customized bidder setup via the model
     */
    @Test
    public void testCompleteBundleValue() {
        CATSRegionModel model2 = new CATSRegionModel();
        CATSWorld world2 = model2.createWorld(983742L);

        List<CATSBidder> customPopulation = customPopulation(world2, 1);
        Assert.assertEquals(customPopulation.size(), 1);

        BigDecimal value = customPopulation.get(0).calculateValue(completeBundle);

        float expectedValue = 0;
        for (CATSLicense license : completeBundle) {
            expectedValue += license.getCommonValue();
            expectedValue += customPopulation.get(0).getPrivateValues().get(license.getLongId()).floatValue();
        }
        expectedValue += Math.pow(completeBundle.size(), 1.2);

        Assert.assertEquals(value.floatValue(), expectedValue, 0.01f);
    }

    @Test
    public void testCompleteBundleValueQuadraticPricing() {
        CATSWorldSetup.Builder worldSetupBuilder = new CATSWorldSetup.Builder();
        worldSetupBuilder.setUseQuadraticPricingOption(true);
        CATSWorld world2 = new CATSWorld(worldSetupBuilder.build(), new JavaUtilRNGSupplier(983742L));

        List<CATSBidder> customPopulation = customPopulation(world2, 1);
        Assert.assertEquals(customPopulation.size(), 1);

        BigDecimal value = customPopulation.get(0).calculateValue(completeBundle);

        float expectedValue = 0;
        for (CATSLicense license : completeBundle) {
            expectedValue += license.getCommonValue();
            expectedValue += Math.pow(license.getCommonValue(), 2);
            expectedValue += customPopulation.get(0).getPrivateValues().get(license.getLongId()).floatValue();
        }

        Assert.assertEquals(value.floatValue(), expectedValue, 0.1);
    }

    // ------- Helpers ------- //

    private List<CATSBidder> customPopulation(CATSWorld world, int numberOfBidders) {

        CATSBidderSetup.Builder BidderBuilder = new CATSBidderSetup.Builder();
        BidderBuilder.setPrivateValueParameters(15, 60);
        BidderBuilder.setNumberOfBidders(numberOfBidders);
        BidderBuilder.setSetupName("Test CATS Bidder");

        List<CATSBidderSetup> regionalSetups = new ArrayList<>();
        regionalSetups.add(BidderBuilder.build());

        return world.createPopulation(regionalSetups, new JavaUtilRNGSupplier(983742L));
    }

    private void checkBidder(CATSBidder bidder, String setupType) {
        Assert.assertEquals(bidder.getSetupType(), setupType);
    }
}

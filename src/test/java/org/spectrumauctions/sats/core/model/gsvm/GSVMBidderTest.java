package org.spectrumauctions.sats.core.model.gsvm;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.marketdesignresearch.mechlib.domain.Bundle;
import org.spectrumauctions.sats.core.model.LicenseBundle;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Fabio Isler
 */
public class GSVMBidderTest {

    private static Bundle completeBundle;

    @BeforeClass
    public static void setUpBeforeClass() {
        GlobalSynergyValueModel model = new GlobalSynergyValueModel();
        GSVMWorld world = model.createWorld(983742L);
        completeBundle = Bundle.singleGoods(world.getLicenses());
    }

    /**
     * Tests the default bidder setup via the model
     */
    @Test
    public void testDefaultCustomBidderSetup() {
        GlobalSynergyValueModel model = new GlobalSynergyValueModel();
        GSVMWorld world = model.createWorld(983742L);
        List<GSVMBidder> defaultPopulation = model.createPopulation(world);

        Assert.assertEquals(defaultPopulation.size(), 7);

        checkBidder(defaultPopulation.get(0), "Regional Bidder Setup");
        checkBidder(defaultPopulation.get(6), "National Bidder Setup");
    }

    /**
     * Tests the minimalistic bidder setup via the model
     */
    @Test
    public void testMinimalCustomBidderSetup() {
        GlobalSynergyValueModel model2 = new GlobalSynergyValueModel();
        GSVMWorld world2 = model2.createWorld(983742L);
        model2.setNumberOfNationalBidders(3);
        model2.setNumberOfRegionalBidders(2);
        List<GSVMBidder> minimalPopulation = model2.createPopulation(world2);

        Assert.assertEquals(minimalPopulation.size(), 5);

        checkBidder(minimalPopulation.get(0), "Regional Bidder Setup");
        checkBidder(minimalPopulation.get(4), "National Bidder Setup");
    }

    /**
     * Tests a highly customized bidder setup via the model
     */
    @Test
    public void testCustomBidderSetup() {
        GlobalSynergyValueModel model2 = new GlobalSynergyValueModel();
        GSVMWorld world2 = model2.createWorld(983742L);

        List<GSVMBidder> customPopulation = customPopulation(world2, 8, 2);
        Assert.assertEquals(customPopulation.size(), 10);

        for (int i = 0; i < 8; i++)
            checkBidder(customPopulation.get(i), "Test Regional Bidder");
        for (int i = 8; i < 10; i++) checkBidder(customPopulation.get(i), "Test National Bidder");

        BigDecimal[] values = new BigDecimal[10];
        for (int i = 0; i < values.length; i++) values[i] = customPopulation.get(i).calculateValue(completeBundle);

        float regionalValue = 2 * 15;
        float nationalValueZeroHighs = 4 * 25;
        float nationalValueTwoHighs = 2 * 25 + 2 * 35;
        float nationalValueFourHighs = 4 * 35;
        float factor = (completeBundle.getSingleAvailabilityGoods().size() - 1) * 0.2f;

        float[] expectedValues = new float[6];
        expectedValues[0] = (nationalValueZeroHighs + regionalValue) + (nationalValueZeroHighs + regionalValue) * factor;
        expectedValues[1] = (nationalValueTwoHighs + regionalValue) + (nationalValueTwoHighs + regionalValue) * factor;
        expectedValues[2] = (nationalValueFourHighs + regionalValue) + (nationalValueFourHighs + regionalValue) * factor;
        expectedValues[3] = (nationalValueTwoHighs + regionalValue) + (nationalValueTwoHighs + regionalValue) * factor;
        expectedValues[4] = (nationalValueZeroHighs + regionalValue) + (nationalValueZeroHighs + regionalValue) * factor;
        expectedValues[5] = (nationalValueZeroHighs + regionalValue) + (nationalValueZeroHighs + regionalValue) * factor;
        float expectedNationalBidderValue = 8 * 16 + 4 * 26 + (8 * 16 + 4 * 26) * factor;

        for (int i = 0; i < 8; i++) {
            Assert.assertEquals(values[i].floatValue(), expectedValues[i % 6], 0.001f);
        }
        for (int i = 8; i < 10; i++) Assert.assertEquals(values[i].floatValue(), expectedNationalBidderValue, 0.001f);

    }

    /**
     * Test a custom world setup
     */
    @Test
    public void testCustomWorldSetup() {
        GSVMWorldSetup.GSVMWorldSetupBuilder worldSetupBuilder = new GSVMWorldSetup.GSVMWorldSetupBuilder();
        worldSetupBuilder.setSizeInterval(new IntegerInterval(1, 16));
        GSVMWorldSetup setup = worldSetupBuilder.build();
        GSVMWorld world = new GSVMWorld(setup, new JavaUtilRNGSupplier(983742L));
        Assert.assertEquals(world.getSize(), 8);
        Assert.assertEquals(world.getNumberOfGoods(), 8 * 3);
        Assert.assertEquals(world.getNationalCircle().getLicenses().length, 8 * 2);
        Assert.assertEquals(world.getRegionalCircle().getLicenses().length, 8);

        List<GSVMBidder> customPopulation = customPopulation(world, 3, 1);
        Assert.assertEquals(customPopulation.size(), 4);

        Bundle regionalBundle = Bundle.singleGoods(Arrays.asList(world.getRegionalCircle().getLicenses()));

        for (int i = 0; i < 3; i++)
            checkBidder(customPopulation.get(i), "Test Regional Bidder");
        for (int i = 3; i < 4; i++) checkBidder(customPopulation.get(i), "Test National Bidder");

        // Assert that national bidder has zero value for the whole regional bundle
        Assert.assertEquals(customPopulation.get(3).calculateValue(regionalBundle).doubleValue(), 0, 0);

        float factor = (completeBundle.getSingleAvailabilityGoods().size() - 1) * 0.2f;

        // Check if national bidder has expected value
        float expectedValue = 12 * 16 + 4 * 26 + (12 * 16 + 4 * 26) * factor;
        Assert.assertEquals(customPopulation.get(3).calculateValue(completeBundle).floatValue(), expectedValue, 0.001f);

        // Check if regional bidder in low region has expected value
        expectedValue = 4 * 25 + 2 * 15 + (4 * 25 + 2 * 15) * factor;
        Assert.assertEquals(customPopulation.get(0).calculateValue(completeBundle).floatValue(), expectedValue, 0.001f);

        // Check if regional bidder in high region has expected value
        expectedValue = 4 * 35 + 2 * 15 + (4 * 35 + 2 * 15) * factor;
        Assert.assertEquals(customPopulation.get(2).calculateValue(completeBundle).floatValue(), expectedValue, 0.001f);

        // Check if regional bidder in mixed region has expected value
        expectedValue = 2 * 25 + 2 * 35 + 2 * 15 + (2 * 25 + 2 * 35 + 2 * 15) * factor;
        Assert.assertEquals(customPopulation.get(1).calculateValue(completeBundle).floatValue(), expectedValue, 0.001f);
    }

    /**
     * Test a very small world setup
     */
    @Test
    public void testExtremeWorldSetup() {
        GSVMWorldSetup.GSVMWorldSetupBuilder worldSetupBuilder = new GSVMWorldSetup.GSVMWorldSetupBuilder();
        worldSetupBuilder.setSizeInterval(new IntegerInterval(1));
        GSVMWorldSetup setup = worldSetupBuilder.build();
        GSVMWorld world = new GSVMWorld(setup, new JavaUtilRNGSupplier(983742L));
        Assert.assertEquals(world.getSize(), 1);
        Assert.assertEquals(world.getNumberOfGoods(), 3);
        Assert.assertEquals(world.getNationalCircle().getLicenses().length, 2);
        Assert.assertEquals(world.getRegionalCircle().getLicenses().length, 1);

        List<GSVMBidder> customPopulation = customPopulation(world, 3, 1);
        Assert.assertEquals(customPopulation.size(), 4);

        for (int i = 0; i < 3; i++)
            checkBidder(customPopulation.get(i), "Test Regional Bidder");
        for (int i = 3; i < 4; i++) checkBidder(customPopulation.get(i), "Test National Bidder");

        Bundle regionalBundle = Bundle.singleGoods(Arrays.asList(world.getRegionalCircle().getLicenses()));

        // Assert that national bidder has zero value for the whole regional bundle
        Assert.assertEquals(customPopulation.get(3).calculateValue(regionalBundle).doubleValue(), 0, 0);

        float factor = (completeBundle.getSingleAvailabilityGoods().size() - 1) * 0.2f;

        // Check if national bidder has expected value
        float expectedValue = 2 * 16 + (2 * 16) * factor;
        Assert.assertEquals(customPopulation.get(3).calculateValue(completeBundle).floatValue(), expectedValue, 0.001f);

        // Check if regional bidders all have expected value
        expectedValue = 2 * 25 + 15 + (2 * 25 + 15) * factor;
        Assert.assertEquals(customPopulation.get(0).calculateValue(completeBundle).floatValue(), expectedValue, 0.001f);
        Assert.assertEquals(customPopulation.get(1).calculateValue(completeBundle).floatValue(), expectedValue, 0.001f);
        Assert.assertEquals(customPopulation.get(2).calculateValue(completeBundle).floatValue(), expectedValue, 0.001f);
    }


    // ------- Helpers ------- //


    private List<GSVMBidder> customPopulation(GSVMWorld world, int numberOfRegionalBidders, int numberOfNationalBidders) {

        GSVMRegionalBidderSetup.Builder regionalBidderBuilder = new GSVMRegionalBidderSetup.Builder();
        regionalBidderBuilder.setRegionalValueInterval(new DoubleInterval(15));
        regionalBidderBuilder.setLowNationalValueInterval(new DoubleInterval(25));
        regionalBidderBuilder.setHighNationalValueInterval(new DoubleInterval(35));
        regionalBidderBuilder.setNumberOfBidders(numberOfRegionalBidders);
        regionalBidderBuilder.setSetupName("Test Regional Bidder");

        GSVMNationalBidderSetup.Builder nationalBidderBuilder = new GSVMNationalBidderSetup.Builder();
        nationalBidderBuilder.setNumberOfBidders(numberOfNationalBidders);
        nationalBidderBuilder.setLowNationalValueInterval(new DoubleInterval(16));
        nationalBidderBuilder.setHighNationalValueInterval(new DoubleInterval(26));
        nationalBidderBuilder.setSetupName("Test National Bidder");

        Collection<GSVMRegionalBidderSetup> regionalSetups = new ArrayList<>();
        regionalSetups.add(regionalBidderBuilder.build());
        Collection<GSVMNationalBidderSetup> nationalSetups = new ArrayList<>();
        nationalSetups.add(nationalBidderBuilder.build());

        return world.createPopulation(regionalSetups, nationalSetups, new JavaUtilRNGSupplier(983742L));
    }

    private void checkBidder(GSVMBidder bidder, String setupType) {
        Assert.assertEquals(bidder.getSetupType(), setupType);
    }
}

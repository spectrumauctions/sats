package org.spectrumauctions.sats.core.model.srvm;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabio Isler
 */
public class SRVMBidderTest {

    private static Bundle<SRVMLicense> completeBundle;
    private static SRVMLicense singleLicense;
    private static Bundle<SRVMLicense> singleLicenseBundle;

    @BeforeClass
    public static void setUpBeforeClass() {
        SingleRegionModel model = new SingleRegionModel();
        SRVMWorld world = model.createWorld(983742L);
        completeBundle = new Bundle<>();
        completeBundle.addAll(world.getLicenses());
        singleLicense = world.getLicenses().iterator().next();
        singleLicenseBundle = new Bundle<>();
        singleLicenseBundle.add(singleLicense);
    }

    /**
     * Tests the default bidder setup via the model
     */
    @Test
    public void testDefaultCustomBidderSetup() {
        SingleRegionModel model = new SingleRegionModel();
        SRVMWorld world = model.createWorld(983742L);
        List<SRVMBidder> defaultPopulation = model.createPopulation(world);
        Assert.assertEquals(defaultPopulation.size(), 7);
        checkBidder(defaultPopulation.get(0), "SRVM Bidder Setup");
    }

    /**
     * Tests the value of the complete bundle
     */
    @Test
    public void testCompleteBundleValue() {
        SingleRegionModel model2 = new SingleRegionModel();
        SRVMWorld world2 = model2.createWorld(983742L);

        List<SRVMBidder> customPopulation = customPopulation(world2, 1);
        Assert.assertEquals(customPopulation.size(), 1);

        SRVMBidder bidder = customPopulation.get(0);

        Map<SRVMBand, Integer> quantities = new HashMap<>();

        double expectedValue = 0;

        int synergyCount = 0;
        for (SRVMBand band : world2.getBands()) {
            int numberOfLicenses = band.getNumberOfLicenses();
            quantities.put(band, numberOfLicenses);
            double baseValue = bidder.getBaseValues().get(band.getName()).doubleValue();
            double synergyThreshold = bidder.getSynergyThreshold().get(band.getName());
            double synergyFactor = bidder.getIntrabandSynergyFactors().get(band.getName()).doubleValue();

            double firstSummand = Math.min(synergyThreshold, numberOfLicenses);
            double x = numberOfLicenses > 0 ? (numberOfLicenses - 1.0) / numberOfLicenses : 0;
            double secondSummand = Math.min((synergyThreshold - 1.0) / synergyThreshold, x) * synergyFactor;
            double secondTerm = synergyThreshold <= numberOfLicenses ? Math.log(numberOfLicenses - synergyThreshold + 1.0) : 0.0;
            double thirdSummand = Math.max(0.0, secondTerm);

            expectedValue += (firstSummand + secondSummand + thirdSummand) * baseValue;
            if (numberOfLicenses > 0) {
                synergyCount++;
            }
        }

        if (synergyCount > 1) expectedValue *= bidder.getInterbandSynergyValue().doubleValue();

        BigDecimal value = bidder.calculateValue(completeBundle);
        BigDecimal compareValue = bidder.calculateValue(quantities);
        Assert.assertEquals(value, compareValue);

        Assert.assertEquals(expectedValue, value.floatValue(), 1);
    }

    /**
     * Tests the value of a single license
     */
    @Test
    public void testSingleLicenseBundleValue() {
        SingleRegionModel model2 = new SingleRegionModel();
        SRVMWorld world2 = model2.createWorld(983742L);

        List<SRVMBidder> customPopulation = customPopulation(world2, 1);
        Assert.assertEquals(customPopulation.size(), 1);

        SRVMBidder bidder = customPopulation.get(0);
        SRVMBand band = singleLicense.getBand();
        BigDecimal value = bidder.calculateValue(singleLicenseBundle);

        double expectedValue = 0;

        double baseValue = bidder.getBaseValues().get(band.getName()).doubleValue();
        double synergyThreshold = bidder.getSynergyThreshold().get(band.getName());
        double synergyFactor = bidder.getIntrabandSynergyFactors().get(band.getName()).doubleValue();

        expectedValue += Math.min(synergyThreshold, 1.0) * baseValue;
        expectedValue += Math.min((synergyThreshold - 1) / synergyThreshold, 0) * synergyFactor * baseValue;
        double secondTerm = synergyThreshold <= 1.0 ? Math.log(1.0 - synergyThreshold + 1.0) : 0.0;
        expectedValue += Math.max(0.0, secondTerm) * baseValue;

        Assert.assertEquals(value.floatValue(), expectedValue, 0.00001);
    }

    // ------- Helpers ------- //

    private List<SRVMBidder> customPopulation(SRVMWorld world, int numberOfBidders) {

        SRVMBidderSetup.PrimaryBidderBuilder BidderBuilder = new SRVMBidderSetup.PrimaryBidderBuilder();
        BidderBuilder.setNumberOfBidders(numberOfBidders);
        BidderBuilder.setSetupName("Test SRVM Bidder");

        List<SRVMBidderSetup> regionalSetups = new ArrayList<>();
        regionalSetups.add(BidderBuilder.build());

        return world.createPopulation(regionalSetups, new JavaUtilRNGSupplier(983742L));
    }

    private void checkBidder(SRVMBidder bidder, String setupType) {
        Assert.assertEquals(bidder.getSetupType(), setupType);
    }
}

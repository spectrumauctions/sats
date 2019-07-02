package org.spectrumauctions.sats.core.model.lsvm;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.LicenseBundle;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabio Isler
 */
public class LSVMBidderTest {

    private static LicenseBundle<LSVMLicense> completeBundle;
    private static List<LSVMBidder> standardPopulation;

    @BeforeClass
    public static void setUpBeforeClass() {
        LocalSynergyValueModel model = new LocalSynergyValueModel();
        LSVMWorld world = model.createWorld(983742L);
        LSVMBidderSetup regionalBidders = new LSVMBidderSetup.RegionalBidderBuilder().build();
        LSVMBidderSetup nationalBidder = new LSVMBidderSetup.NationalBidderBuilder().build();
        List<LSVMBidderSetup> setups = new ArrayList<>();
        setups.add(regionalBidders);
        setups.add(nationalBidder);
        standardPopulation = world.createPopulation(setups, new JavaUtilRNGSupplier(983742L));
        completeBundle = new LicenseBundle<>();
        completeBundle.addAll(world.getLicenses());
    }

    /**
     * Tests the minimalistic bidder setup via the model
     */
    @Test
    public void testMinimalCustomBidderSetup() {
        LocalSynergyValueModel model2 = new LocalSynergyValueModel();
        LSVMWorld world2 = model2.createWorld(983742L);
        model2.setNumberOfNationalBidders(3);
        model2.setNumberOfRegionalBidders(2);
        List<LSVMBidder> minimalPopulation = model2.createPopulation(world2);

        Assert.assertTrue(minimalPopulation.size() == 5);

        Assert.assertTrue(minimalPopulation.get(0).getSetupType().equals("National Bidder Setup"));
        Assert.assertTrue(minimalPopulation.get(4).getSetupType().equals("Regional Bidder Setup"));
    }

    /**
     * Tests a highly customized bidder setup via the model
     */
    @Test
    public void testCustomBidderSetup() {
        LocalSynergyValueModel model2 = new LocalSynergyValueModel();
        LSVMWorld world2 = model2.createWorld(983742L);

        LSVMBidderSetup.RegionalBidderBuilder regionalBidderBuilder = new LSVMBidderSetup.RegionalBidderBuilder();
        regionalBidderBuilder.setProximitySize(1);
        regionalBidderBuilder.setNumberOfBidders(8);
        regionalBidderBuilder.setSetupName("Test Regional Bidder");
        regionalBidderBuilder.setValueInterval(new DoubleInterval(10));
        regionalBidderBuilder.setLsvmA(0);
        regionalBidderBuilder.setLsvmB(5);

        LSVMBidderSetup.NationalBidderBuilder nationalBidderBuilder = new LSVMBidderSetup.NationalBidderBuilder();
        nationalBidderBuilder.setNumberOfBidders(2);
        nationalBidderBuilder.setValueInterval(new DoubleInterval(4));
        nationalBidderBuilder.setSetupName("Test National Bidder");

        List<LSVMBidderSetup> setups = new ArrayList<>();
        setups.add(regionalBidderBuilder.build());
        setups.add(nationalBidderBuilder.build());
        List<LSVMBidder> customPopulation = world2.createPopulation(setups, new JavaUtilRNGSupplier(983742L));

        Assert.assertTrue(customPopulation.size() == 10);

        Assert.assertTrue(customPopulation.get(0).getSetupType().equals("Test Regional Bidder"));

        Assert.assertTrue(customPopulation.get(9).getSetupType().equals("Test National Bidder"));

        LicenseBundle<LSVMLicense> proximity = new LicenseBundle<>();
        proximity.addAll(customPopulation.get(0).getProximity());

        BigDecimal valueRegionalBidderComplete = customPopulation.get(0).calculateValue(completeBundle);
        BigDecimal valueRegionalBidderProximity = customPopulation.get(0).calculateValue(proximity);

        // Value is the same because LSVM_A and therefore the whole factor is zero
        Assert.assertTrue(valueRegionalBidderComplete.compareTo(valueRegionalBidderProximity) == 0);
    }

    /**
     * Checks if the a national bidder values a complete bundle more than a regional bidder in the standard setting
     */
    @Test
    public void testCompleteBundleValues() {
        BigDecimal valueRegionalBidder = standardPopulation.get(0).calculateValue(completeBundle);
        BigDecimal valueNationalBidder = standardPopulation.get(5).calculateValue(completeBundle);

        Assert.assertTrue(valueRegionalBidder.compareTo(valueNationalBidder) == -1);
    }

    /**
     * Checks if the a regional bidder values a bundle consisting of only licenses in her proximity more
     * than a national bidder in the standard setting
     */
    @Test
    public void testProximityBundleValues() {
        LicenseBundle<LSVMLicense> proximity = new LicenseBundle<>();
        proximity.addAll(standardPopulation.get(0).getProximity());
        BigDecimal valueRegionalBidder = standardPopulation.get(0).calculateValue(proximity);
        BigDecimal valueNationalBidder = standardPopulation.get(5).calculateValue(proximity);

        Assert.assertTrue(valueRegionalBidder.compareTo(valueNationalBidder) == 1);

    }

    /**
     * Checks if a regional bidder has indeed zero value for any bundle that doesn't consist of at least
     * one license of her proximity
     */
    @Test
    public void testZeroValueForNonProximityLicenses() {
        LicenseBundle<LSVMLicense> nonProximity = new LicenseBundle<>(completeBundle);
        nonProximity.removeAll(standardPopulation.get(0).getProximity());

        BigDecimal valueNonProximity = standardPopulation.get(0).calculateValue(nonProximity);

        Assert.assertTrue(valueNonProximity.compareTo(BigDecimal.ZERO) == 0);
    }

}

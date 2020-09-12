package org.spectrumauctions.sats.opt.model.gsvm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.marketdesignresearch.mechlib.core.Bundle;
import org.marketdesignresearch.mechlib.core.BundleEntry;
import org.marketdesignresearch.mechlib.core.Domain;
import org.marketdesignresearch.mechlib.core.price.LinearPrices;
import org.marketdesignresearch.mechlib.core.price.Price;
import org.marketdesignresearch.mechlib.core.price.Prices;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMNationalBidderSetup;
import org.spectrumauctions.sats.core.model.gsvm.GSVMRegionalBidderSetup;
import org.spectrumauctions.sats.core.model.gsvm.GSVMWorld;
import org.spectrumauctions.sats.core.model.gsvm.GSVMWorldSetup;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.mechanism.domains.GSVMDomain;

/**
 * @author Fabio Isler
 */
public class GSVMDemandQueryTest {

    private static final Logger logger = LogManager.getLogger(GSVMDemandQueryTest.class);

    @Test
    public void testAllBiddersInGSVM() {
        List<GSVMBidder> bidders = new GlobalSynergyValueModel().createNewWorldAndPopulation(new JavaUtilRNGSupplier(73246104));

        for (GSVMBidder bidder : bidders) {
            Bundle bundle = bidder.getBestBundle(Prices.NONE);
            BigDecimal value = bidder.calculateValue(bundle);
            Price price = Prices.NONE.getPrice(bundle);
            BigDecimal utility = bidder.getUtility(bundle, Prices.NONE);
            Assert.assertTrue(utility.compareTo(BigDecimal.ZERO) > 0);
            logger.info("Bidder {} chooses bundle [{}].\tValue: {}\tPrice: {}\tUtility: {})",
                    bidder.getName(),
                    bundle,
                    value.setScale(2, RoundingMode.HALF_UP),
                    price.getAmount().setScale(2, RoundingMode.HALF_UP),
                    utility.setScale(2, RoundingMode.HALF_UP));
        }
    }

    @Test
    @Ignore // Price generation for GSVM with AllocationLimit takes quite a little time
    public void testMultipleBundles() {
        List<GSVMBidder> bidders = new GlobalSynergyValueModel().createNewWorldAndPopulation(new JavaUtilRNGSupplier(73246104));
        Domain domain = new GSVMDomain(bidders);
        GSVMBidder bidder = bidders.get(0);
        Prices prices = domain.proposeStartingPrices();

        Set<Bundle> bundles = bidder.getBestBundles(prices, 10);
        for (Bundle bundle : bundles) {
            BigDecimal value = bidder.calculateValue(bundle);
            Price price = prices.getPrice(bundle);
            BigDecimal utility = bidder.getUtility(bundle, prices);
            Assert.assertTrue(utility.compareTo(BigDecimal.ZERO) > 0);
            logger.info("Bidder {} chooses bundle [{}].\tValue: {}\tPrice: {}\tUtility: {})",
                    bidder.getName(),
                    bundle,
                    value.setScale(2, RoundingMode.HALF_UP),
                    price.getAmount().setScale(2, RoundingMode.HALF_UP),
                    utility.setScale(2, RoundingMode.HALF_UP));
        }

    }
    
    @Test
    public void testAllBiddersInGSVMOriginal() {
    	GSVMWorldSetup.GSVMWorldSetupBuilder worldSetupBuilder = new GSVMWorldSetup.GSVMWorldSetupBuilder();
		worldSetupBuilder.setSizeInterval(new IntegerInterval(6));
		// Do not allow Assignment of licenses with zero base value
		worldSetupBuilder.setLegacyGSVM(false);
		GSVMWorldSetup setup = worldSetupBuilder.build();
		GSVMWorld world = new GSVMWorld(setup, new JavaUtilRNGSupplier(983749L));

        List<GSVMBidder> customPopulation = customPopulation(world, 8, 2);
        Assert.assertEquals(customPopulation.size(), 10);
        
        // regional bidders
        for(GSVMBidder bidder : customPopulation.subList(0, 8)) {
        	Bundle interestIn = new Bundle(bidder.getBaseValues().entrySet().stream().sorted((a,b) -> -a.getValue().compareTo(b.getValue())).limit(4).map(Map.Entry::getKey).map(l -> new BundleEntry(world.getLicenses().stream().filter(lic -> lic.getLongId() == l).findAny().orElseThrow(), 1)).collect(Collectors.toSet()));
        	Prices prices = new LinearPrices(world.getLicenses().stream().collect(Collectors.toMap(l -> l, l -> new Price(BigDecimal.valueOf(bidder.getBaseValues().containsKey(l.getLongId()) ? 5.0 : 0.1)))));
        	Bundle demandedBundle = bidder.getBestBundle(prices);
        	Assert.assertEquals(interestIn, demandedBundle);
        }
        // regional national
        for(GSVMBidder bidder : customPopulation.subList(8, 10)) {
        	Bundle interestIn = new Bundle(bidder.getBaseValues().entrySet().stream().sorted((a,b) -> -a.getValue().compareTo(b.getValue())).map(Map.Entry::getKey).map(l -> new BundleEntry(world.getLicenses().stream().filter(lic -> lic.getLongId() == l).findAny().orElseThrow(), 1)).collect(Collectors.toSet()));
        	Prices prices = new LinearPrices(world.getLicenses().stream().collect(Collectors.toMap(l -> l, l -> new Price(BigDecimal.valueOf(bidder.getBaseValues().containsKey(l.getLongId()) ? 5.0 : 0.1)))));
        	Bundle demandedBundle = bidder.getBestBundle(prices);
        	Assert.assertEquals(interestIn, demandedBundle);
        }
    }
    
    @Test
    public void testAllBiddersInGSVMLegacy() {
    	GSVMWorldSetup.GSVMWorldSetupBuilder worldSetupBuilder = new GSVMWorldSetup.GSVMWorldSetupBuilder();
		worldSetupBuilder.setSizeInterval(new IntegerInterval(6));
		// Do not allow Assignment of licenses with zero base value
		worldSetupBuilder.setLegacyGSVM(true);
		GSVMWorldSetup setup = worldSetupBuilder.build();
		GSVMWorld world = new GSVMWorld(setup, new JavaUtilRNGSupplier(983749L));

        List<GSVMBidder> customPopulation = customPopulation(world, 8, 2);
        Assert.assertEquals(customPopulation.size(), 10);
        
        for(GSVMBidder bidder : customPopulation) {
        	Prices prices = new LinearPrices(world.getLicenses().stream().collect(Collectors.toMap(l -> l, l -> new Price(BigDecimal.valueOf(bidder.getBaseValues().containsKey(l.getLongId()) ? 5.0 : 0.1)))));
        	Bundle demandedBundle = bidder.getBestBundle(prices);
        	Assert.assertEquals(Bundle.of(world.getLicenses()), demandedBundle);
        }
    }
    
    @Test
    // TODO fix for AllocationLimits
    public void testMaxNumberInGSVMOriginal() {
    	GSVMWorldSetup.GSVMWorldSetupBuilder worldSetupBuilder = new GSVMWorldSetup.GSVMWorldSetupBuilder();
		worldSetupBuilder.setSizeInterval(new IntegerInterval(6));
		// Do not allow Assignment of licenses with zero base value
		worldSetupBuilder.setLegacyGSVM(false);
		GSVMWorldSetup setup = worldSetupBuilder.build();
		GSVMWorld world = new GSVMWorld(setup, new JavaUtilRNGSupplier(983749L));

        List<GSVMBidder> customPopulation = customPopulation(world, 8, 2);
        Assert.assertEquals(customPopulation.size(), 10);
        
        List<GSVMBidder> testbidders = new ArrayList<>();
        
        GSVMBidder regionalBidder = customPopulation.get(2);
        checkBidder(regionalBidder, "Test Regional Bidder");
        testbidders.add(regionalBidder);
        
        GSVMBidder nationalBidder = customPopulation.get(9);
        checkBidder(nationalBidder, "Test National Bidder");
        testbidders.add(nationalBidder);
        
        // only empty bundle a best response (high prices)
        for(GSVMBidder bidder : testbidders) {
        	Prices prices = new LinearPrices(world.getLicenses().stream().collect(Collectors.toMap(l -> l, l -> new Price(BigDecimal.valueOf(bidder.getBaseValues().containsKey(l.getLongId()) ? 500.0 : 0.1)))));
        	Set<Bundle> demandedBundle = bidder.getBestBundles(prices,100);
        	logger.info("{}: {} bundles returned for a demand query of 100 bundles",bidder,demandedBundle.size());
        	Assert.assertEquals(1,demandedBundle.size());
        }
        
        // allow negative with high prices
        for(GSVMBidder bidder : testbidders) {
        	Prices prices = new LinearPrices(world.getLicenses().stream().collect(Collectors.toMap(l -> l, l -> new Price(BigDecimal.valueOf(bidder.getBaseValues().containsKey(l.getLongId()) ? 500.0 : 0.1)))));
        	Set<Bundle> demandedBundle = bidder.getBestBundles(prices, 100, true);
        	logger.info("{}: {} bundles returned for a demand query of 100 bundles",bidder,demandedBundle.size());
        	Assert.assertEquals(Math.min(100,bidder.getAllocationLimit().calculateAllocationBundleSpace(bidder.getBaseValues().keySet().stream().map(l -> world.getLicenses().stream().filter(lic -> lic.getLongId() == l).findAny().orElseThrow()).collect(Collectors.toList()))),demandedBundle.size(),0);
        }
        
        // query with prices lower than value
        for(GSVMBidder bidder : testbidders) {
        	Prices prices = new LinearPrices(world.getLicenses().stream().collect(Collectors.toMap(l -> l, l -> new Price(BigDecimal.valueOf(bidder.getBaseValues().containsKey(l.getLongId()) ? 5.0 : 0.1)))));
        	Set<Bundle> demandedBundle = bidder.getBestBundles(prices, 100, true);
        	logger.info("{}: {} bundles returned for a demand query of 100 bundles",bidder,demandedBundle.size());
        	Assert.assertEquals(Math.min(100,bidder.getAllocationLimit().calculateAllocationBundleSpace(bidder.getBaseValues().keySet().stream().map(l -> world.getLicenses().stream().filter(lic -> lic.getLongId() == l).findAny().orElseThrow()).collect(Collectors.toList()))),demandedBundle.size(),0);
        }
    }
    
    @Test
    public void testMaxNumberInGSVMLegacy() {
    	GSVMWorldSetup.GSVMWorldSetupBuilder worldSetupBuilder = new GSVMWorldSetup.GSVMWorldSetupBuilder();
		worldSetupBuilder.setSizeInterval(new IntegerInterval(6));
		// Do not allow Assignment of licenses with zero base value
		worldSetupBuilder.setLegacyGSVM(true);
		GSVMWorldSetup setup = worldSetupBuilder.build();
		GSVMWorld world = new GSVMWorld(setup, new JavaUtilRNGSupplier(983749L));

        List<GSVMBidder> customPopulation = customPopulation(world, 8, 2);
        Assert.assertEquals(customPopulation.size(), 10);
        
        List<GSVMBidder> testbidders = new ArrayList<>();
        
        GSVMBidder regionalBidder = customPopulation.get(2);
        checkBidder(regionalBidder, "Test Regional Bidder");
        testbidders.add(regionalBidder);
        
        GSVMBidder nationalBidder = customPopulation.get(9);
        checkBidder(nationalBidder, "Test National Bidder");
        testbidders.add(nationalBidder);
        
        
        // only empty bundle a best response (high prices)
        for(GSVMBidder bidder : testbidders) {
        	Prices prices = new LinearPrices(world.getLicenses().stream().collect(Collectors.toMap(l -> l, l -> new Price(BigDecimal.valueOf(bidder.getBaseValues().containsKey(l.getLongId()) ? 500.0 : 0.1)))));
        	Set<Bundle> demandedBundle = bidder.getBestBundles(prices,100);
        	logger.info("{}: {} bundles returned for a demand query of 100 bundles",bidder,demandedBundle.size());
        	Assert.assertEquals(1,demandedBundle.size());
        }
        
        // allow negative with high prices
        for(GSVMBidder bidder : testbidders) {
        	Prices prices = new LinearPrices(world.getLicenses().stream().collect(Collectors.toMap(l -> l, l -> new Price(BigDecimal.valueOf(bidder.getBaseValues().containsKey(l.getLongId()) ? 500.0 : 0.1)))));
        	Set<Bundle> demandedBundle = bidder.getBestBundles(prices, 100, true);
        	logger.info("{}: {} bundles returned for a demand query of 100 bundles",bidder,demandedBundle.size());
        	Assert.assertEquals(100,demandedBundle.size());
        }
        
        // query with prices lower than value
        for(GSVMBidder bidder : testbidders) {
        	Prices prices = new LinearPrices(world.getLicenses().stream().collect(Collectors.toMap(l -> l, l -> new Price(BigDecimal.valueOf(bidder.getBaseValues().containsKey(l.getLongId()) ? 5.0 : 0.1)))));
        	Set<Bundle> demandedBundle = bidder.getBestBundles(prices, 100, true);
        	logger.info("{}: {} bundles returned for a demand query of 100 bundles",bidder,demandedBundle.size());
        	Assert.assertEquals(100,demandedBundle.size());
        }
    }
    
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

package org.spectrumauctions.sats.opt.model.lsvm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.marketdesignresearch.mechlib.core.Allocation;
import org.marketdesignresearch.mechlib.core.Bundle;
import org.spectrumauctions.sats.core.model.lsvm.*;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides some basic unit tests for the LSVMStandardMIP
 *
 * @author Nicolas Küchler
 */
public class LSVMStandardMIPTest {

	private Map<Long, Double> seedMap;

	@Before
	public void setup() {
		seedMap = LSVMStandardMIPTestData.getTestData();
		// get seeds with known efficient allocation value
	}

	@Test
	public void testLegacyDefaultSetupEasySeed() {
		// reference runtime approx 2 seconds
		testLegacyDefaultSetup(1498246131808L);
	}

	@Test
	public void testLegacyDefaultSetupMediumSeed() {
		// reference runtime approx 1 minute
		testLegacyDefaultSetup(1498247338147L);
	}

	@Test
	@Ignore // -> Remove to test the hard seed
	public void testLegacyDefaultSetupHardSeed() {
		// hardest known seed -> reference runtime approx 15 minutes
		testLegacyDefaultSetup(1498249317254L);
	}

	@Test
	public void testEfficientAllocationCustomSetup() {
		LSVMWorldSetup.LSVMWorldSetupBuilder worldSetupBuilder = new LSVMWorldSetup.LSVMWorldSetupBuilder();
		worldSetupBuilder.setLegacyLSVM(true);
		worldSetupBuilder.setNumberOfColumnsInterval(new IntegerInterval(3));
		worldSetupBuilder.setNumberOfRowsInterval(new IntegerInterval(2));
		LSVMWorldSetup setup = worldSetupBuilder.build();
		LSVMWorld world = new LSVMWorld(setup, new JavaUtilRNGSupplier(983742L));
		List<LSVMBidder> population = customPopulation(world, 2, 1);

		LSVMStandardMIP lsvmMIP = new LSVMStandardMIP(world, population);
		Allocation allocation = lsvmMIP.getAllocation();

		testTotalValue(population, allocation);
	}

	private void testLegacyDefaultSetup(Long seed) {
		LocalSynergyValueModel model = new LocalSynergyValueModel();
		model.setLegacyLSVM(true);
		LSVMWorld world = model.createWorld(seed);
		List<LSVMBidder> population = model.createNewPopulation(world, seed);

		LSVMStandardMIP lsvmMIP = new LSVMStandardMIP(world, population);
		Allocation allocation = lsvmMIP.getAllocation();

		Assert.assertEquals("Error Objective Value not matching Test Data Seed: " + seed, seedMap.get(seed),
				allocation.getTotalAllocationValue().doubleValue(), 0.0000001);
		testTotalValue(population, allocation);
	}

	private void testTotalValue(List<LSVMBidder> population, Allocation allocation) {
		BigDecimal totalValue = new BigDecimal(0);

		for (LSVMBidder bidder : population) {
			Bundle bundle = allocation.allocationOf(bidder).getBundle();
			totalValue = totalValue.add(bidder.calculateValue(bundle));
		}

		double delta = 0.0000001;
		Assert.assertEquals("Values of allocated bundles don't match with objectie value of MIP ",
				allocation.getTotalAllocationValue().doubleValue(), totalValue.doubleValue(), delta);
	}

	private List<LSVMBidder> customPopulation(LSVMWorld world, int numberOfRegionalBidders,
			int numberOfNationalBidders) {

		LSVMBidderSetup.RegionalBidderBuilder regionalBidderBuilder = new LSVMBidderSetup.RegionalBidderBuilder();
		regionalBidderBuilder.setProximitySize(1);
		regionalBidderBuilder.setNumberOfBidders(numberOfRegionalBidders);
		regionalBidderBuilder.setSetupName("Test Regional Bidder");
		regionalBidderBuilder.setValueInterval(new DoubleInterval(10));
		regionalBidderBuilder.setLsvmA(0);
		regionalBidderBuilder.setLsvmB(5);

		LSVMBidderSetup.NationalBidderBuilder nationalBidderBuilder = new LSVMBidderSetup.NationalBidderBuilder();
		nationalBidderBuilder.setNumberOfBidders(numberOfNationalBidders);
		nationalBidderBuilder.setValueInterval(new DoubleInterval(4));
		nationalBidderBuilder.setSetupName("Test National Bidder");

		List<LSVMBidderSetup> setups = new ArrayList<>();
		setups.add(regionalBidderBuilder.build());
		setups.add(nationalBidderBuilder.build());
		return world.createPopulation(setups, new JavaUtilRNGSupplier(983742L));
	}

}

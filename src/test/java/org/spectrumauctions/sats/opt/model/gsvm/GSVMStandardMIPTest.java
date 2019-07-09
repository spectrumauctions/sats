package org.spectrumauctions.sats.opt.model.gsvm;

import org.junit.Assert;
import org.junit.Test;
import org.marketdesignresearch.mechlib.domain.Allocation;
import org.marketdesignresearch.mechlib.domain.Bundle;
import org.marketdesignresearch.mechlib.domain.Good;
import org.spectrumauctions.sats.core.model.gsvm.*;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.math.BigDecimal;
import java.util.*;

public class GSVMStandardMIPTest {

    @Test
    public void testDeterministicOutcome() {
        GlobalSynergyValueModel model = new GlobalSynergyValueModel();
        GSVMWorld world = model.createWorld();
        List<GSVMBidder> population = model.createPopulation(world);

        GSVMStandardMIP gsvmMIP = new GSVMStandardMIP(world, population);
        Allocation allocation = gsvmMIP.getAllocation();

        GSVMStandardMIP secondGsvmMIP = new GSVMStandardMIP(world, population);
        Allocation secondAllocation = secondGsvmMIP.getAllocation();

        population.forEach(bidder -> Assert.assertEquals(allocation.allocationOf(bidder), secondAllocation.allocationOf(bidder)));
    }

	@Test
    public void testDifferentOrderOfBidders() {
        GlobalSynergyValueModel model = new GlobalSynergyValueModel();
        GSVMWorld world = model.createWorld();
        List<GSVMBidder> population = model.createPopulation(world);
        List<GSVMBidder> invertedPopulation = new ArrayList<>(population);
        Collections.reverse(population);

        Assert.assertEquals(population.get(0), invertedPopulation.get(invertedPopulation.size() - 1));

        GSVMStandardMIP gsvmMIP = new GSVMStandardMIP(world, population);
		Allocation allocation = gsvmMIP.getAllocation();

        GSVMStandardMIP invertedGsvmMIP = new GSVMStandardMIP(world, invertedPopulation);
		Allocation invertedAllocation = invertedGsvmMIP.getAllocation();

        population.forEach(bidder -> Assert.assertEquals(allocation.allocationOf(bidder), invertedAllocation.allocationOf(bidder)));
    }

	@Test
	public void testValidAllocationDefaultSetup() {
		GlobalSynergyValueModel model = new GlobalSynergyValueModel();
		GSVMWorld world = model.createWorld();
		List<GSVMBidder> population = model.createPopulation(world);

		GSVMStandardMIP gsvmMIP = new GSVMStandardMIP(world, population);
		Allocation allocation = gsvmMIP.getAllocation();

		Map<Good, GSVMBidder> invertedAllocation = new HashMap<>();

		for (GSVMBidder bidder : population) {
			Bundle bundle = allocation.allocationOf(bidder).getBundle();
			for (Good license : bundle.getSingleAvailabilityGoods()) {
				// Checks if same license is allocated multiple times
				Assert.assertFalse("Invalid Allocation, same license allocated multiple times", invertedAllocation.containsKey(license));
				invertedAllocation.put(license, bidder);
			}
		}
		testTotalValue(population, allocation);
	}

	@Test
	public void testEfficientAllocationCustomSetup() {
		GSVMWorldSetup.GSVMWorldSetupBuilder worldSetupBuilder = new GSVMWorldSetup.GSVMWorldSetupBuilder();
		worldSetupBuilder.setSizeInterval(new IntegerInterval(1));
		GSVMWorldSetup setup = worldSetupBuilder.build();
		GSVMWorld world = new GSVMWorld(setup, new JavaUtilRNGSupplier(983742L));
		List<GSVMBidder> population = customPopulation(world, 2, 1);

		GSVMStandardMIP gsvmMIP = new GSVMStandardMIP(world, population);
		Allocation allocation = gsvmMIP.getAllocation();

		// Efficient Allocation should be 91.0
		Assert.assertEquals(0, BigDecimal.valueOf(91.0).compareTo(allocation.getTotalAllocationValue()));
		testTotalValue(population, allocation);
	}

	@Test
	public void testEfficientAllocationWhenAllowingToAssignLicensesToAgentsWithZeroBasevalue() {
		GSVMWorldSetup.GSVMWorldSetupBuilder worldSetupBuilder = new GSVMWorldSetup.GSVMWorldSetupBuilder();
		worldSetupBuilder.setSizeInterval(new IntegerInterval(6));
		GSVMWorldSetup setup = worldSetupBuilder.build();
		GSVMWorld world = new GSVMWorld(setup, new JavaUtilRNGSupplier(983742L));
		List<GSVMBidder> population = buildSpecialPopulation(world);

		GSVMStandardMIP gsvmMIP = new GSVMStandardMIP(world, population, true);
		Allocation allocation = gsvmMIP.getAllocation();

		GSVMBidder nationalBidder = population.stream()
				.filter(bidder -> bidder.getSetupType().equals("Test National Bidder")).findFirst().get();

		Bundle fullBundle = Bundle.singleGoods(world.getLicenses());

		// the efficient allocation is giving all licenses (including the
		// licenses of the regional circle) to the one national bidder
		Assert.assertEquals(fullBundle, allocation.allocationOf(nationalBidder).getBundle());

		testTotalValue(population, allocation);
	}

	@Test
	public void testEfficientAllocationWhenNotAllowingToAssignLicensesToAgentsWithZeroBasevalue() {
		GSVMWorldSetup.GSVMWorldSetupBuilder worldSetupBuilder = new GSVMWorldSetup.GSVMWorldSetupBuilder();
		worldSetupBuilder.setSizeInterval(new IntegerInterval(6));
		GSVMWorldSetup setup = worldSetupBuilder.build();
		GSVMWorld world = new GSVMWorld(setup, new JavaUtilRNGSupplier(983742L));
		List<GSVMBidder> population = buildSpecialPopulation(world);

		// use only licenses with positive values
		GSVMStandardMIP gsvmMIP = new GSVMStandardMIP(world, population, false);
		Allocation allocation = gsvmMIP.getAllocation();

		GSVMBidder nationalBidder = population.stream()
				.filter(bidder -> bidder.getSetupType().equals("Test National Bidder")).findFirst().get();

		Set<GSVMLicense> fullBundle = new HashSet<>(world.getLicenses());

		// the efficient allocation is not giving all licenses to the one national bidder
		Assert.assertTrue(fullBundle.size() > allocation.allocationOf(nationalBidder).getBundle().getSingleAvailabilityGoods().size());

		testTotalValue(population, allocation);
	}

	private void testTotalValue(List<GSVMBidder> population, Allocation allocation) {
		BigDecimal totalValue = new BigDecimal(0);

		for (GSVMBidder bidder : population) {
			Bundle bundle = allocation.allocationOf(bidder).getBundle();
			totalValue = totalValue.add(bidder.calculateValue(bundle));
		}

		double delta = 0.00000001;
		Assert.assertEquals("Values of allocated bundles don't match with objective value of MIP ",
				allocation.getTotalAllocationValue().doubleValue(), totalValue.doubleValue(), delta);
	}

	private List<GSVMBidder> customPopulation(GSVMWorld world, int numberOfRegionalBidders,
			int numberOfNationalBidders) {

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

	/*
	 * builds a population with one national bidder with huge values and only
	 * one regional bidder with really low values
	 */
	private List<GSVMBidder> buildSpecialPopulation(GSVMWorld world) {
		GSVMRegionalBidderSetup.Builder regionalBidderBuilder = new GSVMRegionalBidderSetup.Builder();
		regionalBidderBuilder.setRegionalValueInterval(new DoubleInterval(0.001));
		regionalBidderBuilder.setNumberOfBidders(1);
		regionalBidderBuilder.setSetupName("Test Regional Bidder");

		GSVMNationalBidderSetup.Builder nationalBidderBuilder = new GSVMNationalBidderSetup.Builder();
		nationalBidderBuilder.setNumberOfBidders(1);
		nationalBidderBuilder.setLowNationalValueInterval(new DoubleInterval(1000));
		nationalBidderBuilder.setHighNationalValueInterval(new DoubleInterval(2000));
		nationalBidderBuilder.setSetupName("Test National Bidder");

		Collection<GSVMRegionalBidderSetup> regionalSetups = new ArrayList<>();
		regionalSetups.add(regionalBidderBuilder.build());
		Collection<GSVMNationalBidderSetup> nationalSetups = new ArrayList<>();
		nationalSetups.add(nationalBidderBuilder.build());

		return world.createPopulation(regionalSetups, nationalSetups,
				new JavaUtilRNGSupplier(983742L));
	}
}

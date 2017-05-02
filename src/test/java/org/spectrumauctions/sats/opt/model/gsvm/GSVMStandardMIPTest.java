package org.spectrumauctions.sats.opt.model.gsvm;

import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.gsvm.*;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.opt.vcg.external.vcg.ItemAllocation;

import java.math.BigDecimal;
import java.util.*;

public class GSVMStandardMIPTest {

    @Test
    public void testValidAllocationDefaultSetup() {
        GlobalSynergyValueModel model = new GlobalSynergyValueModel();
        GSVMWorld world = model.createWorld();
        List<GSVMBidder> population = model.createPopulation(world);

        GSVMStandardMIP gsvmMIP = new GSVMStandardMIP(world, population);
        gsvmMIP.build();
        ItemAllocation<GSVMLicense> allocation = gsvmMIP.calculateAllocation();

        Map<GSVMLicense, GSVMBidder> invertedAllocation = new HashMap<>();

        for (GSVMBidder bidder : population) {
            Bundle<GSVMLicense> bundle = allocation.getAllocation(bidder);
            for (GSVMLicense license : bundle) {
                // Checks if same license is allocated multiple times
                Assert.assertTrue("Invalid Allocation, same license allocated multiple times",
                        !invertedAllocation.containsKey(license));
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
        gsvmMIP.build();
        ItemAllocation<GSVMLicense> allocation = gsvmMIP.calculateAllocation();

        // Efficient Allocation should be 91.0
        Assert.assertEquals(0, BigDecimal.valueOf(91.0).compareTo(allocation.getTotalValue()));
        testTotalValue(population, allocation);
    }

    private void testTotalValue(List<GSVMBidder> population, ItemAllocation<GSVMLicense> allocation) {
        BigDecimal totalValue = new BigDecimal(0);

        for (GSVMBidder bidder : population) {
            Bundle<GSVMLicense> bundle = allocation.getAllocation(bidder);
            totalValue = totalValue.add(bidder.calculateValue(bundle));
        }

        double delta = 0.00000001;
        Assert.assertEquals("Values of allocated bundles don't match with objectie value of MIP ",
                allocation.getTotalValue().doubleValue(), totalValue.doubleValue(), delta);
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

}

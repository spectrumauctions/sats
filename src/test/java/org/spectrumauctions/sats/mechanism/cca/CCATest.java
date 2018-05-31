package org.spectrumauctions.sats.mechanism.cca;

import com.google.common.collect.Sets;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.DemandDependentPriceUpdate;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.SimpleRelativePriceUpdate;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.ProfitMaximizingSupplementaryRound;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.model.mrvm.MRVMMipResult;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;
import org.spectrumauctions.sats.opt.model.mrvm.demandquery.MRVM_DemandQueryMIPBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class CCATest {

    private static final Logger logger = LogManager.getLogger(CCATest.class);

    private static int ITERATIONS = 5;

    @Test
    public void testMultipleCCASimplePriceUpdate() {
        ArrayList<Double> qualities = new ArrayList<>();
        for (int i = 0; i < ITERATIONS; i++) {
            logger.info("Starting round {} of {}...", i + 1, ITERATIONS);
            List<MRVMBidder> rawBidders = new MultiRegionModel().createNewPopulation();
            MRVM_MIP mip = new MRVM_MIP(Sets.newHashSet(rawBidders));
            MRVMMipResult efficientAllocation = mip.calculateAllocation();
            qualities.add(testCCAWithMRVMSimplePriceUpdate(rawBidders, efficientAllocation));
        }
        DescriptiveStatistics stats = new DescriptiveStatistics();
        qualities.forEach(stats::addValue);
        logger.warn(stats.toString());
    }

    private double testCCAWithMRVMSimplePriceUpdate(List<MRVMBidder> rawBidders, MRVMMipResult efficientAllocation) {
        long start = System.currentTimeMillis();
        List<Bidder<MRVMLicense>> bidders = rawBidders.stream()
                .map(b -> (Bidder<MRVMLicense>) b).collect(Collectors.toList());
        CCAMechanism<MRVMLicense> cca = new CCAMechanism<>(bidders, new MRVM_DemandQueryMIPBuilder());
        cca.setVariant(CCAVariant.XORQ);
        cca.setStartingPrice(BigDecimal.ZERO);
        cca.setEpsilon(0.001);

        SimpleRelativePriceUpdate<MRVMLicense> priceUpdater = new SimpleRelativePriceUpdate<>();
        priceUpdater.setPriceUpdate(BigDecimal.valueOf(0.05));
        cca.setPriceUpdater(priceUpdater);

        ProfitMaximizingSupplementaryRound<MRVMLicense> supplementaryRound = new ProfitMaximizingSupplementaryRound<>();
        supplementaryRound.setNumberOfSupplementaryBids(50);
        cca.setSupplementaryRound(supplementaryRound);

        MechanismResult<MRVMLicense> result = cca.getMechanismResult();
        long end = System.currentTimeMillis();

        logger.info("Total rounds: {}", cca.getTotalRounds());
        logger.info("(Supply - Demand) of final round: {}", cca.getSupplyMinusDemand());
        logger.info("Generic bids count per bidder:\n{}", cca.getGenericBidsCount());
        logger.info("CCA took {}s.", (end - start) / 1000);

        Allocation<MRVMLicense> allocationFromMechanism = result.getAllocation();
        Allocation<MRVMLicense> allocationWithTrueValues = allocationFromMechanism.getAllocationWithTrueValues();
        assertTrue(result.getAllocation().getTotalValue().doubleValue() > 0);
        assertNotEquals(allocationFromMechanism, allocationWithTrueValues);

        // Compare allocations:
        BigDecimal quality = allocationWithTrueValues.getTotalValue().divide(efficientAllocation.getTotalValue(), RoundingMode.HALF_UP);
        logger.info("Quality of this iteration: {}", quality);
        return quality.doubleValue();
    }

    @Test
    @Ignore // It doesn't terminate for now
    public void testCCAWithMRVMComplexPriceUpdate() {
        List<MRVMBidder> rawBidders = new MultiRegionModel().createNewPopulation(234456867);
        MRVM_MIP mip = new MRVM_MIP(Sets.newHashSet(rawBidders));
        logger.info("Finding optimal solution first as comparison...");
        MRVMMipResult efficientAllocation = mip.calculateAllocation();
        logger.info("Optimal solution found. Starting CCA...");

        long start = System.currentTimeMillis();
        List<Bidder<MRVMLicense>> bidders = rawBidders.stream()
                .map(b -> (Bidder<MRVMLicense>) b).collect(Collectors.toList());

        // Find max value for price update
        BigDecimal maxValue = BigDecimal.valueOf(-1);
        for (MRVMBidder bidder : rawBidders) {
            BigDecimal value = bidder.calculateValue(new Bundle<>(bidder.getWorld().getLicenses()));
            if (value.compareTo(maxValue) > 0) maxValue = value;
        }
        // Define the price update behavior
        DemandDependentPriceUpdate<MRVMLicense> priceUpdater = new DemandDependentPriceUpdate<>();
        priceUpdater.setConstant(maxValue.divide(BigDecimal.valueOf(1e6), RoundingMode.HALF_UP));

        CCAMechanism<MRVMLicense> cca = new CCAMechanism<>(bidders, new MRVM_DemandQueryMIPBuilder());
        cca.setPriceUpdater(priceUpdater);
        cca.setVariant(CCAVariant.XORQ);
        cca.setStartingPrice(BigDecimal.ZERO);
        cca.setEpsilon(0.1);
        MechanismResult<MRVMLicense> result = cca.getMechanismResult();
        long end = System.currentTimeMillis();
        logger.warn("CCA took {}s.", (end - start) / 1000);
        Allocation<MRVMLicense> allocationFromMechanism = result.getAllocation();
        Allocation<MRVMLicense> allocationWithTrueValues = allocationFromMechanism.getAllocationWithTrueValues();
        assertTrue(allocationFromMechanism.getTotalValue().doubleValue() > 0);
        assertNotEquals(allocationFromMechanism, allocationWithTrueValues);

        // Compare allocations:
        BigDecimal quality = allocationWithTrueValues.getTotalValue().divide(efficientAllocation.getTotalValue(), RoundingMode.HALF_UP);
        logger.warn("Quality of this iteration: {}", quality);
    }
}

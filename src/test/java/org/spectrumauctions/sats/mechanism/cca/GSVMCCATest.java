package org.spectrumauctions.sats.mechanism.cca;

import com.google.common.collect.Lists;
import edu.harvard.econcs.jopt.solver.SolveParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.model.mrvm.MRVMGenericDefinition;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLicense;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.SimpleRelativeGenericPriceUpdate;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.SimpleRelativeNonGenericPriceUpdate;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.ProfitMaximizingGenericSupplementaryRound;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.ProfitMaximizingNonGenericSupplementaryRound;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.ItemAllocation;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;
import org.spectrumauctions.sats.opt.model.gsvm.demandquery.GSVM_DemandQueryMIPBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class GSVMCCATest {

    private static final Logger logger = LogManager.getLogger(GSVMCCATest.class);

    private static int ITERATIONS = 5;

    @Test
    public void testMultipleInstances() {
        for (int i = 0; i < ITERATIONS; i++) {
            logger.info("Starting round {} of {}...", i + 1, ITERATIONS);
            testClockPhaseVsSupplementaryPhaseEfficiency();
        }
    }

    private void testClockPhaseVsSupplementaryPhaseEfficiency() {
        List<GSVMBidder> rawBidders = new GlobalSynergyValueModel().createNewPopulation();
        GSVMStandardMIP mip = new GSVMStandardMIP(Lists.newArrayList(rawBidders));
        mip.getMip().setSolveParam(SolveParam.RELATIVE_OBJ_GAP, 0.01);
        ItemAllocation<GSVMLicense> efficientAllocation = mip.calculateAllocation();

        long start = System.currentTimeMillis();
        NonGenericCCAMechanism<GSVMLicense> cca = getMechanism(rawBidders);

        Allocation<GSVMLicense> allocationAfterClockPhase = cca.calculateClockPhaseAllocation();
        Allocation<GSVMLicense> allocCP = allocationAfterClockPhase.getAllocationWithTrueValues();
        //assertNotEquals(allocationAfterClockPhase, allocCP);

        Allocation<GSVMLicense> allocationAfterSupplementaryRound = cca.calculateAllocationAfterSupplementaryRound();
        Allocation<GSVMLicense> allocSR = allocationAfterSupplementaryRound.getAllocationWithTrueValues();
        //assertNotEquals(allocationAfterSupplementaryRound, allocSR);
        long end = System.currentTimeMillis();

        logger.info("Total rounds: {}", cca.getTotalRounds());
        logger.info("(Supply - Demand) of final round: {}", cca.getSupplyMinusDemand());
        logger.info("Generic bids after clock phase per bidder: {}", cca.getBidCountAfterClockPhase());
        logger.info("Generic bids after supplementary round per bidder: {}", cca.getBidCountAfterSupplementaryRound());
        logger.info("CCA took {}s.", (end - start) / 1000);

        BigDecimal qualityCP = allocCP.getTotalValue().divide(efficientAllocation.getTotalValue(), RoundingMode.HALF_UP);
        logger.info("Quality after clock phase: {}", qualityCP.setScale(4, RoundingMode.HALF_UP));

        BigDecimal qualitySR = allocSR.getTotalValue().divide(efficientAllocation.getTotalValue(), RoundingMode.HALF_UP);
        logger.info("Quality with supplementary round: {}", qualitySR.setScale(4, RoundingMode.HALF_UP));

        assertTrue(qualityCP.compareTo(qualitySR) < 0);
    }

    private NonGenericCCAMechanism<GSVMLicense> getMechanism(List<GSVMBidder> rawBidders) {
        List<Bidder<GSVMLicense>> bidders = rawBidders.stream()
                .map(b -> (Bidder<GSVMLicense>) b).collect(Collectors.toList());
        NonGenericCCAMechanism<GSVMLicense> cca = new NonGenericCCAMechanism<>(bidders, new GSVM_DemandQueryMIPBuilder());
        cca.setStartingPrice(BigDecimal.ZERO);
        cca.setEpsilon(0.01);

        SimpleRelativeNonGenericPriceUpdate<GSVMLicense> priceUpdater = new SimpleRelativeNonGenericPriceUpdate<>();
        priceUpdater.setPriceUpdate(BigDecimal.valueOf(0.1));
        priceUpdater.setInitialUpdate(BigDecimal.valueOf(1));
        cca.setPriceUpdater(priceUpdater);

        ProfitMaximizingNonGenericSupplementaryRound<GSVMLicense> supplementaryRound = new ProfitMaximizingNonGenericSupplementaryRound<>();
        supplementaryRound.setNumberOfSupplementaryBids(500);
        cca.setSupplementaryRound(supplementaryRound);

        return cca;
    }

/*    @Test
    public void testMultipleCCASimplePriceUpdate() {
        ArrayList<Double> qualities = new ArrayList<>();
        for (int i = 0; i < ITERATIONS; i++) {
            logger.info("Starting round {} of {}...", i + 1, ITERATIONS);
            List<MRVMBidder> rawBidders = new MultiRegionModel().createNewPopulation();
            MRVM_MIP mip = new MRVM_MIP(Sets.newHashSet(rawBidders));
            mip.setEpsilon(0.01);
            MRVMMipResult efficientAllocation = mip.calculateAllocation();
            qualities.add(testCCAWithMRVMSimplePriceUpdate(rawBidders, efficientAllocation));
        }
        DescriptiveStatistics stats = new DescriptiveStatistics();
        qualities.forEach(stats::addValue);
        logger.warn(stats.toString());
    }

    private double testCCAWithMRVMSimplePriceUpdate(List<MRVMBidder> rawBidders, MRVMMipResult efficientAllocation) {
        long start = System.currentTimeMillis();
        CCAMechanism<MRVMLicense> cca = getMechanism(rawBidders);
        MechanismResult<MRVMLicense> result = cca.getMechanismResult();
        long end = System.currentTimeMillis();

        logger.info("Total rounds: {}", cca.getTotalRounds());
        logger.info("(Supply - Demand) of final round: {}", cca.getSupplyMinusDemand());
        logger.info("Generic bids count per bidder: {}", cca.getGenericBidCountAfterClockPhase());
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
        DemandDependentGenericPriceUpdate<MRVMLicense> priceUpdater = new DemandDependentGenericPriceUpdate<>();
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
    }*/
}

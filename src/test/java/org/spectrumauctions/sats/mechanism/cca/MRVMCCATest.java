package org.spectrumauctions.sats.mechanism.cca;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.SimpleRelativeGenericPriceUpdate;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.ProfitMaximizingGenericSupplementaryRound;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.model.mrvm.MRVMMipResult;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;
import org.spectrumauctions.sats.opt.model.mrvm.demandquery.MRVM_DemandQueryMIPBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MRVMCCATest {

    private static final Logger logger = LogManager.getLogger(MRVMCCATest.class);

    private static int ITERATIONS = 5;

    @Test
    public void testMultipleInstances() {
        for (int i = 0; i < ITERATIONS; i++) {
            logger.info("Starting round {} of {}...", i + 1, ITERATIONS);
            testClockPhaseVsSupplementaryPhaseEfficiency();
        }
    }

    private void testClockPhaseVsSupplementaryPhaseEfficiency() {
        List<MRVMBidder> rawBidders = new MultiRegionModel().createNewPopulation();
        MRVM_MIP mip = new MRVM_MIP(Sets.newHashSet(rawBidders));
        mip.setEpsilon(0.01);
        MRVMMipResult efficientAllocation = mip.calculateAllocation();

        long start = System.currentTimeMillis();
        GenericCCAMechanism<MRVMGenericDefinition, MRVMLicense> cca = getMechanism(rawBidders);

        Allocation<MRVMLicense> allocationAfterClockPhase = cca.calculateClockPhaseAllocation();
        Allocation<MRVMLicense> allocCP = allocationAfterClockPhase.getAllocationWithTrueValues();
        assertNotEquals(allocationAfterClockPhase, allocCP);

        Allocation<MRVMLicense> allocationAfterSupplementaryRound = cca.calculateAllocationAfterSupplementaryRound();
        Allocation<MRVMLicense> allocSR = allocationAfterSupplementaryRound.getAllocationWithTrueValues();
        assertNotEquals(allocationAfterSupplementaryRound, allocSR);
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

    private GenericCCAMechanism<MRVMGenericDefinition, MRVMLicense> getMechanism(List<MRVMBidder> rawBidders) {
        List<Bidder<MRVMLicense>> bidders = rawBidders.stream()
                .map(b -> (Bidder<MRVMLicense>) b).collect(Collectors.toList());
        GenericCCAMechanism<MRVMGenericDefinition, MRVMLicense> cca = new GenericCCAMechanism<>(bidders, new MRVM_DemandQueryMIPBuilder());
        cca.setStartingPrice(BigDecimal.ZERO);
        cca.setEpsilon(0.01);

        SimpleRelativeGenericPriceUpdate<MRVMGenericDefinition, MRVMLicense> priceUpdater = new SimpleRelativeGenericPriceUpdate<>();
        priceUpdater.setPriceUpdate(BigDecimal.valueOf(0.1));
        cca.setPriceUpdater(priceUpdater);

        ProfitMaximizingGenericSupplementaryRound<MRVMGenericDefinition, MRVMLicense> supplementaryRound = new ProfitMaximizingGenericSupplementaryRound<>();
        supplementaryRound.setNumberOfSupplementaryBids(500);
        cca.setSupplementaryRound(supplementaryRound);

        return cca;
    }

}

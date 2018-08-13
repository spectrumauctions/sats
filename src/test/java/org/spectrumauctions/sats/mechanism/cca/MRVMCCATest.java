package org.spectrumauctions.sats.mechanism.cca;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.GenericBid;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.mechanism.PaymentRuleEnum;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.DemandDependentGenericPriceUpdate;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.SimpleRelativeGenericPriceUpdate;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.LastBidsTrueValueGenericSupplementaryRound;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.ProfitMaximizingGenericSupplementaryRound;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.model.mrvm.MRVMMipResult;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;
import org.spectrumauctions.sats.opt.model.mrvm.demandquery.MRVM_DemandQueryMIPBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MRVMCCATest {

    private static final Logger logger = LogManager.getLogger(MRVMCCATest.class);

    private static int ITERATIONS = 5;

    @Test
    @Ignore
    public void testMultipleInstances() {
        for (int i = 0; i < ITERATIONS; i++) {
            logger.info("Starting round {} of {}...", i + 1, ITERATIONS);
            testClockPhaseVsSupplementaryPhaseEfficiency();
        }
    }

    private void testClockPhaseVsSupplementaryPhaseEfficiency() {
        List<MRVMBidder> rawBidders = new MultiRegionModel().createNewPopulation();
        MRVM_MIP mip = new MRVM_MIP(Sets.newHashSet(rawBidders));
        mip.setEpsilon(1e-5);
        MRVMMipResult efficientAllocation = mip.calculateAllocation();
        Allocation<MRVMLicense> efficientAllocationWithTrueValues = efficientAllocation.getAllocationWithTrueValues();
        double diff = efficientAllocation.getTotalValue().doubleValue() - efficientAllocationWithTrueValues.getTotalValue().doubleValue();
        assertTrue(diff > -1e-6 && diff < 1e-6);

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

        MechanismResult<MRVMLicense> result = cca.getMechanismResult();
    }

    private GenericCCAMechanism<MRVMGenericDefinition, MRVMLicense> getMechanism(List<MRVMBidder> rawBidders) {
        List<Bidder<MRVMLicense>> bidders = rawBidders.stream()
                .map(b -> (Bidder<MRVMLicense>) b).collect(Collectors.toList());
        GenericCCAMechanism<MRVMGenericDefinition, MRVMLicense> cca = new GenericCCAMechanism<>(bidders, new MRVM_DemandQueryMIPBuilder());
        cca.setStartingPrice(BigDecimal.ZERO);
        cca.setEpsilon(1e-5);

        SimpleRelativeGenericPriceUpdate<MRVMGenericDefinition, MRVMLicense> priceUpdater = new SimpleRelativeGenericPriceUpdate<>();
        priceUpdater.setPriceUpdate(BigDecimal.valueOf(0.1));
        cca.setPriceUpdater(priceUpdater);

        ProfitMaximizingGenericSupplementaryRound<MRVMGenericDefinition, MRVMLicense> supplementaryRound = new ProfitMaximizingGenericSupplementaryRound<>();
        supplementaryRound.setNumberOfSupplementaryBids(500);
        cca.addSupplementaryRound(supplementaryRound);

        return cca;
    }

    // This method simply shows what settings can be changed
    private GenericCCAMechanism<MRVMGenericDefinition, MRVMLicense> getCustomMechanism(List<MRVMBidder> rawBidders) {
        List<Bidder<MRVMLicense>> bidders = rawBidders.stream()
                .map(b -> (Bidder<MRVMLicense>) b).collect(Collectors.toList());
        GenericCCAMechanism<MRVMGenericDefinition, MRVMLicense> cca = new GenericCCAMechanism<>(bidders, new MRVM_DemandQueryMIPBuilder());
        cca.setStartingPrice(BigDecimal.ZERO);
        cca.setEpsilon(1e-5);
        cca.setPaymentRule(PaymentRuleEnum.CCG);
        cca.setClockPhaseNumberOfBundles(3);
        cca.setMaxRounds(500);

        SimpleRelativeGenericPriceUpdate<MRVMGenericDefinition, MRVMLicense> priceUpdater = new SimpleRelativeGenericPriceUpdate<>();
        priceUpdater.setPriceUpdate(BigDecimal.valueOf(0.1));
        priceUpdater.setInitialUpdate(BigDecimal.valueOf(2e5));
        cca.setPriceUpdater(priceUpdater);

        // Or:
        DemandDependentGenericPriceUpdate<MRVMGenericDefinition, MRVMLicense> priceUpdaterAlternative = new DemandDependentGenericPriceUpdate<>();
        priceUpdaterAlternative.setConstant(BigDecimal.valueOf(5e5));

        ProfitMaximizingGenericSupplementaryRound<MRVMGenericDefinition, MRVMLicense> supplementaryRound = new ProfitMaximizingGenericSupplementaryRound<>();
        supplementaryRound.setNumberOfSupplementaryBids(500);
        cca.addSupplementaryRound(supplementaryRound);

        return cca;
    }

    @Test
    public void testMultipleSupplementaryRounds() {
        List<MRVMBidder> rawBidders = new MultiRegionModel().createNewPopulation();
        GenericCCAMechanism<MRVMGenericDefinition, MRVMLicense> cca = getMechanism(rawBidders);

        ProfitMaximizingGenericSupplementaryRound<MRVMGenericDefinition, MRVMLicense> supplementaryRoundLastPrices = new ProfitMaximizingGenericSupplementaryRound<>();
        supplementaryRoundLastPrices.setNumberOfSupplementaryBids(150);
        supplementaryRoundLastPrices.useLastDemandedPrices(true);
        cca.addSupplementaryRound(supplementaryRoundLastPrices);

        Allocation<MRVMLicense> allocationAfterSupplementaryRound = cca.calculateAllocationAfterSupplementaryRound();
        rawBidders.forEach(b -> assertEquals(cca.getBidCountAfterSupplementaryRound().get(b) - cca.getBidCountAfterClockPhase().get(b), 650));
    }

    @Test
    public void testLastBidsSupplementaryRound() {
        List<MRVMBidder> rawBidders = new MultiRegionModel().createNewPopulation();
        List<Bidder<MRVMLicense>> bidders = rawBidders.stream()
                .map(b -> (Bidder<MRVMLicense>) b).collect(Collectors.toList());
        GenericCCAMechanism<MRVMGenericDefinition, MRVMLicense> cca = new GenericCCAMechanism<>(bidders, new MRVM_DemandQueryMIPBuilder());
        cca.setStartingPrice(BigDecimal.ZERO);
        cca.setEpsilon(1e-5);

        SimpleRelativeGenericPriceUpdate<MRVMGenericDefinition, MRVMLicense> priceUpdater = new SimpleRelativeGenericPriceUpdate<>();
        priceUpdater.setPriceUpdate(BigDecimal.valueOf(0.1));
        cca.setPriceUpdater(priceUpdater);

        LastBidsTrueValueGenericSupplementaryRound<MRVMGenericDefinition, MRVMLicense> lastBidsSupplementaryRound = new LastBidsTrueValueGenericSupplementaryRound<>();
        lastBidsSupplementaryRound.setNumberOfSupplementaryBids(10);
        cca.addSupplementaryRound(lastBidsSupplementaryRound);

        Allocation<MRVMLicense> allocationAfterSupplementaryRound = cca.calculateAllocationAfterSupplementaryRound();
        for (MRVMBidder bidder : rawBidders) {
            GenericBid<MRVMGenericDefinition, MRVMLicense> bid = cca.getBidAfterSupplementaryRound(bidder);
            int maxBids = Math.min(10, bid.getValues().size() / 2);
            int count = 0;
            for (int i = bid.getValues().size() - 1; i > 0 && count++ < maxBids; i--) {
                GenericValue<MRVMGenericDefinition, MRVMLicense> current = bid.getValues().get(i);
                int baseIndex = 2 * bid.getValues().size() - 2*maxBids - 1 - i;
                GenericValue<MRVMGenericDefinition, MRVMLicense> base = bid.getValues().get(baseIndex);
                assertEquals(current.getQuantities(), base.getQuantities());
                assertTrue(current.getValue().compareTo(base.getValue()) > 0);
            }
        }
    }

}

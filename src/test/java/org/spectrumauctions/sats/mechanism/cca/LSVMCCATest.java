package org.spectrumauctions.sats.mechanism.cca;

import com.google.common.collect.Lists;
import edu.harvard.econcs.jopt.solver.SolveParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;
import org.spectrumauctions.sats.core.model.lsvm.LocalSynergyValueModel;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.SimpleRelativeNonGenericPriceUpdate;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.LastBidsTrueValueNonGenericSupplementaryRound;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.ProfitMaximizingNonGenericSupplementaryRound;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.ItemAllocation;
import org.spectrumauctions.sats.opt.model.lsvm.LSVMStandardMIP;
import org.spectrumauctions.sats.opt.model.lsvm.demandquery.LSVM_DemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.xor.XORWinnerDetermination;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class LSVMCCATest {

    private static final Logger logger = LogManager.getLogger(LSVMCCATest.class);

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
        List<LSVMBidder> rawBidders = new LocalSynergyValueModel().createNewPopulation();
        LSVMStandardMIP mip = new LSVMStandardMIP(Lists.newArrayList(rawBidders));
        mip.getMip().setSolveParam(SolveParam.RELATIVE_OBJ_GAP, 1e-5);
        ItemAllocation<LSVMLicense> efficientAllocation = mip.calculateAllocation();
        Allocation<LSVMLicense> efficientAllocationWithTrueValues = efficientAllocation.getAllocationWithTrueValues();
        double diff = efficientAllocation.getTotalValue().doubleValue() - efficientAllocationWithTrueValues.getTotalValue().doubleValue();
        assertTrue(diff > -1e-6 && diff < 1e-6);

        long start = System.currentTimeMillis();
        NonGenericCCAMechanism<LSVMLicense> cca = getMechanism(rawBidders);

        Allocation<LSVMLicense> allocationAfterClockPhase = cca.calculateClockPhaseAllocation();
        Allocation<LSVMLicense> allocCP = allocationAfterClockPhase.getAllocationWithTrueValues();
        assertNotEquals(allocationAfterClockPhase, allocCP);

        Allocation<LSVMLicense> allocationAfterSupplementaryRound = cca.calculateAllocationAfterSupplementaryRound();
        Allocation<LSVMLicense> allocSR = allocationAfterSupplementaryRound.getAllocationWithTrueValues();
        assertNotEquals(allocationAfterSupplementaryRound, allocSR);
        long end = System.currentTimeMillis();

        logger.info("Total rounds: {}", cca.getTotalRounds());
        logger.info("(Supply - Demand) of final round: {}", cca.getSupplyMinusDemand());
        logger.info("Bids after clock phase per bidder: {}", cca.getBidCountAfterClockPhase());
        logger.info("Bids after supplementary round per bidder: {}", cca.getBidCountAfterSupplementaryRound());
        logger.info("CCA took {}s.", (end - start) / 1000);

        BigDecimal qualityCP = allocCP.getTotalValue().divide(efficientAllocationWithTrueValues.getTotalValue(), RoundingMode.HALF_UP);
        logger.info("Quality after clock phase: {}", qualityCP.setScale(4, RoundingMode.HALF_UP));

        BigDecimal qualitySR = allocSR.getTotalValue().divide(efficientAllocationWithTrueValues.getTotalValue(), RoundingMode.HALF_UP);
        logger.info("Quality with supplementary round: {}", qualitySR.setScale(4, RoundingMode.HALF_UP));

        assertTrue(qualityCP.compareTo(qualitySR) < 1);
        assertTrue(qualitySR.compareTo(BigDecimal.ONE) < 1);
    }

    private NonGenericCCAMechanism<LSVMLicense> getMechanism(List<LSVMBidder> rawBidders) {
        List<Bidder<LSVMLicense>> bidders = rawBidders.stream()
                .map(b -> (Bidder<LSVMLicense>) b).collect(Collectors.toList());
        NonGenericCCAMechanism<LSVMLicense> cca = new NonGenericCCAMechanism<>(bidders, new LSVM_DemandQueryMIPBuilder());
        cca.calculateSampledStartingPrices(50, 100, 0.1);
        cca.setEpsilon(1e-5);
        cca.setTimeLimit(60);

        SimpleRelativeNonGenericPriceUpdate<LSVMLicense> priceUpdater = new SimpleRelativeNonGenericPriceUpdate<>();
        priceUpdater.setPriceUpdate(BigDecimal.valueOf(0.1));
        priceUpdater.setInitialUpdate(BigDecimal.valueOf(0.2));
        cca.setPriceUpdater(priceUpdater);

        ProfitMaximizingNonGenericSupplementaryRound<LSVMLicense> supplementaryRound = new ProfitMaximizingNonGenericSupplementaryRound<>();
        supplementaryRound.setNumberOfSupplementaryBids(500);
        cca.addSupplementaryRound(supplementaryRound);

        return cca;
    }

    @Test
    public void testNoDuplicatesInSupplementaryRound() {
        List<LSVMBidder> rawBidders = new LocalSynergyValueModel().createNewPopulation();
        NonGenericCCAMechanism<LSVMLicense> cca = getMechanism(rawBidders);
        cca.calculateSampledStartingPrices(50, 100 ,0.1);
        cca.setTimeLimit(30);

        for (LSVMBidder bidder : rawBidders) {
            List<XORValue<LSVMLicense>> valuesCP = cca.getBidsAfterClockPhase()
                    .stream()
                    .filter(bid -> bid.getBidder().equals(bidder))
                    .map(XORBid::getValues)
                    .findFirst().orElseThrow(NoSuchElementException::new);
            List<XORValue<LSVMLicense>> valuesSR = new ArrayList<>(
                    cca.getBidsAfterSupplementaryRound()
                            .stream()
                            .filter(bid -> bid.getBidder().equals(bidder))
                            .map(XORBid::getValues)
                            .findFirst().orElseThrow(NoSuchElementException::new)
            );
            valuesSR.removeAll(valuesCP);
            for (int i = 0; i < valuesSR.size(); i++) {
                for (int j = i + 1; j < valuesSR.size(); j++) {
                    assertNotEquals(valuesSR.get(i).getLicenses(), valuesSR.get(j).getLicenses());
                }
            }
        }
    }

    @Test
    public void testMultipleSupplementaryRounds() {
        List<LSVMBidder> rawBidders = new LocalSynergyValueModel().createNewPopulation();
        NonGenericCCAMechanism<LSVMLicense> cca = getMechanism(rawBidders);

        ProfitMaximizingNonGenericSupplementaryRound<LSVMLicense> supplementaryRoundLastPrices = new ProfitMaximizingNonGenericSupplementaryRound<>();
        supplementaryRoundLastPrices.setNumberOfSupplementaryBids(150);
        supplementaryRoundLastPrices.useLastDemandedPrices(true);
        cca.addSupplementaryRound(supplementaryRoundLastPrices);

        Allocation<LSVMLicense> allocationAfterSupplementaryRound = cca.calculateAllocationAfterSupplementaryRound();
        rawBidders.forEach(b -> assertEquals(cca.getBidCountAfterSupplementaryRound().get(b) - cca.getBidCountAfterClockPhase().get(b), 650));
    }

    @Test
    public void testSampledStartingPrices() {
        List<LSVMBidder> rawBidders = new LocalSynergyValueModel().createNewPopulation();
        NonGenericCCAMechanism<LSVMLicense> ccaZero = getMechanism(rawBidders);
        long startZero = System.currentTimeMillis();
        Allocation<LSVMLicense> allocZero = ccaZero.calculateClockPhaseAllocation();
        BigDecimal zeroTotalValue = allocZero.getAllocationWithTrueValues().getTotalValue();
        long durationZero = System.currentTimeMillis() - startZero;

        NonGenericCCAMechanism<LSVMLicense> ccaSampled = getMechanism(rawBidders);
        ccaSampled.calculateSampledStartingPrices(10, 100, 0.5);
        long startSampled = System.currentTimeMillis();
        Allocation<LSVMLicense> allocSampled = ccaSampled.calculateClockPhaseAllocation();
        BigDecimal sampledTotalValue = allocSampled.getAllocationWithTrueValues().getTotalValue();
        long durationSampled = System.currentTimeMillis() - startSampled;

        assertTrue(ccaZero.getTotalRounds() > ccaSampled.getTotalRounds());
        assertTrue(durationZero > durationSampled);
    }
    @Test
    public void testLastBidsSupplementaryRound() {
        List<LSVMBidder> rawBidders = new LocalSynergyValueModel().createNewPopulation();
        List<Bidder<LSVMLicense>> bidders = rawBidders.stream()
                .map(b -> (Bidder<LSVMLicense>) b).collect(Collectors.toList());
        NonGenericCCAMechanism<LSVMLicense> cca = new NonGenericCCAMechanism<>(bidders, new LSVM_DemandQueryMIPBuilder());
        cca.setFallbackStartingPrice(BigDecimal.ZERO);
        cca.setEpsilon(1e-5);

        SimpleRelativeNonGenericPriceUpdate<LSVMLicense> priceUpdater = new SimpleRelativeNonGenericPriceUpdate<>();
        priceUpdater.setPriceUpdate(BigDecimal.valueOf(0.1));
        priceUpdater.setInitialUpdate(BigDecimal.valueOf(5));
        cca.setPriceUpdater(priceUpdater);

        LastBidsTrueValueNonGenericSupplementaryRound<LSVMLicense> lastBidsSupplementaryRound = new LastBidsTrueValueNonGenericSupplementaryRound<>();
        lastBidsSupplementaryRound.setNumberOfSupplementaryBids(10);
        cca.addSupplementaryRound(lastBidsSupplementaryRound);

        Allocation<LSVMLicense> allocationAfterSupplementaryRound = cca.calculateAllocationAfterSupplementaryRound();
        for (LSVMBidder bidder : rawBidders) {
            XORBid<LSVMLicense> bid = cca.getBidAfterSupplementaryRound(bidder);
            int maxBids = Math.min(10, bid.getValues().size() / 2);
            int count = 0;
            for (int i = bid.getValues().size() - 1; i > 0 && count++ < maxBids; i--) {
                XORValue<LSVMLicense> current = bid.getValues().get(i);
                int baseIndex = 2 * bid.getValues().size() - 2*maxBids - 1 - i;
                XORValue<LSVMLicense> base = bid.getValues().get(baseIndex);
                assertEquals(current.getLicenses(), base.getLicenses());
                assertTrue(current.value().compareTo(base.value()) > 0);
            }
        }
    }

    @Test
    public void testBidsAnomaly() {
        List<LSVMBidder> rawBidders = new LocalSynergyValueModel().createNewPopulation(123456);
        assertEquals(rawBidders, new LocalSynergyValueModel().createNewPopulation());
        List<Collection<XORBid<LSVMLicense>>> resultingBids = new ArrayList<>();
        List<Bidder<LSVMLicense>> bidders = rawBidders.stream()
                .map(b -> (Bidder<LSVMLicense>) b).collect(Collectors.toList());
        resultingBids.add(runStandardCCA(bidders));
        //for (int i = 0; i < 3; i++) {
        //    // Unrelated code
        //    new LocalSynergyValueModel().createNewPopulation();
        //    // Add another instance
        //    resultingBids.add(runStandardCCA(bidders));
        //}
        Collection<XORBid<LSVMLicense>> first = resultingBids.get(0);
        Set<XORBid<LSVMLicense>> firstBids = new HashSet<>(first);
        XORWinnerDetermination<LSVMLicense> firstWdp = new XORWinnerDetermination<>(firstBids);
        Allocation<LSVMLicense> firstAllocation = firstWdp.calculateAllocation();
        Allocation<LSVMLicense> firstAllocationTrueValues = firstAllocation.getAllocationWithTrueValues();

        for (Collection<XORBid<LSVMLicense>> set : resultingBids) {
            // Check for bids equality
            assertEquals(first, set);
            for (XORBid<LSVMLicense> bid : set) {
                XORBid<LSVMLicense> otherBid = firstBids.stream().filter(b -> b.getBidder().equals(bid.getBidder())).findFirst().orElseThrow(NoSuchElementException::new);
                for (XORValue<LSVMLicense> value : bid.getValues()) {
                    XORValue<LSVMLicense> otherValue = otherBid.getValues().stream().filter(v -> v.getLicenses().equals(value.getLicenses()) && v.value().equals(value.value())).findFirst().orElseThrow(NoSuchElementException::new);
                    assertEquals(value.getLicenses(), otherValue.getLicenses());
                    assertEquals(value.value(), otherValue.value());
                }
            }
            Set<XORBid<LSVMLicense>> bids = new HashSet<>(set);
            XORWinnerDetermination<LSVMLicense> wdp = new XORWinnerDetermination<>(bids);
            Allocation<LSVMLicense> allocation = wdp.calculateAllocation();
            logger.info("Allocation: {}", allocation);
            logger.info("Total Declared Value: {}", allocation.getTotalValue());
            Allocation<LSVMLicense> allocationTrueValues = allocation.getAllocationWithTrueValues();
            logger.info("Total True Value:     {}", allocationTrueValues.getTotalValue());
            // Check for allocation equality
            assertEquals(firstAllocationTrueValues, allocationTrueValues);
            for (Bidder<LSVMLicense> bidder : bidders) {
                assertEquals(firstAllocationTrueValues.getAllocation(bidder), allocationTrueValues.getAllocation(bidder));
                assertEquals(firstAllocationTrueValues.getTradeValue(bidder), allocationTrueValues.getTradeValue(bidder));
            }
            assertEquals(firstAllocationTrueValues.getTotalValue(), allocationTrueValues.getTotalValue());
            assertEquals(firstAllocationTrueValues.getWinners(), allocationTrueValues.getWinners());
        }
    }

    private Collection<XORBid<LSVMLicense>> runStandardCCA(List<Bidder<LSVMLicense>> bidders) {
        NonGenericCCAMechanism<LSVMLicense> cca = new NonGenericCCAMechanism<>(bidders, new LSVM_DemandQueryMIPBuilder());
        cca.setFallbackStartingPrice(BigDecimal.ZERO);
        cca.setEpsilon(1e-5);

        SimpleRelativeNonGenericPriceUpdate<LSVMLicense> priceUpdater = new SimpleRelativeNonGenericPriceUpdate<>();
        priceUpdater.setPriceUpdate(BigDecimal.valueOf(0.1));
        priceUpdater.setInitialUpdate(BigDecimal.valueOf(5));
        cca.setPriceUpdater(priceUpdater);

        ProfitMaximizingNonGenericSupplementaryRound<LSVMLicense> supplementaryRound = new ProfitMaximizingNonGenericSupplementaryRound<>();
        supplementaryRound.setNumberOfSupplementaryBids(10);
        cca.addSupplementaryRound(supplementaryRound);

        return cca.getBidsAfterSupplementaryRound();
    }
}

package org.spectrumauctions.sats.mechanism.cca;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLicense;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.DemandDependentPriceUpdate;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
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

public class CCATest {

    private static final Logger logger = LogManager.getLogger(CCATest.class);

    private static MRVMMipResult efficientAllocation;
    private static List<MRVMBidder> rawBidders;

    @BeforeClass
    public static void findEfficientSolution() {
        rawBidders = new MultiRegionModel().createNewPopulation(234456867);
        MRVM_MIP mip = new MRVM_MIP(Sets.newHashSet(rawBidders));
        efficientAllocation = mip.calculateAllocation();
    }

    @Test
    public void testCCAWithMRVMSimplePriceUpdate() {
        long start = System.currentTimeMillis();
        List<Bidder<MRVMLicense>> bidders = rawBidders.stream()
                .map(b -> (Bidder<MRVMLicense>) b).collect(Collectors.toList());
        CCAMechanism<MRVMLicense> cca = new CCAMechanism<>(bidders, new MRVM_DemandQueryMIPBuilder());
        cca.setVariant(CCAVariant.XORQ);
        cca.setStartingPrice(BigDecimal.ZERO);
        cca.setEpsilon(0.1);
        MechanismResult<MRVMLicense> result = cca.getMechanismResult();
        long end = System.currentTimeMillis();
        logger.warn("CCA took {}s.", (end - start) / 1000);
        Allocation<MRVMLicense> allocationFromMechanism = result.getAllocation();
        Allocation<MRVMLicense> allocationWithTrueValues = allocationFromMechanism.getAllocationWithTrueValues();
        assertTrue(result.getAllocation().getTotalValue().doubleValue() > 0);
        assertNotEquals(allocationFromMechanism, allocationWithTrueValues);

        // Compare allocations:
        BigDecimal quality = allocationWithTrueValues.getTotalValue().divide(efficientAllocation.getTotalValue(), RoundingMode.HALF_UP);
        logger.warn("Quality of this iteration: {}", quality);
    }

    @Test
    @Ignore // It doesn't terminate for now
    public void testCCAWithMRVMComplexPriceUpdate() {
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

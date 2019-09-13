package org.spectrumauctions.sats.mechanism.pvm;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.marketdesignresearch.mechlib.core.*;
import org.marketdesignresearch.mechlib.core.bidder.Bidder;
import org.marketdesignresearch.mechlib.instrumentation.MipLoggingInstrumentation;
import org.marketdesignresearch.mechlib.mechanism.auctions.pvm.PVMAuction;
import org.marketdesignresearch.mechlib.mechanism.auctions.pvm.ml.MLAlgorithm;
import org.marketdesignresearch.mechlib.outcomerules.OutcomeRuleGenerator;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.model.lsvm.LocalSynergyValueModel;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;
import org.spectrumauctions.sats.mechanism.domains.GSVMDomain;
import org.spectrumauctions.sats.mechanism.domains.LSVMDomain;
import org.spectrumauctions.sats.mechanism.domains.MRVMDomain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class PVMTest {

    @Test
    public void testGSVM() {
        Domain domain = new GSVMDomain(new GlobalSynergyValueModel().createNewPopulation());
        testPVM(domain);
    }

    @Test
    public void testLSVM() {
        Domain domain = new LSVMDomain(new LocalSynergyValueModel().createNewPopulation());
        testPVM(domain);
    }

    @Test
    public void testMRVM() {
        Domain domain = new MRVMDomain(new MultiRegionModel().createNewPopulation());
        testPVM(domain);
    }

    private void testPVM(Domain domain) {
        PVMAuction pvm = new PVMAuction(domain, MLAlgorithm.Type.LINEAR_REGRESSION, OutcomeRuleGenerator.CCG, domain.getGoods().size() * 2);
        pvm.setMipInstrumentation(new MipLoggingInstrumentation());
        pvm.advanceRound();
        while (!pvm.finished()) {
            for (Bidder bidder : domain.getBidders()) {
                log.info("----- Elicitation in round {} for bidder {}", pvm.getNumberOfRounds(), bidder.getName());
                for (Bundle bundle : Sets.powerSet(new HashSet<>(domain.getGoods())).stream().limit(10).map(Bundle::of).collect(Collectors.toSet())) {
                    Optional<BundleBid> reported = pvm.getLatestAggregatedBids(bidder).getBundleBids().stream().filter(bbid -> bbid.getBundle().equals(bundle)).findAny();
                    log.info("- Bundle {}", bundle);
                    log.info("\t*\tTrue Value: {}", bidder.getValue(bundle).setScale(2, RoundingMode.HALF_UP));
                    log.info("\t*\tReported Value: {}", reported.isPresent() ? reported.get().getAmount().setScale(2, RoundingMode.HALF_UP) : "-");
                    log.info("\t*\tInferred Value: {}", pvm.getInferredValue(bidder, bundle).setScale(2, RoundingMode.HALF_UP));
                }
            }
            pvm.advanceRound();
        }
        Outcome mechanismResult = pvm.getOutcome();
        log.info(mechanismResult.toString());
        Allocation mechanismAllocationWithTrueValues = mechanismResult.getAllocation().getAllocationWithTrueValues();
        Allocation efficientAllocation = domain.getEfficientAllocation();
        BigDecimal efficiency = mechanismAllocationWithTrueValues.getTotalAllocationValue().divide(efficientAllocation.getTotalAllocationValue(), RoundingMode.HALF_UP).setScale(4, RoundingMode.HALF_UP);
        log.info("Efficiency PVM: " + efficiency);
        Assert.assertTrue(efficiency.doubleValue() < 1);
    }
}

package org.spectrumauctions.sats.mechanism.cca;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.marketdesignresearch.mechlib.core.Allocation;
import org.marketdesignresearch.mechlib.core.Domain;
import org.marketdesignresearch.mechlib.core.Outcome;
import org.marketdesignresearch.mechlib.instrumentation.MipLoggingInstrumentation;
import org.marketdesignresearch.mechlib.mechanism.auctions.cca.CCAuction;
import org.marketdesignresearch.mechlib.mechanism.auctions.cca.supplementaryphase.ProfitMaximizingSupplementaryPhase;
import org.marketdesignresearch.mechlib.outcomerules.OutcomeRuleGenerator;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.model.lsvm.LocalSynergyValueModel;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;
import org.spectrumauctions.sats.mechanism.domains.GSVMDomain;
import org.spectrumauctions.sats.mechanism.domains.LSVMDomain;
import org.spectrumauctions.sats.mechanism.domains.MRVMDomain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CCATest {

    private static final Logger logger = LogManager.getLogger(CCATest.class);

    @Test
    @Ignore // price generation for GSVM with AllocationLimits takes a litte time
    public void testGSVM() {
        Domain domain = new GSVMDomain(new GlobalSynergyValueModel().createNewWorldAndPopulation());
        testCCA(domain);
    }

    @Test
    public void testLSVM() {
        Domain domain = new LSVMDomain(new LocalSynergyValueModel().createNewWorldAndPopulation());
        testCCA(domain);
    }

    @Test
    public void testMRVM() {
        Domain domain = new MRVMDomain(new MultiRegionModel().createNewWorldAndPopulation());
        testCCA(domain);
    }

    private void testCCA(Domain domain) {
        CCAuction cca = new CCAuction(domain, OutcomeRuleGenerator.CCG, true);
        cca.setMipInstrumentation(new MipLoggingInstrumentation());
        cca.addSupplementaryRound(new ProfitMaximizingSupplementaryPhase().withNumberOfSupplementaryBids(10));
        Outcome mechanismResult = cca.getOutcome();
        logger.info(mechanismResult);
        Allocation mechanismAllocationWithTrueValues = mechanismResult.getAllocation().getAllocationWithTrueValues();
        Allocation efficientAllocation = domain.getEfficientAllocation();
        BigDecimal efficiency = mechanismAllocationWithTrueValues.getTotalAllocationValue().divide(efficientAllocation.getTotalAllocationValue(), RoundingMode.HALF_UP).setScale(4, RoundingMode.HALF_UP);
        logger.info("Efficiency CCA: " + efficiency);
        Assert.assertTrue(efficiency.doubleValue() <= 1);
    }
}

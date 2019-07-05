package org.spectrumauctions.sats.mechanism.cca;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.marketdesignresearch.mechlib.auction.cca.CCAuction;
import org.marketdesignresearch.mechlib.auction.cca.bidcollection.supplementaryround.ProfitMaximizingSupplementaryRound;
import org.marketdesignresearch.mechlib.domain.Allocation;
import org.marketdesignresearch.mechlib.domain.Domain;
import org.marketdesignresearch.mechlib.mechanisms.MechanismResult;
import org.marketdesignresearch.mechlib.mechanisms.MechanismType;
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
    public void testGSVM() {
        Domain domain = new GSVMDomain(new GlobalSynergyValueModel().createNewPopulation());
        testCCA(domain);
    }

    @Test
    public void testLSVM() {
        Domain domain = new LSVMDomain(new LocalSynergyValueModel().createNewPopulation());
        testCCA(domain);
    }

    @Test
    public void testMRVM() {
        Domain domain = new MRVMDomain(new MultiRegionModel().createNewPopulation());
        testCCA(domain);
    }

    private void testCCA(Domain domain) {
        CCAuction cca = new CCAuction(domain, MechanismType.CCG, true);
        cca.addSupplementaryRound(new ProfitMaximizingSupplementaryRound(cca).withNumberOfSupplementaryBids(10));
        MechanismResult mechanismResult = cca.getMechanismResult();
        logger.info(mechanismResult);
        Allocation mechanismAllocationWithTrueValues = mechanismResult.getAllocation().getAllocationWithTrueValues();
        Allocation efficientAllocation = domain.getEfficientAllocation();
        BigDecimal efficiency = mechanismAllocationWithTrueValues.getTotalAllocationValue().divide(efficientAllocation.getTotalAllocationValue(), RoundingMode.HALF_UP).setScale(4, RoundingMode.HALF_UP);
        logger.info("Efficiency CCA: " + efficiency);
        Assert.assertTrue(efficiency.doubleValue() < 1);
    }
}

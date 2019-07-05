package org.spectrumauctions.sats.mechanism.pvm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.marketdesignresearch.mechlib.auction.cca.CCAuction;
import org.marketdesignresearch.mechlib.auction.cca.bidcollection.supplementaryround.ProfitMaximizingSupplementaryRound;
import org.marketdesignresearch.mechlib.auction.pvm.PVMAuction;
import org.marketdesignresearch.mechlib.domain.Allocation;
import org.marketdesignresearch.mechlib.domain.Domain;
import org.marketdesignresearch.mechlib.mechanisms.MechanismResult;
import org.marketdesignresearch.mechlib.mechanisms.MechanismType;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.model.lsvm.LocalSynergyValueModel;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;
import org.spectrumauctions.sats.mechanism.domains.GSVMDomain;
import org.spectrumauctions.sats.mechanism.domains.LSVMDomain;
import org.spectrumauctions.sats.mechanism.domains.MRVMGenericDomain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PVMTest {

    private static final Logger logger = LogManager.getLogger(PVMTest.class);

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
        Domain domain = new MRVMGenericDomain(new MultiRegionModel().createNewPopulation());
        testPVM(domain);
    }

    private void testPVM(Domain domain) {
        PVMAuction pvm = new PVMAuction(domain, MechanismType.CCG);
        MechanismResult mechanismResult = pvm.getMechanismResult();
        logger.info(mechanismResult);
        Allocation mechanismAllocationWithTrueValues = mechanismResult.getAllocation().getAllocationWithTrueValues();
        Allocation efficientAllocation = domain.getEfficientAllocation();
        BigDecimal efficiency = mechanismAllocationWithTrueValues.getTotalAllocationValue().divide(efficientAllocation.getTotalAllocationValue(), RoundingMode.HALF_UP).setScale(4, RoundingMode.HALF_UP);
        logger.info("Efficiency PVM: " + efficiency);
        Assert.assertTrue(efficiency.doubleValue() < 1);
    }
}

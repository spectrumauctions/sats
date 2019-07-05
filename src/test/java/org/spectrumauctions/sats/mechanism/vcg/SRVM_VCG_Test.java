package org.spectrumauctions.sats.mechanism.vcg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.marketdesignresearch.mechlib.mechanisms.MechanismResult;
import org.marketdesignresearch.mechlib.mechanisms.vcg.VCGMechanism;
import org.spectrumauctions.sats.core.model.srvm.SingleRegionModel;
import org.spectrumauctions.sats.opt.model.srvm.SRVM_MIP;

public class SRVM_VCG_Test {

    private static final Logger logger = LogManager.getLogger(SRVM_VCG_Test.class);

    @Test
    public void testModelVCG() {
        VCGMechanism vcg = new ModelVCGMechanism(new SRVM_MIP(new SingleRegionModel().createNewPopulation()));
        MechanismResult mechanismResult = vcg.getMechanismResult();
        logger.info(mechanismResult);
    }

}

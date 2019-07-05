package org.spectrumauctions.sats.mechanism.vcg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.marketdesignresearch.mechlib.mechanisms.MechanismResult;
import org.marketdesignresearch.mechlib.mechanisms.vcg.VCGMechanism;
import org.spectrumauctions.sats.core.model.lsvm.LocalSynergyValueModel;
import org.spectrumauctions.sats.opt.model.lsvm.LSVMStandardMIP;

public class LSVM_VCG_Test {

    private static final Logger logger = LogManager.getLogger(LSVM_VCG_Test.class);

    @Test
    public void testModelVCG() {
        VCGMechanism vcg = new ModelVCGMechanism(new LSVMStandardMIP(new LocalSynergyValueModel().createNewPopulation()));
        MechanismResult mechanismResult = vcg.getMechanismResult();
        logger.info(mechanismResult);
    }

}

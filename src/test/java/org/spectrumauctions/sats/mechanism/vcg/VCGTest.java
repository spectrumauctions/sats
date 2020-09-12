package org.spectrumauctions.sats.mechanism.vcg;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.marketdesignresearch.mechlib.core.Outcome;
import org.marketdesignresearch.mechlib.instrumentation.MipInstrumentation;
import org.marketdesignresearch.mechlib.instrumentation.MipLoggingInstrumentation;
import org.marketdesignresearch.mechlib.outcomerules.OutcomeRule;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.model.lsvm.LocalSynergyValueModel;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;
import org.spectrumauctions.sats.core.model.srvm.SingleRegionModel;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;
import org.spectrumauctions.sats.opt.model.lsvm.LSVMStandardMIP;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;
import org.spectrumauctions.sats.opt.model.srvm.SRVM_MIP;

@Slf4j
public class VCGTest {

    @Test
    public void testGSVMVCG() {
        ModelMIP mip = new GSVMStandardMIP(new GlobalSynergyValueModel().createNewWorldAndPopulation());
        testModelVCG(mip);
    }

    @Test
    public void testLSVMVCG() {
        ModelMIP mip = new LSVMStandardMIP(new LocalSynergyValueModel().createNewWorldAndPopulation());
        testModelVCG(mip);
    }

    @Test
    @Ignore
    public void testMRVMVCG() {
        ModelMIP mip = new MRVM_MIP(new MultiRegionModel().createNewWorldAndPopulation());
        testModelVCG(mip);
    }

    @Test
    public void testSRVMVCG() {
        ModelMIP mip = new SRVM_MIP(new SingleRegionModel().createNewWorldAndPopulation());
        testModelVCG(mip);
    }

    private void testModelVCG(ModelMIP mip) {
        OutcomeRule vcg = new ModelVCGMechanism(mip);
        vcg.setMipInstrumentation(new MipLoggingInstrumentation());
        Outcome outcome = vcg.getOutcome();
        log.info(outcome.toString());
    }

}

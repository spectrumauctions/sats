package org.spectrumauctions.sats.mechanism.domains;

import org.marketdesignresearch.mechlib.instrumentation.MipInstrumentation;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.lsvm.LSVMStandardMIP;

import java.util.List;
import java.util.stream.Collectors;

public class LSVMDomain extends ModelDomain {

    public LSVMDomain(List<LSVMBidder> bidders) {
        super(bidders);
    }

    @Override
    protected ModelMIP getMIP() {
        List<LSVMBidder> bidders = getBidders().stream().map(b -> (LSVMBidder) b).collect(Collectors.toList());
        return new LSVMStandardMIP(bidders, MipInstrumentation.MipPurpose.ALLOCATION, getMipInstrumentation());
    }

}

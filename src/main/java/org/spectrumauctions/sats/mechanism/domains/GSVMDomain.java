package org.spectrumauctions.sats.mechanism.domains;

import lombok.Getter;
import org.marketdesignresearch.mechlib.instrumentation.MipInstrumentation;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;

import java.util.List;
import java.util.stream.Collectors;

public class GSVMDomain extends ModelDomain {

    public GSVMDomain(List<GSVMBidder> bidders) {
        super(bidders);
    }

    @Override
    protected ModelMIP getMIP() {
        List<GSVMBidder> bidders = getBidders().stream().map(b -> (GSVMBidder) b).collect(Collectors.toList());
        return new GSVMStandardMIP(bidders, MipInstrumentation.MipPurpose.ALLOCATION, getMipInstrumentation());
    }

}

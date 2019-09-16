package org.spectrumauctions.sats.mechanism.domains;

import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;

import java.util.List;
import java.util.stream.Collectors;

public class MRVMDomain extends ModelDomain {

    public MRVMDomain(List<MRVMBidder> bidders) {
        super(bidders, true);
    }
    public MRVMDomain(List<MRVMBidder> bidders, boolean generic) {
        super(bidders, generic);
    }

    @Override
    protected ModelMIP getMIP() {
        List<MRVMBidder> bidders = getBidders().stream().map(b -> (MRVMBidder) b).collect(Collectors.toList());
        return new MRVM_MIP(bidders);
    }

    @Override
    public String getName() {
        return super.getName() + " (MRVM)";
    }

}

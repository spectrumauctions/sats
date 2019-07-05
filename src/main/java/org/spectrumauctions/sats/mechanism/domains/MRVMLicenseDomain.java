package org.spectrumauctions.sats.mechanism.domains;

import lombok.Getter;
import org.marketdesignresearch.mechlib.domain.Allocation;
import org.marketdesignresearch.mechlib.domain.Domain;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLicense;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;

import java.util.List;

public class MRVMLicenseDomain implements Domain {

    @Getter
    private List<MRVMBidder> bidders;

    @Getter
    private List<MRVMLicense> goods;

    private transient Allocation efficientAllocation;


    public MRVMLicenseDomain(List<MRVMBidder> bidders) {
        this.bidders = bidders;
        this.goods = bidders.iterator().next().getWorld().getLicenses();
    }

    @Override
    public Allocation getEfficientAllocation() {
        if (efficientAllocation == null) {
            efficientAllocation = new MRVM_MIP(bidders).getAllocation();
        }
        return efficientAllocation;
    }

}

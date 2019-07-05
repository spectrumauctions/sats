package org.spectrumauctions.sats.mechanism.domains;

import lombok.Getter;
import org.marketdesignresearch.mechlib.domain.Allocation;
import org.marketdesignresearch.mechlib.domain.Domain;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;
import org.spectrumauctions.sats.opt.model.lsvm.LSVMStandardMIP;

import java.util.List;

public class LSVMDomain implements Domain {

    @Getter
    private List<LSVMBidder> bidders;

    @Getter
    private List<LSVMLicense> goods;

    private transient Allocation efficientAllocation;


    public LSVMDomain(List<LSVMBidder> bidders) {
        this.bidders = bidders;
        this.goods = bidders.iterator().next().getWorld().getLicenses();
    }

    @Override
    public Allocation getEfficientAllocation() {
        if (efficientAllocation == null) {
            efficientAllocation = new LSVMStandardMIP(bidders).getAllocation();
        }
        return efficientAllocation;
    }

}

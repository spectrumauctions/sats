package org.spectrumauctions.sats.mechanism.domains;

import lombok.Getter;
import org.marketdesignresearch.mechlib.domain.Allocation;
import org.marketdesignresearch.mechlib.domain.Domain;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;

import java.util.List;

public class GSVMDomain implements Domain {

    @Getter
    private List<GSVMBidder> bidders;

    @Getter
    private List<GSVMLicense> goods;

    private transient Allocation efficientAllocation;


    public GSVMDomain(List<GSVMBidder> bidders) {
        this.bidders = bidders;
        this.goods = bidders.iterator().next().getWorld().getLicenses();
    }

    @Override
    public Allocation getEfficientAllocation() {
        if (efficientAllocation == null) {
            efficientAllocation = new GSVMStandardMIP(bidders).getAllocation();
        }
        return efficientAllocation;
    }

}

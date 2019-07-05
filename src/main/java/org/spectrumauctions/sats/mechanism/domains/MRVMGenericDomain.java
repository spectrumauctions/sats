package org.spectrumauctions.sats.mechanism.domains;

import lombok.Getter;
import org.marketdesignresearch.mechlib.domain.Allocation;
import org.marketdesignresearch.mechlib.domain.Domain;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMGenericDefinition;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLicense;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;

import java.util.List;

public class MRVMGenericDomain implements Domain {

    @Getter
    private List<MRVMBidder> bidders;

    @Getter
    private List<MRVMGenericDefinition> goods;

    private transient Allocation efficientAllocation;


    public MRVMGenericDomain(List<MRVMBidder> bidders) {
        this.bidders = bidders;
        this.goods = bidders.iterator().next().getWorld().getAllGenericDefinitions();
    }

    @Override
    public Allocation getEfficientAllocation() {
        if (efficientAllocation == null) {
            efficientAllocation = new MRVM_MIP(bidders).getAllocation();
        }
        return efficientAllocation;
    }

}

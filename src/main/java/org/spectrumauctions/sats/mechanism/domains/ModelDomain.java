package org.spectrumauctions.sats.mechanism.domains;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.marketdesignresearch.mechlib.domain.Allocation;
import org.marketdesignresearch.mechlib.domain.Domain;
import org.spectrumauctions.sats.core.model.GenericWorld;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.SATSGood;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;

import java.util.List;

@EqualsAndHashCode
public abstract class ModelDomain implements Domain {

    @Getter
    private List<? extends SATSBidder> bidders;

    @Getter
    private List<? extends SATSGood> goods;

    private transient Allocation efficientAllocation;

    protected abstract ModelMIP getMIP();

    public ModelDomain(List<? extends SATSBidder> bidders) {
        this(bidders, false);
    }

    public ModelDomain(List<? extends SATSBidder> bidders, boolean generic) {
        this.bidders = bidders;
        if (generic) {
            GenericWorld genericWorld = (GenericWorld) bidders.iterator().next().getWorld();
            this.goods = genericWorld.getAllGenericDefinitions();
        } else {
            this.goods = bidders.iterator().next().getWorld().getLicenses();
        }
    }

    @Override
    public Allocation getEfficientAllocation() {
        if (!hasEfficientAllocationCalculated()) {
            efficientAllocation = getMIP().getAllocation();
        }
        return efficientAllocation;
    }

    @Override
    public boolean hasEfficientAllocationCalculated() {
        return efficientAllocation != null;
    }

}

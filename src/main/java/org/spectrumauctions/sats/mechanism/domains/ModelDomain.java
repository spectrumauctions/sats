package org.spectrumauctions.sats.mechanism.domains;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.marketdesignresearch.mechlib.core.Allocation;
import org.marketdesignresearch.mechlib.core.Domain;
import org.marketdesignresearch.mechlib.instrumentation.MipInstrumentation;
import org.spectrumauctions.sats.core.model.GenericWorld;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.SATSGood;
import org.spectrumauctions.sats.opt.model.ModelMIP;

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

    // region instrumentation
    @Getter @Setter(AccessLevel.PROTECTED)
    private MipInstrumentation mipInstrumentation = new MipInstrumentation();

    @Override
    public void attachMipInstrumentation(MipInstrumentation mipInstrumentation) {
        this.mipInstrumentation = mipInstrumentation;
        getBidders().forEach(bidder -> bidder.attachMipInstrumentation(mipInstrumentation));
    }

}

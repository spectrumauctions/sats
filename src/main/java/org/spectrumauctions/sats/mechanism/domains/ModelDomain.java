package org.spectrumauctions.sats.mechanism.domains;

import lombok.EqualsAndHashCode;
import lombok.Getter;
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
            getMIP().setMipInstrumentation(getMipInstrumentation());
            getMIP().setPurpose(MipInstrumentation.MipPurpose.ALLOCATION);
            efficientAllocation = getMIP().getAllocation();
        }
        return efficientAllocation;
    }

    @Override
    public boolean hasEfficientAllocationCalculated() {
        return efficientAllocation != null;
    }

    @Override
    public String getName() {
        return "SATS Domain";
    }

    // region instrumentation
    @Getter
    private MipInstrumentation mipInstrumentation = MipInstrumentation.NO_OP;

    @Override
    public void setMipInstrumentation(MipInstrumentation mipInstrumentation) {
        this.mipInstrumentation = mipInstrumentation;
        getBidders().forEach(bidder -> bidder.setMipInstrumentation(mipInstrumentation));
    }
    // endregion

}

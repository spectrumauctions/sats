package org.spectrumauctions.sats.opt.model.mrvm.demandquery;

import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.opt.domain.DemandQueryResult;
import org.spectrumauctions.sats.opt.domain.GenericAllocation;

import java.math.BigDecimal;

/**
 * @author Fabio Isler
 *
 */
public final class MRVMDemandQueryMipResult implements DemandQueryResult<MRVMLicense> {

    private final MRVMWorld world;
    private final BigDecimal totalUtility;
    private final GenericValue<MRVMGenericDefinition, MRVMLicense> resultingBundle;

    private MRVMDemandQueryMipResult(Builder builder) {
        this.world = builder.world;
        this.totalUtility = BigDecimal.valueOf(builder.totalUtility);
        this.resultingBundle = builder.result;
    }

    @Override
    public GenericValue<MRVMGenericDefinition, MRVMLicense> getResultingBundle() {
        return resultingBundle;
    }


    public MRVMWorld getWorld() {
        return world;
    }


    public static final class Builder extends GenericAllocation.Builder<MRVMGenericDefinition, MRVMLicense> {

        private MRVMWorld world;
        private double totalUtility;
        private GenericValue<MRVMGenericDefinition, MRVMLicense> result;

        public Builder(MRVMWorld world, double totalUtility, GenericValue<MRVMGenericDefinition, MRVMLicense> result) {
            super();
            this.world = world;
            this.totalUtility = totalUtility;
            this.result = result;
        }

        public MRVMDemandQueryMipResult build() {
            return new MRVMDemandQueryMipResult(this);
        }
    }

    @Override
    public String toString() {
        return "MRVMDemandQueryMipResult{" +
                "world=" + world +
                ", totalUtility=" + totalUtility +
                ", resultingBundle=" + resultingBundle +
                '}';
    }
}

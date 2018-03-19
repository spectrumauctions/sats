package org.spectrumauctions.sats.opt.model.mrvm.demandquery;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.mrvm.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Fabio Isler
 *
 */
public class MRVM_DemandQueryMIP extends ModelMIP {

    private static final Logger logger = LogManager.getLogger(MRVM_DemandQueryMIP.class);

    private static SolverClient solver = new SolverClient();

    private MRVMBidder bidder;
    private MRVMWorld world;
    private MRVM_MIP mrvmMip;

    private Variable priceVar;

    public MRVM_DemandQueryMIP(MRVMBidder bidder, Map<MRVMLicense, BigDecimal> prices) {
        Preconditions.checkNotNull(bidder);
        this.bidder = bidder;
        Preconditions.checkNotNull(prices);
        this.world = bidder.getWorld();
        Preconditions.checkArgument(prices.size() == world.getNumberOfGoods());
        mrvmMip = new MRVM_MIP(Sets.newHashSet(bidder));

        mrvmMip.getMip().setSolveParam(SolveParam.RELATIVE_OBJ_GAP, 0.001);
        double scalingFactor = mrvmMip.getBidderPartialMips().get(bidder).getScalingFactor();
        priceVar = new Variable("p", VarType.DOUBLE, 0, MIP.MAX_VALUE);
        mrvmMip.addVariable(priceVar);
        mrvmMip.addObjectiveTerm(-1, priceVar);
        Constraint price = new Constraint(CompareType.EQ, 0);
        price.addTerm(-1, priceVar);
        for (Map.Entry<MRVMLicense, BigDecimal> entry : prices.entrySet()) {
            MRVMLicense license = entry.getKey();
            Variable xVariable = mrvmMip.getWorldPartialMip().getXVariable(bidder, license.getRegion(), license.getBand());
            price.addTerm(entry.getValue().doubleValue() / scalingFactor, xVariable);
        }
        mrvmMip.addConstraint(price);
    }

    public MRVMDemandQueryMipResult calculateAllocation() {
        logger.debug(mrvmMip.getMip());
        IMIPResult mipResult = solver.solve(mrvmMip.getMip());
        logger.debug("Result:\n{}", mipResult);

        double scalingFactor = mrvmMip.getBidderPartialMips().get(bidder).getScalingFactor();

        Variable bidderValueVar = mrvmMip.getWorldPartialMip().getValueVariable(bidder);
        double resultingValue = mipResult.getValue(bidderValueVar);
        double resultingPrice = mipResult.getValue(priceVar);
        double unscaledValue = resultingValue * scalingFactor;
        double unscaledPrice = resultingPrice * scalingFactor;
        double unscaledObjVal = mipResult.getObjectiveValue() * scalingFactor;
        if (Math.abs(unscaledValue - unscaledPrice - unscaledObjVal) >= 1e-7) {
            logger.warn("Values don't match. Delta of {}. Unscaled value = {}, Unscaled price = {}, Unscaled objective value = {}",
                    unscaledValue - unscaledPrice - unscaledObjVal, unscaledValue, unscaledPrice, unscaledObjVal);
        }
        GenericValue.Builder<MRVMGenericDefinition> valueBuilder = new GenericValue.Builder<>(BigDecimal.valueOf(unscaledValue));
        for (Region region : world.getRegionsMap().getRegions()) {
            for (MRVMBand band : world.getBands()) {
                Variable xVar = mrvmMip.getWorldPartialMip().getXVariable(bidder, region, band);
                double doubleQuantity = mipResult.getValue(xVar);
                int quantity = (int) Math.round(doubleQuantity);
                MRVMGenericDefinition def = new MRVMGenericDefinition(band, region);
                valueBuilder.putQuantity(def, quantity);
            }
        }

        MRVMDemandQueryMipResult.Builder resultBuilder = new MRVMDemandQueryMipResult.Builder(world, unscaledValue - unscaledPrice, valueBuilder.build());
        return resultBuilder.build();
    }
}

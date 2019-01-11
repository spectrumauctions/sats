package org.spectrumauctions.sats.opt.model.mrvm.demandquery;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.ISolution;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Fabio Isler
 */
public class MRVM_DemandQueryMIP extends ModelMIP implements GenericDemandQueryMIP<MRVMGenericDefinition, MRVMLicense> {

    private static final Logger logger = LogManager.getLogger(MRVM_DemandQueryMIP.class);

    private static SolverClient solver = new SolverClient();

    private MRVMBidder bidder;
    private MRVMWorld world;
    private MRVM_MIP mrvmMip;

    private Variable priceVar;

    public MRVM_DemandQueryMIP(MRVMBidder bidder, Map<MRVMGenericDefinition, BigDecimal> prices) {
        this(bidder, prices, 0.001);
    }

    public MRVM_DemandQueryMIP(MRVMBidder bidder, Map<MRVMGenericDefinition, BigDecimal> prices, double epsilon) {
        Preconditions.checkNotNull(bidder);
        this.bidder = bidder;
        Preconditions.checkNotNull(prices);
        this.world = bidder.getWorld();
        Preconditions.checkArgument(prices.size() == world.getAllGenericDefinitions().size());
        mrvmMip = new MRVM_MIP(Sets.newHashSet(bidder));

        mrvmMip.getMip().setSolveParam(SolveParam.RELATIVE_OBJ_GAP, epsilon);
        double scalingFactor = mrvmMip.getBidderPartialMips().get(bidder).getScalingFactor();
        priceVar = new Variable("p", VarType.DOUBLE, 0, MIP.MAX_VALUE);
        mrvmMip.addVariable(priceVar);
        mrvmMip.addObjectiveTerm(-1, priceVar);
        Constraint price = new Constraint(CompareType.EQ, 0);
        price.addTerm(-1, priceVar);
        for (Map.Entry<MRVMGenericDefinition, BigDecimal> entry : prices.entrySet()) {
            MRVMGenericDefinition bandInRegion = entry.getKey();
            Variable xVariable = mrvmMip.getWorldPartialMip().getXVariable(bidder, bandInRegion.getRegion(), bandInRegion.getBand());
            price.addTerm(entry.getValue().doubleValue() / scalingFactor, xVariable);
        }
        mrvmMip.addConstraint(price);
    }

    @Override
    public MRVMDemandQueryMipResult getResult() {
        List<MRVMDemandQueryMipResult> results = getResultPool(1);
        if (results.size() > 1) logger.warn("Requested one solution, got {}.", results.size());
        return results.get(0);
    }

    @Override
    public List<MRVMDemandQueryMipResult> getResultPool(int numberOfResults) {
        if (numberOfResults < 1) {
            return Lists.newArrayList();
        }

        mrvmMip.getMip().setSolveParam(SolveParam.SOLUTION_POOL_CAPACITY, numberOfResults);
        mrvmMip.getMip().setSolveParam(SolveParam.SOLUTION_POOL_MODE, 4);
        // TODO: Mode 3 doesn't work because so far because the X variables are actually integers...
        // mrvmMip.getMip().setSolveParam(SolveParam.SOLUTION_POOL_MODE, 3);
        mrvmMip.getMip().setVariablesOfInterest(mrvmMip.getXVariables());
        IMIPResult mipResult = solver.solve(mrvmMip.getMip());
        logger.debug("Result:\n{}", mipResult);

        List<MRVMDemandQueryMipResult> results = new ArrayList<>();
        for (ISolution sol : mipResult.getPoolSolutions()) {
            double scalingFactor = mrvmMip.getBidderPartialMips().get(bidder).getScalingFactor();

            Variable bidderValueVar = mrvmMip.getWorldPartialMip().getValueVariable(bidder);
            double resultingValue = sol.getValue(bidderValueVar);
            double resultingPrice = sol.getValue(priceVar);
            double unscaledValue = resultingValue * scalingFactor;
            double unscaledPrice = resultingPrice * scalingFactor;
            double unscaledObjVal = sol.getObjectiveValue() * scalingFactor;

            if (Math.abs(unscaledValue - unscaledPrice - unscaledObjVal) >= 1e-5) {
                logger.warn("Values don't match. Delta of {}. Unscaled value = {}, Unscaled price = {}, Unscaled objective value = {}",
                        unscaledValue - unscaledPrice - unscaledObjVal, unscaledValue, unscaledPrice, unscaledObjVal);
            }

            GenericValue.Builder<MRVMGenericDefinition, MRVMLicense> valueBuilder = new GenericValue.Builder<>(BigDecimal.valueOf(unscaledValue));
            for (Region region : world.getRegionsMap().getRegions()) {
                for (MRVMBand band : world.getBands()) {
                    Variable xVar = mrvmMip.getWorldPartialMip().getXVariable(bidder, region, band);
                    double doubleQuantity = sol.getValue(xVar);
                    int quantity = (int) Math.round(doubleQuantity);
                    if (quantity > 0) {
                        MRVMGenericDefinition def = new MRVMGenericDefinition(band, region);
                        valueBuilder.putQuantity(def, quantity);
                    }
                }
            }

            MRVMDemandQueryMipResult.Builder resultBuilder = new MRVMDemandQueryMipResult.Builder(world, unscaledValue - unscaledPrice, valueBuilder.build());

            results.add(resultBuilder.build());
        }

        return results;
    }

}

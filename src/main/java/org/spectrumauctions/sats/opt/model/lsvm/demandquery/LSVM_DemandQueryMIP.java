package org.spectrumauctions.sats.opt.model.lsvm.demandquery;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;
import org.spectrumauctions.sats.core.model.lsvm.LSVMWorld;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.lsvm.LSVMStandardMIP;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabio Isler
 */
public class LSVM_DemandQueryMIP extends ModelMIP implements NonGenericDemandQueryMIP<LSVMLicense> {

    private static final Logger logger = LogManager.getLogger(LSVM_DemandQueryMIP.class);

    private static SolverClient solver = new SolverClient();

    private LSVMBidder bidder;
    private LSVMWorld world;
    private LSVMStandardMIP lsvmMip;

    private Variable priceVar;

    public LSVM_DemandQueryMIP(LSVMBidder bidder, Map<LSVMLicense, BigDecimal> prices) {
        this(bidder, prices, 0.001);
    }

    public LSVM_DemandQueryMIP(LSVMBidder bidder, Map<LSVMLicense, BigDecimal> prices, double epsilon) {
        Preconditions.checkNotNull(bidder);
        this.bidder = bidder;
        Preconditions.checkNotNull(prices);
        this.world = bidder.getWorld();
        Preconditions.checkArgument(prices.size() == world.getLicenses().size());
        lsvmMip = new LSVMStandardMIP(Lists.newArrayList(bidder));

        lsvmMip.getMip().setSolveParam(SolveParam.RELATIVE_OBJ_GAP, epsilon);
        priceVar = new Variable("p", VarType.DOUBLE, 0, MIP.MAX_VALUE);
        lsvmMip.getMip().add(priceVar);
        lsvmMip.getMip().addObjectiveTerm(-1, priceVar);
        Constraint price = new Constraint(CompareType.EQ, 0);
        price.addTerm(-1, priceVar);
        for (Map.Entry<LSVMLicense, BigDecimal> entry : prices.entrySet()) {
            LSVMLicense license = entry.getKey();
            Map<Integer, Variable> xVariables = lsvmMip.getXVariables(bidder, license);
            for (Variable xVariable : xVariables.values()) {
                price.addTerm(entry.getValue().doubleValue(), xVariable);
            }
        }
        lsvmMip.getMip().add(price);
    }

    public LSVM_DemandQueryMipResult getResult() {
        logger.debug(lsvmMip.getMip());
        IMIPResult mipResult = solver.solve(lsvmMip.getMip());
        logger.debug("Result:\n{}", mipResult);


        Set<LSVMLicense> licenses = new HashSet<>();
        for (LSVMLicense license : world.getLicenses()) {
            Map<Integer, Variable> xVars = lsvmMip.getXVariables(bidder, license);
            for (Variable var : xVars.values()) {
                double value = mipResult.getValue(var);
                if (value >= 1 - 1e-6 && value <= 1 + 1e-6) {
                    licenses.add(license);
                }
            }
        }

        Bundle<LSVMLicense> bundle = new Bundle<>(licenses);

        XORValue<LSVMLicense> xorValue = new XORValue<>(bundle, bidder.calculateValue(bundle));

        return new LSVM_DemandQueryMipResult(BigDecimal.valueOf(mipResult.getObjectiveValue()), xorValue);
    }

    @Override
    public Set<LSVM_DemandQueryMipResult> getResultPool(int numberOfResults) {
        if (numberOfResults < 1) {
            return Sets.newHashSet();
        }

        lsvmMip.getMip().setSolveParam(SolveParam.SOLUTION_POOL_CAPACITY, numberOfResults);
        lsvmMip.getMip().setSolveParam(SolveParam.SOLUTION_POOL_REPLACEMENT, 1);
        lsvmMip.getMip().setSolveParam(SolveParam.SOLUTION_POOL_MODE, 2);
        lsvmMip.getMip().setSolveParam(SolveParam.SOLUTION_POOL_INTENSITY, 4);
        lsvmMip.getMip().setSolveParam(SolveParam.POPULATE_LIMIT, numberOfResults * 3);
        IMIPResult mipResult = solver.solve(lsvmMip.getMip());
        logger.debug("Result:\n{}", mipResult);

        Set<LSVM_DemandQueryMipResult> results = new HashSet<>();
        for (Solution sol : mipResult.getIntermediateSolutions()) {
            Set<LSVMLicense> licenses = new HashSet<>();
            for (LSVMLicense license : world.getLicenses()) {
                Map<Integer, Variable> xVars = lsvmMip.getXVariables(bidder, license);
                for (Variable var : xVars.values()) {
                    double value = sol.getValue(var);
                    if (value >= 1 - 1e-6 && value <= 1 + 1e-6) {
                        licenses.add(license);
                    }
                }
            }

            Bundle<LSVMLicense> bundle = new Bundle<>(licenses);

            XORValue<LSVMLicense> xorValue = new XORValue<>(bundle, bidder.calculateValue(bundle));

            results.add(new LSVM_DemandQueryMipResult(BigDecimal.valueOf(sol.getObjectiveValue()), xorValue));
        }

        return results;
    }

}

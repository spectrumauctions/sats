package org.spectrumauctions.sats.opt.model.gsvm.demandquery;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.ISolution;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.core.model.gsvm.GSVMWorld;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Fabio Isler
 */
public class GSVM_DemandQueryMIP extends ModelMIP implements NonGenericDemandQueryMIP<GSVMLicense> {

    private static final Logger logger = LogManager.getLogger(GSVM_DemandQueryMIP.class);

    private static SolverClient solver = new SolverClient();

    private GSVMBidder bidder;
    private GSVMWorld world;
    private GSVMStandardMIP gsvmMip;
    private Set<Variable> variablesOfInterest;
    private Variable priceVar;

    public GSVM_DemandQueryMIP(GSVMBidder bidder, Map<GSVMLicense, BigDecimal> prices) {
        this(bidder, prices, 0.001);
    }

    public GSVM_DemandQueryMIP(GSVMBidder bidder, Map<GSVMLicense, BigDecimal> prices, double epsilon) {
        Preconditions.checkNotNull(bidder);
        this.bidder = bidder;
        Preconditions.checkNotNull(prices);
        this.world = bidder.getWorld();
        Preconditions.checkArgument(prices.size() == world.getLicenses().size());
        gsvmMip = new GSVMStandardMIP(Lists.newArrayList(bidder));

        gsvmMip.getMip().setSolveParam(SolveParam.RELATIVE_OBJ_GAP, epsilon);
        priceVar = new Variable("p", VarType.DOUBLE, 0, MIP.MAX_VALUE);
        gsvmMip.getMip().add(priceVar);
        gsvmMip.getMip().addObjectiveTerm(-1, priceVar);
        Constraint price = new Constraint(CompareType.EQ, 0);
        price.addTerm(-1, priceVar);
        variablesOfInterest = new HashSet<>();
        for (Map.Entry<GSVMLicense, BigDecimal> entry : prices.entrySet()) {
            GSVMLicense license = entry.getKey();
            Map<Integer, Variable> xVariables = gsvmMip.getXVariables(bidder, license);
            for (Variable xVariable : xVariables.values()) {
                variablesOfInterest.add(xVariable);
                price.addTerm(entry.getValue().doubleValue(), xVariable);
            }
        }
        gsvmMip.getMip().add(price);
    }

    @Override
    public GSVM_DemandQueryMipResult getResult() {
        List<GSVM_DemandQueryMipResult> results = getResultPool(1);
        if (results.size() > 1) logger.warn("Requested one solution, got {}.", results.size());
        return results.get(0);
    }

    @Override
    public List<GSVM_DemandQueryMipResult> getResultPool(int numberOfResults) {
        if (numberOfResults < 1) {
            return Lists.newArrayList();
        }

        gsvmMip.getMip().setSolveParam(SolveParam.SOLUTION_POOL_CAPACITY, numberOfResults);
        gsvmMip.getMip().setSolveParam(SolveParam.SOLUTION_POOL_MODE, 3);
        gsvmMip.getMip().setVariablesOfInterest(variablesOfInterest);

        IMIPResult mipResult = solver.solve(gsvmMip.getMip());
        logger.debug("Result:\n{}", mipResult);

        List<GSVM_DemandQueryMipResult> results = new ArrayList<>();
        for (ISolution sol : mipResult.getPoolSolutions()) {
            Set<GSVMLicense> licenses = new HashSet<>();
            for (GSVMLicense license : world.getLicenses()) {
                Map<Integer, Variable> xVars = gsvmMip.getXVariables(bidder, license);
                for (Variable var : xVars.values()) {
                    double value = sol.getValue(var);
                    if (value >= 1 - 1e-6 && value <= 1 + 1e-6) {
                        licenses.add(license);
                    }
                }
            }

            Bundle<GSVMLicense> bundle = new Bundle<>(licenses);

            XORValue<GSVMLicense> xorValue = new XORValue<>(bundle, bidder.calculateValue(bundle));

            results.add(new GSVM_DemandQueryMipResult(BigDecimal.valueOf(sol.getObjectiveValue()), xorValue));
        }

        return results;
    }

}

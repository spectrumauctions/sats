package org.spectrumauctions.sats.opt.model.lsvm.demandquery;

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
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;
import org.spectrumauctions.sats.core.model.lsvm.LSVMWorld;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.lsvm.LSVMStandardMIP;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Fabio Isler
 */
public class LSVM_DemandQueryMIP implements NonGenericDemandQueryMIP<LSVMLicense> {

    private static final Logger logger = LogManager.getLogger(LSVM_DemandQueryMIP.class);

    private static SolverClient solver = new SolverClient();

    private LSVMBidder bidder;
    private LSVMWorld world;
    private LSVMStandardMIP lsvmMip;
    private Collection<Collection<Variable>> variableSetsOfInterest;
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
        variableSetsOfInterest = new HashSet<>();
        for (Map.Entry<LSVMLicense, BigDecimal> entry : prices.entrySet()) {
            Set<Variable> variablesOfInterest = new HashSet<>();
            LSVMLicense license = entry.getKey();
            Map<Integer, Variable> xVariables = lsvmMip.getXVariables(bidder, license);
            for (Variable xVariable : xVariables.values()) {
                variablesOfInterest.add(xVariable);
                price.addTerm(entry.getValue().doubleValue(), xVariable);
            }
            variableSetsOfInterest.add(variablesOfInterest);
        }
        lsvmMip.getMip().add(price);
    }

    @Override
    public LSVM_DemandQueryMipResult getResult() {
        List<LSVM_DemandQueryMipResult> results = getResultPool(1);
        if (results.size() > 1) logger.warn("Requested one solution, got {}.", results.size());
        return results.get(0);
    }

    @Override
    public List<LSVM_DemandQueryMipResult> getResultPool(int numberOfResults) {
        if (numberOfResults < 1) {
            return Lists.newArrayList();
        }

        lsvmMip.getMip().setSolveParam(SolveParam.SOLUTION_POOL_CAPACITY, numberOfResults);
        lsvmMip.getMip().setSolveParam(SolveParam.SOLUTION_POOL_MODE, 4);
        lsvmMip.getMip().setAdvancedVariablesOfInterest(variableSetsOfInterest);
        IMIPResult mipResult = solver.solve(lsvmMip.getMip());
        logger.debug("Result:\n{}", mipResult);

        List<LSVM_DemandQueryMipResult> results = new ArrayList<>();
        for (ISolution sol : mipResult.getPoolSolutions()) {
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

    @Override
    public ModelMIP getMip() {
        return lsvmMip;
    }
}

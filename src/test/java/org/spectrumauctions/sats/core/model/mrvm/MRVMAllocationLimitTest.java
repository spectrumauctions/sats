package org.spectrumauctions.sats.core.model.mrvm;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Random;

import org.junit.Test;
import org.marketdesignresearch.mechlib.core.Allocation;
import org.spectrumauctions.sats.mechanism.domains.MRVMDomain;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;

import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.VarType;
import edu.harvard.econcs.jopt.solver.mip.Variable;

public class MRVMAllocationLimitTest {

	@Test
	public void testDifferentFormulations() {
		int setting = 23410702;
		
		MRVMDomain domain1 = new MRVMDomain(
				new MultiRegionModel().createNewWorldAndPopulation(setting),
				true, true);
		MRVMDomain domain2 = new MRVMDomain(
				new MultiRegionModel().createNewWorldAndPopulation(setting),
				false, true);
		MRVMDomain domain3 = new MRVMDomain(new MultiRegionModel().createNewWorldAndPopulation(setting),
				false, false);
		
		MRVM_MIP mip = new MRVM_MIP(domain3.getBidders());
		appendAggregationLimits(domain3.getBidders(), mip);
		
		Allocation allocation1 = domain1.getEfficientAllocation();
		Allocation allocation2 = domain2.getEfficientAllocation();
		Allocation allocation3 = mip.getAllocation();
		
		assertEquals(allocation1.getTrueSocialWelfare().doubleValue(), allocation2.getTrueSocialWelfare().doubleValue(), 1e-6);
		assertEquals(allocation1.getTrueSocialWelfare().doubleValue(), allocation3.getTrueSocialWelfare().doubleValue(), 1e-6);
		assertEquals(allocation2.getTrueSocialWelfare().doubleValue(), allocation3.getTrueSocialWelfare().doubleValue(), 1e-6);
	}
	
	public static final  String LOW_PAIRED_NAME = "LOW_PAIRED";
    public static final String HIGH_PAIRED_NAME = "HIGH_PAIRED";
    public static final String UNPAIRED_NAME = "UNPAIRED";
	
	// original formulation of the allocation constraints
	public static void appendAggregationLimits(Collection<MRVMBidder> bidders, MRVM_MIP mip) {
        MRVMWorld world = bidders.iterator().next().getWorld();
        MRVMBand lp = null;
        MRVMBand hp = null;
        for (MRVMBand band : world.getBands()) {
            if (band.getName().equals(LOW_PAIRED_NAME)) {
                lp = band;
            } else if (band.getName().equals(HIGH_PAIRED_NAME)) {
                hp = band;
            }
        }
        if (lp == null || hp == null) {
            throw new RuntimeException("Band not found");
        }
        for (MRVMBidder bidder : bidders) {
            for (MRVMRegionsMap.Region region : world.getRegionsMap().getRegions()) {
                Variable varLp = mip.getWorldPartialMip().getXVariable(bidder, region, lp);
                Variable varHp = mip.getWorldPartialMip().getXVariable(bidder, region, hp);
                Constraint constraint = new Constraint(CompareType.LEQ, 2);
                constraint.addTerm(1, varLp);
                constraint.addTerm(1, varHp);
                mip.addConstraint(constraint);

                if (bidder instanceof MRVMNationalBidder) {
                    String varName = "AgggregationLimitXhat_i=" + bidder.getId() +
                            "_r=" + region.getId() + "b=" + "LP";

                    // Initiate and Constrain variable indication if at least one licences in band lp
                    Variable hatVarLP = new Variable(varName, VarType.BOOLEAN, 0, 1);
                    Constraint upperLimitConstraint = new Constraint(CompareType.LEQ, 0);
                    upperLimitConstraint.addTerm(1, hatVarLP);
                    upperLimitConstraint.addTerm(-1, varLp);
                    mip.addConstraint(upperLimitConstraint);
                    mip.addVariable(hatVarLP);
                    Constraint lowerLimitConstraint = new Constraint(CompareType.GEQ, 0);
                    lowerLimitConstraint.addTerm(1, hatVarLP);
                    lowerLimitConstraint.addTerm((-1d) / (double) lp.getNumberOfLots(), varLp);
                    mip.addConstraint(lowerLimitConstraint);
                    // Initiate and Constrain variable indication if at least one licences in band hp
                    varName = "AgggregationLimitXhat_i=" + bidder.getId() +
                            "_r=" + region.getId() + "b=" + "HP";
                    Variable hatVarHP = new Variable(varName, VarType.BOOLEAN, 0, 1);
                    upperLimitConstraint = new Constraint(CompareType.LEQ, 0);
                    upperLimitConstraint.addTerm(1, hatVarHP);
                    upperLimitConstraint.addTerm(-1, varHp);
                    mip.addVariable(hatVarHP);
                    mip.addConstraint(upperLimitConstraint);
                    lowerLimitConstraint = new Constraint(CompareType.GEQ, 0);
                    lowerLimitConstraint.addTerm(1, hatVarHP);
                    lowerLimitConstraint.addTerm((-1.) / (double) hp.getNumberOfLots(), varHp);
                    mip.addConstraint(lowerLimitConstraint);
                    // Must only have license in one of the two paired bands
                    Constraint onlyOne = new Constraint(CompareType.LEQ, 1);
                    onlyOne.addTerm(1, hatVarHP);
                    onlyOne.addTerm(1, hatVarLP);
                    mip.addConstraint(onlyOne);
                    // Not more than one in hp
                    Constraint maxOneInHP = new Constraint(CompareType.LEQ, 1);
                    maxOneInHP.addTerm(1, varHp);
                    mip.addConstraint(maxOneInHP);
                }
            }
        }

    }
}

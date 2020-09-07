package org.spectrumauctions.sats.core.model.mrvm;

import java.util.List;

import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.allocationlimits.AllocationLimitConstraint;

import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.VarType;
import edu.harvard.econcs.jopt.solver.mip.Variable;

public class MRVMNationalBidderGenericAllocationLimit extends MRVMGenericAllocationLimit {

	public MRVMNationalBidderGenericAllocationLimit(MRVMNationalBidder bidder) {
		super(bidder);
		
		for (MRVMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()) {
			MRVMGenericDefinition varLp = bidder.getWorld().getAllGenericDefinitions().stream()
					.filter(g -> g.getBand().getName().equals(LOW_PAIRED_NAME) && g.getRegion().equals(region))
					.findFirst().get();
			MRVMGenericDefinition varHp = bidder.getWorld().getAllGenericDefinitions().stream()
					.filter(g -> g.getBand().getName().equals(HIGH_PAIRED_NAME) && g.getRegion().equals(region))
					.findFirst().get();

			String varName = "AgggregationLimitXhat_i=" + bidder.getId() + "_r=" + region.getId() + "b=" + "LP";

			// Initiate and Constrain variable indication if at least one licences in band
			// lp
			Variable hatVarLP = new Variable(varName, VarType.BOOLEAN, 0, 1);
			AllocationLimitConstraint upperLimitConstraint = new AllocationLimitConstraint(CompareType.LEQ, 0);
			upperLimitConstraint.addTerm(1, hatVarLP);
			upperLimitConstraint.addTerm(-1, varLp);
			this.addAllocationLimitConstraint(upperLimitConstraint);
			
			AllocationLimitConstraint lowerLimitConstraint = new AllocationLimitConstraint(CompareType.GEQ, 0);
			lowerLimitConstraint.addTerm(1, hatVarLP);
			lowerLimitConstraint.addTerm((-1d) / (double) varLp.getQuantity(), varLp);
			this.addAllocationLimitConstraint(lowerLimitConstraint);
			
			// Initiate and Constrain variable indication if at least one licences in band
			// hp
			varName = "AgggregationLimitXhat_i=" + bidder.getId() + "_r=" + region.getId() + "b=" + "HP";
			Variable hatVarHP = new Variable(varName, VarType.BOOLEAN, 0, 1);
			upperLimitConstraint = new AllocationLimitConstraint(CompareType.LEQ, 0);
			upperLimitConstraint.addTerm(1, hatVarHP);
			upperLimitConstraint.addTerm(-1, varHp);
			this.addAllocationLimitConstraint(upperLimitConstraint);
			
			lowerLimitConstraint = new AllocationLimitConstraint(CompareType.GEQ, 0);
			lowerLimitConstraint.addTerm(1, hatVarHP);
			lowerLimitConstraint.addTerm((-1.) / (double) varHp.getQuantity(), varHp);
			this.addAllocationLimitConstraint(lowerLimitConstraint);
			
			// Must only have license in one of the two paired bands
			AllocationLimitConstraint onlyOne = new AllocationLimitConstraint(CompareType.LEQ, 1);
			onlyOne.addTerm(1, hatVarHP);
			onlyOne.addTerm(1, hatVarLP);
			this.addAllocationLimitConstraint(onlyOne);
			
			// Not more than one in hp
			AllocationLimitConstraint maxOneInHP = new AllocationLimitConstraint(CompareType.LEQ, 1);
			maxOneInHP.addTerm(1, varHp);
			this.addAllocationLimitConstraint(maxOneInHP);
		}

	}

	@Override
	public int calculateAllocationBundleSpace(List<? extends Good> startingSpace) {
		// TODO
		// calculate correct Bundle space
		// Bundle space is very large therefore it does not matter for now as the limit
		// will probably never be reached
		return Integer.MAX_VALUE;
	}

}

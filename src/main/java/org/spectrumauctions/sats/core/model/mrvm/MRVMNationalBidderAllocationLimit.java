package org.spectrumauctions.sats.core.model.mrvm;

import java.util.List;
import java.util.stream.Collectors;

import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.allocationlimits.AllocationLimitConstraint;
import org.spectrumauctions.sats.core.model.License;

import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.VarType;
import edu.harvard.econcs.jopt.solver.mip.Variable;

public class MRVMNationalBidderAllocationLimit extends MRVMAllocationLimit {

	public MRVMNationalBidderAllocationLimit(MRVMNationalBidder bidder) {
		super(bidder);
		for (MRVMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()) {
			
				List<License> varLP = bidder.getWorld().getLicenses().stream().filter(l -> l.getRegion().equals(region)).filter(l -> l.getBandName().equals(LOW_PAIRED_NAME)).collect(Collectors.toList());
				List<License> varHP = bidder.getWorld().getLicenses().stream().filter(l -> l.getRegion().equals(region)).filter(l -> l.getBandName().equals(HIGH_PAIRED_NAME)).collect(Collectors.toList());
			
                String varName = "AgggregationLimitXhat_i=" + bidder.getId() +
                        "_r=" + region.getId() + "b=" + "LP";

                // Initiate and Constrain variable indication if at least one licences in band lp
                Variable hatVarLP = new Variable(varName, VarType.BOOLEAN, 0, 1);
                AllocationLimitConstraint upperLimitConstraint = new AllocationLimitConstraint(CompareType.LEQ, 0);
                upperLimitConstraint.addTerm(1, hatVarLP);
                this.addAll(upperLimitConstraint, -1, varLP);           
                this.addAllocationLimitConstraint(upperLimitConstraint);
                
                AllocationLimitConstraint lowerLimitConstraint = new AllocationLimitConstraint(CompareType.GEQ, 0);
                lowerLimitConstraint.addTerm(1, hatVarLP);
                double coefficient = (-1d) / (double) varLP.size();
                this.addAll(lowerLimitConstraint, coefficient, varLP);
                this.addAllocationLimitConstraint(lowerLimitConstraint);
                
                // Initiate and Constrain variable indication if at least one licences in band hp
                varName = "AgggregationLimitXhat_i=" + bidder.getId() +
                        "_r=" + region.getId() + "b=" + "HP";
                Variable hatVarHP = new Variable(varName, VarType.BOOLEAN, 0, 1);
                
                upperLimitConstraint = new AllocationLimitConstraint(CompareType.LEQ, 0);
                upperLimitConstraint.addTerm(1, hatVarHP);
                this.addAll(upperLimitConstraint, -1, varHP);
                this.addAllocationLimitConstraint(upperLimitConstraint);
                
                lowerLimitConstraint = new AllocationLimitConstraint(CompareType.GEQ, 0);
                lowerLimitConstraint.addTerm(1, hatVarHP);
                coefficient = (-1.) / varHP.size();
                this.addAll(lowerLimitConstraint, coefficient, varHP);
                this.addAllocationLimitConstraint(lowerLimitConstraint);
                
                // Must only have license in one of the two paired bands
                AllocationLimitConstraint onlyOne = new AllocationLimitConstraint(CompareType.LEQ, 1);
                onlyOne.addTerm(1, hatVarHP);
                onlyOne.addTerm(1, hatVarLP);
                this.addAllocationLimitConstraint(onlyOne);
          
                // Not more than one in hp
                AllocationLimitConstraint maxOneInHP = new AllocationLimitConstraint(CompareType.LEQ, 1);
                this.addAll(maxOneInHP, 1, varHP);
                this.addAllocationLimitConstraint(maxOneInHP);
            }
	}
	
	private void addAll(AllocationLimitConstraint constraint, double coefficient, List<License> licences) {
		licences.forEach(l -> constraint.addTerm(coefficient, l));
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

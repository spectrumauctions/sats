package org.spectrumauctions.sats.core.model.mrvm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.marketdesignresearch.mechlib.core.Bundle;
import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.allocationlimits.AllocationLimit;
import org.marketdesignresearch.mechlib.core.allocationlimits.AllocationLimitConstraint;
import org.marketdesignresearch.mechlib.core.bundlesampling.UniformRandomBundleSampling;
import org.spectrumauctions.sats.core.model.License;

import com.google.common.base.Preconditions;

import edu.harvard.econcs.jopt.solver.mip.CompareType;

public class MRVMAllocationLimit extends AllocationLimit {
	
	protected static final  String LOW_PAIRED_NAME = "LOW_PAIRED";
    protected static final String HIGH_PAIRED_NAME = "HIGH_PAIRED";
    protected static final String UNPAIRED_NAME = "UNPAIRED";
    
    private Map<MRVMRegionsMap.Region, List<License>> goodsPerRegion = new LinkedHashMap<>();

	public MRVMAllocationLimit(MRVMBidder bidder) {
		super(bidder.getWorld().getLicenses());
		
		List<MRVMBand> pairedBands = new ArrayList<>();
		pairedBands.add(bidder.getWorld().getBands().stream().filter(b -> b.getName().equals(LOW_PAIRED_NAME)).findAny().orElseThrow());
		pairedBands.add(bidder.getWorld().getBands().stream().filter(b -> b.getName().equals(HIGH_PAIRED_NAME)).findAny().orElseThrow());
		
		for (MRVMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()) {
			AllocationLimitConstraint constraint = new AllocationLimitConstraint(CompareType.LEQ, 2);
			bidder.getWorld().getLicenses().stream().filter(l -> l.getRegion().equals(region)).filter(l -> pairedBands.contains(l.getBand())).forEach(l -> constraint.addTerm(1, l));
			this.addAllocationLimitConstraint(constraint);
			
			goodsPerRegion.put(region, bidder.getWorld().getLicenses().stream().filter(l -> l.getRegion().equals(region)).collect(Collectors.toList()));
		}
	}
	
	@Override
	public int calculateAllocationBundleSpace(List<? extends Good> startingSpace) {
		// TODO
		// calculate correct Bundle space
		// Bundle space is very large therefore it does not matter for now as the limit will probably never be reached
		return Integer.MAX_VALUE;
	}

	@Override
	public Bundle getUniformRandomBundle(Random random, List<? extends Good> goods) {
		Preconditions.checkArgument(this.validateDomainCompatiblity(goods));
		UniformRandomBundleSampling randomSampling = new UniformRandomBundleSampling(random);
		Bundle bundle = Bundle.EMPTY;
		for(Map.Entry<MRVMRegionsMap.Region, List<License>> entry : this.goodsPerRegion.entrySet()) {
			Bundle regionBundle;
			List<Good> regionalGoods = new ArrayList<>(goods);
			regionalGoods.retainAll(entry.getValue());
			do {
				regionBundle = randomSampling.getSingleBundle(regionalGoods);
			} while(!this.validate(regionBundle));
			bundle = bundle.merge(regionBundle);
		}
		return bundle;
	}
}

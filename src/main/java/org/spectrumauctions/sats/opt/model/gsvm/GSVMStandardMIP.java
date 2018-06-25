package org.spectrumauctions.sats.opt.model.gsvm;

import com.google.common.base.Preconditions;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.VarType;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.core.model.gsvm.GSVMWorld;
import org.spectrumauctions.sats.opt.domain.ItemAllocation;
import org.spectrumauctions.sats.opt.domain.ItemAllocation.ItemAllocationBuilder;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.model.ModelMIP;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class GSVMStandardMIP extends ModelMIP implements WinnerDeterminator<GSVMLicense> {

    private Map<GSVMBidder, Map<GSVMLicense, Map<Integer, Variable>>> gMap;
	private Map<GSVMBidder, Map<GSVMLicense, Double>> valueMap;
	private Map<GSVMBidder, Integer> tauHatMap;

	private List<GSVMBidder> population;
	private GSVMWorld world;

	private boolean allowAssigningLicensesWithZeroBasevalue;

	public GSVMStandardMIP(List<GSVMBidder> population) {
		this(population.iterator().next().getWorld(), population, true);
	}

	public GSVMStandardMIP(GSVMWorld world, List<GSVMBidder> population) {
		this(world, population, true);
	}

	public GSVMStandardMIP(GSVMWorld world, List<GSVMBidder> population,
			boolean allowAssigningLicensesWithZeroBasevalue) {

		this.allowAssigningLicensesWithZeroBasevalue = allowAssigningLicensesWithZeroBasevalue;

		this.population = population;
		this.world = world;
		tauHatMap = new HashMap<>();
		valueMap = new HashMap<>();
		getMip().setObjectiveMax(true);
		initValues();
		initVariables();
		build();
	}

	@Override
	public WinnerDeterminator<GSVMLicense> getWdWithoutBidder(Bidder<GSVMLicense> bidder) {
        Preconditions.checkArgument(population.contains(bidder));
        return new GSVMStandardMIP(population.stream().filter(b -> !b.equals(bidder)).collect(Collectors.toList()));
	}

	@Override
	public ItemAllocation<GSVMLicense> calculateAllocation() {
		SolverClient solver = new SolverClient();
		IMIPResult result = solver.solve(getMip());

		Map<Bidder<GSVMLicense>, Bundle<GSVMLicense>> allocation = new HashMap<>();

		for (GSVMBidder bidder : population) {
            Bundle<GSVMLicense> bundle = new Bundle<>();
            for (GSVMLicense license : world.getLicenses()) {
                if (allowAssigningLicensesWithZeroBasevalue || valueMap.get(bidder).get(license) > 0) {
                    for (int tau = 0; tau < tauHatMap.get(bidder); tau++) {
                        if (result.getValue(gMap.get(bidder).get(license).get(tau)) == 1) {
                            bundle.add(license);
                        }
                    }
                }
            }
            allocation.put(bidder, bundle);
        }

		ItemAllocationBuilder<GSVMLicense> builder = new ItemAllocationBuilder<GSVMLicense>().withWorld(world)
				.withTotalValue(BigDecimal.valueOf(result.getObjectiveValue())).withAllocation(allocation);

		return builder.build();
	}

	public Map<Integer, Variable> getXVariables(GSVMBidder bidder, GSVMLicense license) {
		for (GSVMBidder b : population) {
			if (b.equals(bidder)) {
				for (GSVMLicense l : world.getLicenses()) {
					if (l.equals(license)) {
						return gMap.get(b).get(l);
					}
				}
			}
		}
		return new HashMap<>();
	}

	@Override
	public WinnerDeterminator<GSVMLicense> copyOf() {
		return new GSVMStandardMIP(population);
	}

	@Override
	public void adjustPayoffs(Map<Bidder<GSVMLicense>, Double> payoffs) {
		throw new UnsupportedOperationException("The GSVM MIP does not support CCG yet.");
	}

	private void build() {

        // build objective term
        for (GSVMBidder bidder : population) {
	        for (GSVMLicense license : world.getLicenses()) {
                if (allowAssigningLicensesWithZeroBasevalue || valueMap.get(bidder).get(license) > 0) {
                    for (int tau = 0; tau < tauHatMap.get(bidder); tau++) {
                        getMip().addObjectiveTerm(calculateComplementarityMarkup(tau + 1) * valueMap.get(bidder).get(license), gMap.get(bidder).get(license).get(tau));
                    }
                }
            }
        }


		// build Supply/Eval Constraint (1)
		for (GSVMLicense license : world.getLicenses()) {
			Constraint constraint = new Constraint(CompareType.LEQ, 1, "SupplyConstraint license=" + license.getId());
			for (GSVMBidder bidder : population) {
				if (allowAssigningLicensesWithZeroBasevalue || valueMap.get(bidder).get(license) > 0) {
					for (int tau = 0; tau < tauHatMap.get(bidder); tau++) {
						constraint.addTerm(1, gMap.get(bidder).get(license).get(tau));
					}
				}
			}
			getMip().add(constraint);
		}

		// build Tau Constraint (2)
		for (GSVMLicense j : world.getLicenses()) {
			for (GSVMBidder bidder : population) {
				Constraint constraint = new Constraint(CompareType.GEQ, 0);
				// build left part: Number of items agent i is allocated
				for (GSVMLicense k : world.getLicenses()) {
					if (allowAssigningLicensesWithZeroBasevalue || valueMap.get(bidder).get(k) > 0) {
						for (int tau = 0; tau < tauHatMap.get(bidder); tau++) {
							constraint.addTerm(1, gMap.get(bidder).get(k).get(tau));
						}
					}
				}
				// build right part: Activate tau that matches the number of
				// allocated items
				for (int tau = 0; tau < tauHatMap.get(bidder); tau++) {
					if (allowAssigningLicensesWithZeroBasevalue || valueMap.get(bidder).get(j) > 0) {
						constraint.addTerm(-(tau + 1), gMap.get(bidder).get(j).get(tau));
					}
				}
				getMip().add(constraint);
			}
		}
	}

	private void initValues() {

	    for (GSVMBidder bidder : population) {
	        valueMap.put(bidder, new HashMap<>());
            int tauCounter = 0;
            for (GSVMLicense license : world.getLicenses()) {
                BigDecimal val = bidder.getBaseValues().getOrDefault(license.getId(), BigDecimal.ZERO);
                if (allowAssigningLicensesWithZeroBasevalue || val.doubleValue() > 0) {
                    tauCounter++;
                }
                valueMap.get(bidder).put(license, val.doubleValue());
            }
            tauHatMap.put(bidder, tauCounter);
        }
	}

	private OptionalDouble getValue(int i, int j) {
		return population.stream().filter(bidder -> bidder.getId() == i).mapToDouble(bidder -> {
			BigDecimal val = bidder.getBaseValues().get((long) j);
			return val == null ? 0 : val.doubleValue();
		}).reduce((element, otherElement) -> {
			throw new IllegalStateException(
					"Error: Multiple values for agent: " + i + " and license: " + j + " in population");
		});
	}

	private void initVariables() {
		gMap = new HashMap<>();
		for (GSVMBidder bidder : population) {
		    gMap.put(bidder, new HashMap<>());
			for (GSVMLicense license : world.getLicenses()) {
				if (allowAssigningLicensesWithZeroBasevalue || valueMap.get(bidder).get(license) > 0) {
				    gMap.get(bidder).put(license, new HashMap<>());
					for (int tau = 0; tau < tauHatMap.get(bidder); tau++) {
					    Variable var = new Variable("g_i[" + (int) bidder.getId() + "]j[" + (int) license.getId() + "]t[" + tau + "]", VarType.BOOLEAN, 0, 1);
						getMip().add(var);
						gMap.get(bidder).get(license).put(tau, var);
					}
				}
			}
		}
	}

	private double calculateComplementarityMarkup(int tau) {
		if (tau < 1) {
			throw new IllegalArgumentException("Error: tau has to be >=1");
		}
		return 1 + (tau - 1) * 0.2;
	}

}

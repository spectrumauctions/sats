package org.spectrumauctions.sats.opt.model.lsvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.VarType;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.cats.graphalgorithms.Vertex;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMGrid;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;
import org.spectrumauctions.sats.core.model.lsvm.LSVMWorld;
import org.spectrumauctions.sats.opt.domain.ItemAllocation;
import org.spectrumauctions.sats.opt.domain.ItemAllocation.ItemAllocationBuilder;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.model.ModelMIP;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class implements a mip finding the efficient allocation in an instance
 * of the Local-Synergy Value Model
 *
 * @author Nicolas Küchler
 */
public class LSVMStandardMIP extends ModelMIP implements WinnerDeterminator<LSVMLicense> {

	private Map<LSVMBidder, Map<LSVMLicense, Double>> valueMap;

	private List<LSVMBidder> population;

	private LSVMWorld world;

	private Map<LSVMBidder, Map<LSVMLicense, Map<Integer, Variable>>> aVariables;
	private Map<LSVMBidder, Map<Edge, Map<Integer, Variable>>> eVariables;

	private Map<Edge, Set<Integer>> validPathLengths = new HashMap<>();

	public LSVMStandardMIP(List<LSVMBidder> population) {
		this(population.iterator().next().getWorld(), population);
	}

	public LSVMStandardMIP(LSVMWorld world, List<LSVMBidder> population) {
		this.world = world;
		this.population = population;

		// init MIP
		getMip().setObjectiveMax(true);
		getMip().setSolveParam(SolveParam.TIME_LIMIT, 3600.0);

		initBaseValues();
		initA();
		initEdge();
		initE();

		buildObjectiveTerm();
		buildSupplyEvalConstraints();
		buildEdgeSupplyConstraints();
		buildNeighbourConstraints();
		buildEdgeConstraints();
		buildTauConstraints();
	}

	@Override
	public WinnerDeterminator<LSVMLicense> getWdWithoutBidder(Bidder bidder) {
        Preconditions.checkArgument(population.contains(bidder));
		return new LSVMStandardMIP(population.stream().filter(b -> !b.equals(bidder)).collect(Collectors.toList()));
	}

	@Override
	public ItemAllocation<LSVMLicense> calculateAllocation() {
		SolverClient solver = new SolverClient();
		IMIPResult result = solver.solve(getMip());

		Map<Bidder<LSVMLicense>, Bundle<LSVMLicense>> allocation = new HashMap<>();
		for (LSVMBidder bidder : population) {
			Bundle<LSVMLicense> bundle = new Bundle<>();
            for (LSVMLicense license : world.getLicenses()) {
                for (int tau = 0; tau < world.getLicenses().size(); tau++) {
                    if (result.getValue(aVariables.get(bidder).get(license).get(tau)) > 0) {
                        bundle.add(license);
                    }
                }
            }
			allocation.put(bidder, bundle);
		}

		ItemAllocationBuilder<LSVMLicense> builder = new ItemAllocationBuilder<LSVMLicense>().withWorld(world)
				.withTotalValue(BigDecimal.valueOf(result.getObjectiveValue())).withAllocation(allocation);

		return builder.build();
	}

    public Map<Integer, Variable> getXVariables(LSVMBidder bidder, LSVMLicense license) {
        for (LSVMBidder b : population) {
            if (b.equals(bidder)) {
                for (LSVMLicense l : world.getLicenses()) {
                    if (l.equals(license)) {
                        return aVariables.get(b).get(l);
                    }
                }
            }
        }
        return new HashMap<>();
    }

	@Override
	public WinnerDeterminator<LSVMLicense> copyOf() {
		return new LSVMStandardMIP(population);
	}

	@Override
	public void adjustPayoffs(Map<Bidder<LSVMLicense>, Double> payoffs) {
        throw new UnsupportedOperationException("The LSVM MIP does not support CCG yet.");
	}

	private void buildObjectiveTerm() {
		for (LSVMBidder bidder : population) {
            for (LSVMLicense license : world.getLicenses()) {
                for (int tau = 0; tau < world.getLicenses().size(); tau++) {
                    double value = calculateComplementarityMarkup(tau + 1, bidder) * valueMap.get(bidder).get(license);
                    getMip().addObjectiveTerm(value, aVariables.get(bidder).get(license).get(tau));
                }
            }
		}
	}

	private void buildSupplyEvalConstraints() {
		for (LSVMLicense license : world.getLicenses()) {
			Constraint constraint = new Constraint(CompareType.LEQ, 1);
			for (LSVMBidder bidder : population) {
                for (int tau = 0; tau < world.getLicenses().size(); tau++) {
                    constraint.addTerm(1, aVariables.get(bidder).get(license).get(tau));
                }
			}
			getMip().add(constraint);
		}
	}

	private void buildEdgeSupplyConstraints() {
		for (Map.Entry<Edge, Set<Integer>> entry : validPathLengths.entrySet()) {
			Constraint constraint = new Constraint(CompareType.LEQ, 1);
			for (LSVMBidder bidder : population) {
                for (int c = 0; c < world.getLicenses().size(); c++) {
                    if (entry.getValue().contains(c + 1)) {
                        constraint.addTerm(1, eVariables.get(bidder).get(entry.getKey()).get(c));
                    }
                }
			}
			getMip().add(constraint);
		}
	}

	private void buildNeighbourConstraints() {
		for (LSVMBidder bidder : population) {
            for (Map.Entry<Edge, Set<Integer>> entry : validPathLengths.entrySet()) {
                Edge edge = entry.getKey();
                for (int c = 1; c < world.getLicenses().size(); c++) { // only for c > 1
                    if (entry.getValue().contains(c + 1)) {
                        Constraint constraint = new Constraint(CompareType.GEQ, 0);
                        constraint.addTerm(-1, eVariables.get(bidder).get(edge).get(c));
                        for (LSVMLicense x : n(gMin(edge))) {
                            LSVMLicense y = gMax(edge);
                            if (!x.equals(y)) {
                                Edge e = fInv(x, y);
                                if (validPathLengths.get(e).contains(c)) {
                                    constraint.addTerm(1, eVariables.get(bidder).get(e).get(c - 1));
                                }
                            }
                        }
                        getMip().add(constraint);
                    }
                }
            }
		}
	}

	private void buildEdgeConstraints() {
		for (LSVMBidder bidder : population) {
            for (Map.Entry<Edge, Set<Integer>> entry : validPathLengths.entrySet()) {
                Edge edge = entry.getKey();
                Constraint constraint = new Constraint(CompareType.GEQ, 0);
                for (int c = 0; c < world.getLicenses().size(); c++) {
                    if (entry.getValue().contains(c + 1)) {
                        constraint.addTerm(-2, eVariables.get(bidder).get(edge).get(c));
                    }
                }
                for (LSVMLicense license : f(edge)) {
                    for (int tau = 0; tau < world.getLicenses().size(); tau++) {
                        constraint.addTerm(1, aVariables.get(bidder).get(license).get(tau));
                    }
                }
                getMip().add(constraint);
            }
		}
	}

	private void buildTauConstraints() {
		for (LSVMBidder bidder : population) {
            for (LSVMLicense license : world.getLicenses()) {
                Constraint constraint = new Constraint(CompareType.LEQ, 1);
                for (int tau = 0; tau < world.getLicenses().size(); tau++) {
                    constraint.addTerm(tau + 1, aVariables.get(bidder).get(license).get(tau));
                }

                for (LSVMLicense other : world.getLicenses()) {
                    if (!other.equals(license)) {
                        for (int c = 0; c < world.getLicenses().size(); c++) {
                            Edge edge = fInv(other, license);
                            if (validPathLengths.get(edge).contains(c + 1)) {
                                constraint.addTerm(-1, eVariables.get(bidder).get(edge).get(c));
                            }
                        }
                    }
                }
                getMip().add(constraint);
            }
        }
	}

	private void initBaseValues() {
	    valueMap = new HashMap<>();
		for (LSVMBidder bidder : population) {
		    valueMap.put(bidder, new HashMap<>());
            for (LSVMLicense license : world.getLicenses()) {
                valueMap.get(bidder).put(license, bidder.getBaseValues().getOrDefault(license.getId(), BigDecimal.ZERO).doubleValue());
            }
		}
	}

	private void initA() {
	    aVariables = new HashMap<>();
		for (LSVMBidder bidder : population) {
		    aVariables.put(bidder, new HashMap<>());
            for (LSVMLicense license : world.getLicenses()) {
                aVariables.get(bidder).put(license, new HashMap<>());
                for (int tau = 0; tau < world.getLicenses().size(); tau++) {
                    Variable var = new Variable(String.format("A_i[%d]j[%d]tau[%d]", (int) bidder.getId(), (int) license.getId(), tau), VarType.BOOLEAN, 0, 1);
                    getMip().add(var);
                    aVariables.get(bidder).get(license).put(tau, var);
                }
            }
		}
	}

	private void initEdge() {
		for (LSVMLicense l1 : world.getLicenses()) {
			for (LSVMLicense l2 : world.getLicenses()) {
                Edge edge = new Edge(l1, l2);
			    if (!validPathLengths.containsKey(edge)) {
                    buildValidPathLength(edge);
                }
			}
		}
	}

	private void buildValidPathLength(Edge edge) {
		LSVMGridGraph grid = new LSVMGridGraph(world.getGrid());
		Set<Set<Vertex>> allPaths = grid.findAllPaths(grid.getVertex(edge.l1), grid.getVertex(edge.l2));

		List<Set<Vertex>> sizeOrderedPaths = allPaths.stream().sorted(Comparator.comparingInt(Set::size))
				.collect(Collectors.toList());

		List<Set<Vertex>> solution = new ArrayList<>();

		for (Set<Vertex> current : sizeOrderedPaths) {
			if (solution.stream().noneMatch(current::containsAll)) {
				solution.add(current);
			}
		}
		Set<Integer> valid = solution.stream().map(path -> path.size() - 1).collect(Collectors.toSet());
		validPathLengths.put(edge, valid);
	}

	private void initE() {
	    eVariables = new HashMap<>();
		for (LSVMBidder bidder : population) {
		    eVariables.put(bidder, new HashMap<>());
            for (Map.Entry<Edge, Set<Integer>> entry : validPathLengths.entrySet()) {
                eVariables.get(bidder).put(entry.getKey(), new HashMap<>());
                for (int c = 0; c < world.getLicenses().size(); c++) {
                    if (entry.getValue().contains(c + 1)) {
                        Variable var = new Variable(String.format("E_i[%d]e[%s]c[%d]", (int) bidder.getId(), entry.getKey(), c), VarType.BOOLEAN, 0, 1);
                        getMip().add(var);
                        eVariables.get(bidder).get(entry.getKey()).put(c, var);
                    }
                }
            }
		}
	}

	private double calculateComplementarityMarkup(int tau, LSVMBidder bidder) {
		if (tau < 1) {
			throw new IllegalArgumentException("Error: tau has to be >=1");
		}
		return bidder.calculateFactor(tau);
	}

	private LSVMLicense gMin(Edge e) {
		int neighbourCountL1 = n(e.l1).size();
		int neighbourCountL2 = n(e.l2).size();

		if (neighbourCountL1 <= neighbourCountL2) {
			return e.l1;
		} else {
			return e.l2;
		}
	}

	private LSVMLicense gMax(Edge e) {
		// return the other license on the edge not chosen by gMin
		Set<LSVMLicense> immutableSet = f(e);
		LSVMLicense minLicense = gMin(e);

		Set<LSVMLicense> mutableSet = immutableSet.stream().filter(l -> !l.equals(minLicense)).collect(Collectors.toSet());

		assert mutableSet.size() == 1;
		return mutableSet.iterator().next();
	}

	private Set<LSVMLicense> n(LSVMLicense license) {
		LSVMGrid grid = world.getGrid();
		return world.getLicenses().stream().filter(x -> grid.isNeighbor(x, license))
				.collect(Collectors.toSet());

	}

	private Set<LSVMLicense> f(Edge e) {
		return ImmutableSet.of(e.l1, e.l2);
	}

	private Edge fInv(LSVMLicense l1, LSVMLicense l2) {
		for (Edge edge : validPathLengths.keySet()) {
			if ((edge.l1 == l1 && edge.l2 == l2) || (edge.l1 == l2 && edge.l2 == l1)) {
				return edge;
			}
		}
		throw new IllegalStateException("Error: fInv edge not found");
	}

	public class Edge {
		LSVMLicense l1;
		LSVMLicense l2;

		public Edge(LSVMLicense l1, LSVMLicense l2) {
		    if (l1.getId() > l2.getId()) {
                this.l1 = l1;
                this.l2 = l2;
            } else {
		        this.l2 = l1;
		        this.l1 = l2;
            }
		}

        @Override
        public String toString() {
            return "Edge(" + (int) l1.getId() + "," + (int) l2.getId() + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return Objects.equals(l1, edge.l1) &&
                    Objects.equals(l2, edge.l2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(l1, l2);
        }
    }

}

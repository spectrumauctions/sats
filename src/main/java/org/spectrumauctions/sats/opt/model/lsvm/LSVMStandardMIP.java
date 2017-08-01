package org.spectrumauctions.sats.opt.model.lsvm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.cats.graphalgorithms.Vertex;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMGrid;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;
import org.spectrumauctions.sats.core.model.lsvm.LSVMWorld;
import org.spectrumauctions.sats.opt.model.EfficientAllocator;
import org.spectrumauctions.sats.opt.vcg.external.vcg.ItemAllocation;
import org.spectrumauctions.sats.opt.vcg.external.vcg.ItemAllocation.ItemAllocationBuilder;

import com.google.common.collect.ImmutableSet;

import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.VarType;
import edu.harvard.econcs.jopt.solver.mip.Variable;

/**
 * This class implements a mip finding the efficient allocation in an instance
 * of the Local-Synergy Value Model
 *
 * @author Nicolas Küchler
 */
public class LSVMStandardMIP implements EfficientAllocator<ItemAllocation<LSVMLicense>> {

	private IMIP mip;

	private int n; // number of agents
	private int m; // number of items

	private double[][] v;

	private Map<Long, LSVMBidder> bidderMap;
	private Map<Long, LSVMLicense> licenseMap;
	private LSVMWorld world;

	private Variable[][][] A;
	private Variable[][][] E;

	private Edge[] edges;
	private Map<Edge, Set<Integer>> validPathLengths = new HashMap<>();

	public LSVMStandardMIP(LSVMWorld world, List<LSVMBidder> population) {
		this.world = world;

		bidderMap = population.stream().collect(Collectors.toMap(b -> b.getId(), Function.identity()));
		licenseMap = world.getLicenses().stream().collect(Collectors.toMap(l -> l.getId(), Function.identity()));

		m = world.getLicenses().size();
		n = population.size();

		// init v_{ij}
		v = new double[n][m];

		// init A_{ijt}
		A = new Variable[n][m][m];

		// init E_{iec}
		int numberOfEdges = m * (m - 1) / 2;
		E = new Variable[n][numberOfEdges][m];

		edges = new Edge[numberOfEdges];

		// init MIP
		mip = new MIP();
		mip.setObjectiveMax(true);
		mip.setSolveParam(SolveParam.DISPLAY_OUTPUT, false);
		mip.setSolveParam(SolveParam.TIME_LIMIT, 3600.0);

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
	public ItemAllocation<LSVMLicense> calculateAllocation() {
		SolverClient solver = new SolverClient();
		IMIPResult result = solver.solve(mip);

		Map<Bidder<LSVMLicense>, Bundle<LSVMLicense>> allocation = new HashMap<>();
		for (int i = 0; i < n; i++) {
			Bundle<LSVMLicense> bundle = new Bundle<>();
			for (int j = 0; j < m; j++) {
				for (int t = 0; t < m; t++) {
					if (result.getValue(A[i][j][t]) > 0) {
						bundle.add(licenseMap.get((long) j));
					}
				}
			}
			allocation.put(bidderMap.get((long) i), bundle);
		}

		ItemAllocationBuilder<LSVMLicense> builder = new ItemAllocationBuilder<LSVMLicense>().withWorld(world)
				.withTotalValue(BigDecimal.valueOf(result.getObjectiveValue())).withAllocation(allocation);

		return builder.build();
	}

	private void buildObjectiveTerm() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				for (int t = 0; t < m; t++) {
					double value = calculateComplementarityMarkup(t + 1, bidderMap.get((long) i)) * v[i][j];
					mip.addObjectiveTerm(value, A[i][j][t]);
				}
			}
		}
	}

	private void buildSupplyEvalConstraints() {
		for (int j = 0; j < m; j++) {
			Constraint constraint = new Constraint(CompareType.LEQ, 1);
			for (int i = 0; i < n; i++) {
				for (int t = 0; t < m; t++) {
					constraint.addTerm(1, A[i][j][t]);
				}
			}
			mip.add(constraint);
		}
	}

	private void buildEdgeSupplyConstraints() {
		for (int e = 0; e < edges.length; e++) {
			Constraint constraint = new Constraint(CompareType.LEQ, 1);
			for (int i = 0; i < n; i++) {
				for (int c = 0; c < m; c++) {
					if (isValidPathLength(edges[e], c + 1)) {
						constraint.addTerm(1, E[i][e][c]);
					}
				}
			}
			mip.add(constraint);
		}
	}

	private void buildNeighbourConstraints() {
		for (int i = 0; i < n; i++) {
			for (int e = 0; e < edges.length; e++) {
				for (int c = 1; c < m; c++) { // only for c > 1
					if (isValidPathLength(edges[e], c + 1)) {
						Constraint constraint = new Constraint(CompareType.GEQ, 0);
						constraint.addTerm(-1, E[i][e][c]);
						for (int x : n(gMin(edges[e]))) {
							int y = gMax(edges[e]);
							if (x != y) {
								int ne = fInv(x, y);
								if (isValidPathLength(edges[ne], c)) {
									constraint.addTerm(1, E[i][ne][c - 1]);
								}
							}
						}
						mip.add(constraint);
					}
				}
			}
		}
	}

	private void buildEdgeConstraints() {
		for (int i = 0; i < n; i++) {
			for (int e = 0; e < edges.length; e++) {
				Constraint constraint = new Constraint(CompareType.GEQ, 0);
				for (int c = 0; c < m; c++) {
					if (isValidPathLength(edges[e], c + 1)) {
						constraint.addTerm(-2, E[i][e][c]);
					}
				}
				for (int j : f(edges[e])) {
					for (int t = 0; t < m; t++) {
						constraint.addTerm(1, A[i][j][t]);
					}
				}
				mip.add(constraint);
			}
		}
	}

	private void buildTauConstraints() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				Constraint constraint = new Constraint(CompareType.LEQ, 1);
				for (int t = 0; t < m; t++) {
					constraint.addTerm(t + 1, A[i][j][t]);
				}

				for (int x = 0; x < m; x++) {
					if (x != j) {
						for (int c = 0; c < m; c++) {
							int e = fInv(x, j);
							if (isValidPathLength(edges[e], c + 1)) {
								constraint.addTerm(-1, E[i][e][c]);
							}
						}
					}
				}
				mip.add(constraint);
			}
		}
	}

	private void initBaseValues() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				v[i][j] = bidderMap.get((long) i).getBaseValues().getOrDefault((long) j, new BigDecimal(0.0))
						.doubleValue();
			}
		}
	}

	private void initA() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				for (int t = 0; t < m; t++) {
					A[i][j][t] = new Variable(String.format("A_i[%d]j[%d]t[%d]", i, j, t), VarType.BOOLEAN, 0, 1);
					mip.add(A[i][j][t]);
				}
			}
		}
	}

	private void initEdge() {
		int index = 0;
		for (int x = 0; x < m; x++) {
			for (int y = x + 1; y < m; y++) {
				edges[index] = new Edge(licenseMap.get((long) x), licenseMap.get((long) y));
				buildValidPathLength(edges[index]);
				index++;
			}
		}
	}

	private void buildValidPathLength(Edge edge) {
		LSVMGridGraph grid = new LSVMGridGraph(world.getGrid());
		Set<Set<Vertex>> allPaths = grid.findAllPaths(grid.getVertex(edge.l1), grid.getVertex(edge.l2));

		List<Set<Vertex>> sizeOrderedPaths = allPaths.stream().sorted((a1, a2) -> Integer.compare(a1.size(), a2.size()))
				.collect(Collectors.toList());

		List<Set<Vertex>> solution = new ArrayList<>();

		for (Set<Vertex> current : sizeOrderedPaths) {
			if (solution.stream().filter(sol -> current.containsAll(sol)).count() == 0l) {
				solution.add(current);
			}
		}
		Set<Integer> valid = solution.stream().map(path -> path.size() - 1).collect(Collectors.toSet());
		validPathLengths.put(edge, valid);
	}

	private void initE() {
		for (int i = 0; i < n; i++) {
			for (int e = 0; e < edges.length; e++) {
				for (int c = 0; c < m; c++) {
					if (isValidPathLength(edges[e], c + 1)) {
						E[i][e][c] = new Variable(String.format("E_i[%d]e[%d]c[%d]", i, e, c), VarType.BOOLEAN, 0, 1);
						mip.add(E[i][e][c]);
					}
				}
			}
		}
	}

	private boolean isValidPathLength(Edge edge, int pathLength) {
		return validPathLengths.get(edge).contains(pathLength);
	}

	private double calculateComplementarityMarkup(int tau, LSVMBidder bidder) {
		if (tau < 1) {
			throw new IllegalArgumentException("Error: tau has to be >=1");
		}
		return bidder.calculateFactor(tau);
	}

	private Integer gMin(Edge e) {
		int l1 = (int) e.l1.getId();
		int l2 = (int) e.l2.getId();

		int neighbourCountL1 = n(l1).size();
		int neighbourCountL2 = n(l2).size();

		if (neighbourCountL1 < neighbourCountL2) {
			return l1;
		} else if (neighbourCountL1 > neighbourCountL2) {
			return l2;
		} else {
			// if both have the same number of neighbours, return the one with
			// the lower id
			return Math.min(l1, l2);
		}
	}

	private Integer gMax(Edge e) {
		// return the other license on the edge not chosen by gMin
		Set<Integer> immutableSet = f(e);
		Integer minLicense = gMin(e);

		Set<Integer> mutableSet = immutableSet.stream().filter(l -> l != minLicense).collect(Collectors.toSet());

		assert mutableSet.size() == 1;
		return mutableSet.iterator().next();
	}

	private Set<Integer> n(Integer j) {
		LSVMGrid grid = world.getGrid();
		LSVMLicense jLicense = licenseMap.get((long) j);
		return licenseMap.values().stream().filter(x -> grid.isNeighbor(x, jLicense)).map(x -> (int) x.getId())
				.collect(Collectors.toSet());

	}

	private Set<Integer> f(Edge e) {
		return ImmutableSet.of((int) e.l1.getId(), (int) e.l2.getId());
	}

	private Integer fInv(Integer l1, Integer l2) {
		for (int e = 0; e < edges.length; e++) {
			Edge edge = edges[e];

			Integer el1 = (int) edge.l1.getId();
			Integer el2 = (int) edge.l2.getId();

			if ((el1 == l1 && el2 == l2) || (el1 == l2 && el2 == l1)) {
				return e;
			}
		}
		throw new IllegalStateException("Error: fInv edge not found");
	}

	public class Edge {
		LSVMLicense l1;
		LSVMLicense l2;

		public Edge(LSVMLicense l1, LSVMLicense l2) {
			super();
			this.l1 = l1;
			this.l2 = l2;
		}
	}

}

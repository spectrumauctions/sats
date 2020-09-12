package org.spectrumauctions.sats.opt.model.lsvm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.spectrumauctions.sats.core.model.cats.graphalgorithms.Graph;
import org.spectrumauctions.sats.core.model.cats.graphalgorithms.Vertex;
import org.spectrumauctions.sats.core.model.lsvm.LSVMGrid;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;

/**
 * This class represents the LSVMGrid as a Graph
 * 
 * @author Nicolas Küchler
 */
public class LSVMGridGraph extends Graph {

	public LSVMGridGraph(LSVMGrid lsvmGrid) {
		super();

		List<Vertex> vertices = lsvmGrid.getLicenses().stream().map(license -> new Vertex(((int) license.getLongId()) + 1))
				.collect(Collectors.toList());

		addListOfVertices(vertices);

		vertices.forEach(v -> addAdjacencyList(new ArrayList<>()));

		for (int v = 0; v < vertices.size(); v++) {
			int curr = v + 1;
			if (curr % lsvmGrid.getNumberOfColumns() != 0) {
				addEdge(curr, curr + 1);
			}
			if (curr + lsvmGrid.getNumberOfColumns() <= vertices.size()) {
				addEdge(curr, curr + lsvmGrid.getNumberOfColumns());
			}
		}

	}

	public Vertex getVertex(LSVMLicense license) {
		return getVertices().stream().filter(v -> (v.getID() - 1) == (int) license.getLongId()).findAny().get();
	}

}

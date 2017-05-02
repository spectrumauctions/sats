package org.spectrumauctions.sats.core.model.cats.graphalgorithms;

import java.util.LinkedList;
import java.util.List;

public class Mesh2D extends Graph {

	public Mesh2D(int x, int y)
	{
		super();
		Vertex[] vrts = new Vertex[x*y];
		for(int i = 0; i < x*y; ++i)
			vrts[i] = new Vertex(i+1);
		
		List<Vertex> vertices = new LinkedList<Vertex>();
		addListOfVertices(vertices);
		
		for(int i = 0; i < x*y; ++i)
		{
			vertices.add(vrts[i]);
			List<VertexCell> adjList = new LinkedList<VertexCell>();
			
			if( ! (vrts[i].getID() % x == 1))
				adjList.add(new VertexCell(vrts[i-1],1));
			if( ! (vrts[i].getID() % x == 0))
				adjList.add(new VertexCell(vrts[i+1],1));
			if( vrts[i].getID() - x > 0)
				adjList.add(new VertexCell(vrts[i-x],1));
			if( vrts[i].getID() + x <= x*y)
				adjList.add(new VertexCell(vrts[i+x],1));
			addAdjacencyList(adjList);
		}
	}
		public String toString()
		{
			String str = "MESH ";
			return str + super.toString();
		}
}

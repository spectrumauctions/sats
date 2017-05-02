package org.spectrumauctions.sats.core.model.cats.graphalgorithms;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/*
 * The class extends the Graph class and implements some spectral properties/methods 
 * for graphs:
 * - 
 * 
 */
public class SpectralGraph extends Graph {

	public SpectralGraph(final List<Vertex> vertices, final List<VertexCell> ...  adjLst)
	{
		super(vertices, adjLst);
	}

	/*
	 * The method returns a contracted graph, i.e. the graph that does not contain edges with weights smaller than wMin.
	 * For every such an edge its vertices are aggregated into a single vertex
	 * WARNING: the method modifies the vertices of the initial graph!!!
	 * @param wMin - the minimum possible value for weight of an edge
	 * @return a contracted graph
	 */
	public Graph contractIt( double wMin )
	{
		List<Vertex> vertices = new LinkedList<Vertex>();
		List< List<VertexCell> > adjLists = new LinkedList< List<VertexCell> >();
		
		List<Vertex> removedVertices = new LinkedList<Vertex>();
		Iterator<Vertex> it = _vertices.iterator();
		int i = 0;																					//a counter for vertices and adjacency lists in the new graph
		while(it.hasNext())
		{
			Vertex v = it.next();
			if( removedVertices.contains(v))
				continue;
			List< VertexCell > adjList = new LinkedList<VertexCell>();								//a new adjacency list for the vertex
			
			for(VertexCell vc : _adjacencyLists.get(v.getAdjacencyListIndex()) )
				if( removedVertices.contains(vc._v))
					continue;
				else if( vc._w < wMin )																//The condition to remove the edge
				{
					v.addChildVertex(vc._v);														//Add all the child vertices
					for( int j = 0; j < vc._v.getNumberOfChilds(); ++j)
						v.addChildVertex(vc._v.getChildVertex(j));

					removedVertices.add(vc._v);														//The peer should be removed from the graph
					
					for( VertexCell vcV : _adjacencyLists.get(v.getAdjacencyListIndex()))			//The vertices adjacent to v will still be adjacent to the new vertex
						if( ( ! vcV._v.equals(vc._v)) && ( ! adjList.contains(vcV) ) && (vcV._w >= wMin))
							adjList.add(vcV);
					
					List< VertexCell > listToUse = new LinkedList<VertexCell>();
					if( vertices.contains(vc._v))													//If the vertex was handled previously,
						listToUse = adjLists.get(vc._v.getAdjacencyListIndex());					//use its new adjacency list;
					else
						listToUse = _adjacencyLists.get(vc._v.getAdjacencyListIndex());				//otherwise use its initial adjacency list.
					
					for( VertexCell vcV : listToUse)												//The vertices that are adjacent to the peer will be adjacent to v
						if( ! vcV._v.equals(v))														//(avoid self-loops)
						{
							if( ! adjList.contains(vcV) )
								adjList.add(vcV);
							
							List<VertexCell> neiListToUse = new LinkedList<VertexCell>();
							if( vertices.contains(vcV._v))											//If the vertex was handled previously,
								neiListToUse = adjLists.get(vcV._v.getAdjacencyListIndex());		//use its new adjacency list;
							else
								neiListToUse = _adjacencyLists.get(vcV._v.getAdjacencyListIndex());	//otherwise use its initial list.
							for(VertexCell vcU : neiListToUse)										//As the peer will be removed, its neighbors now should be adjacent to v
								if( vcU._v.equals(vc._v))
									vcU._v = v;
						}

					if( vertices.contains(vc._v))
					{
						for(int j = vertices.indexOf(vc._v) + 1; j < vertices.size(); ++j)
							vertices.get(j).setAdjacencyListIndex( vertices.get(j).getAdjacencyListIndex() - 1);

						adjLists.remove(vc._v.getAdjacencyListIndex());
						vertices.remove(vc._v);
						i -= 1;
					}
				}
				else if( ! adjList.contains(vc))
					adjList.add(vc);
			
			v.setAdjacencyListIndex(i++);
			vertices.add(v);
			adjLists.add(adjList);			
		}
		return new Graph(vertices, adjLists);
	}
}

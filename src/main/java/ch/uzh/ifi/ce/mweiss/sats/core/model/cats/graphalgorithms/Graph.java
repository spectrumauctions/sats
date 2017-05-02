package ch.uzh.ifi.ce.mweiss.sats.core.model.cats.graphalgorithms;

import java.util.*;

//import org.gnu.glpk.*;
/**
 * The class implements the graph structure (via adjacency lists) and simple algorithms 
 * for processing the graph. The vertices of the graph should be enumerated from 1 to N (not from 0 to N).
 * The list of implemented algorithms:
 * - Breadth-First Search
 * - Bellman-Ford single source graph search algorithm ( complexity O(V*E) )
 * - Dijkstra single source graph search algorithm (complexity O(V*V); priority queue is used => for large graphs it is lower )
 * - Ford-Fulkerson maximum flow search
 * - Multicommodity Flow search with fractional flows (search for the feasible solution)
 * - Multicommodity FLow search with fractional flows (with minimization of the maximum d_i / f_i  - e.g. time needed to transfer d_i Bytes with f_i bandwidth)
 * - Low Stretch Spanning tree composition
 * @author Dmitry Moor
 */
public class Graph {

	/**
	 * Constructor
	 * @param vertices - a list of vertices of the graph
	 * @param vrts - a set of adjacency lists. The 1st list should correspond to the vertex with id=1,
	 *        the 2nd list - to the vertex with id=2, etc...
	 */
	public Graph(final List<Vertex> vertices, final List<VertexCell> ...  adjLst)
	{
		_adjacencyLists = new LinkedList<List<VertexCell>>();
		_edges = new LinkedList<Edge>();
		_vertices = vertices;
		_radius = 0;
		
		for(int i = 0; i < adjLst.length; ++i)				//Init adjacency lists
			_adjacencyLists.add(adjLst[i]);
	}
	
	/**
	 * 
	 * @param vertices
	 * @param adjLsts
	 */
	public Graph(final List<Vertex> vertices, final List< List<VertexCell> >  adjLsts)
	{
		_adjacencyLists = new LinkedList<List<VertexCell>>();
		_edges = new LinkedList<Edge>();
		_vertices = new LinkedList<Vertex>();
		_radius = 0;
		
		for(Vertex v : vertices)
			_vertices.add(v);
		for(int i = 0; i < adjLsts.size(); ++i)				//Init adjacency lists
			_adjacencyLists.add(adjLsts.get(i));
	}
	
	/**
	 * The method constructs
	 * @param vertices -  a list of vertices
	 */
	public Graph(final List<Vertex> vertices)
	{
		_adjacencyLists = new LinkedList<List<VertexCell>>();
		_vertices = vertices;
		_radius = 0;
	}
	
	/**
	 * The method constructs am empty graph
	 */
	public Graph()
	{
		_adjacencyLists = new LinkedList<List<VertexCell>>();
		_vertices = new LinkedList<Vertex>();
		_edges = new LinkedList<Edge>();
		_radius = 0;
	}

	/**
	 * The method returns the number of edges of the graph
	 * @return the number of edges of the graph
	 */
	public int getNumberOfEdges()
	{
		return _adjacencyLists.stream().map( lst -> lst.size() ).reduce((x1, x2) -> x1 + x2).get();
	}

	/**
	 * The method returns the list of vertices of the graph
	 * @return the list of vertices of the graph
	 */
	public List<Vertex> getVertices()
	{
		return _vertices;
	}

	/**
	 * The method returns adjacency lists of the graph
	 * @return a list of adjacency lists of the graph
	 */
	public List<List<VertexCell> > getAdjacencyLists()
	{
		return _adjacencyLists;
	}
	
	/*
	 * The method returns a list of edges of the graph
	 * @return a list of edges of the graph
	 */
	public List<Edge> getEdges()
	{
		if(_edges.size() <= 0)
			constructListOfEdges(0);
		return _edges;
	}

	/**
	 * The method updates the list of vertices of the graph
	 * @param a new list of vertices of the graph
	 */
	public void addListOfVertices(List<Vertex>  vrts)
	{
		_vertices = vrts;
	}

	/**
	 * The method adds an adjacency list
	 * @param adjList - a new adjacency list
	 */
	public void addAdjacencyList(List<VertexCell>  adjLst)
	{
		_adjacencyLists.add(adjLst);
	}
	
	/**
	 * (non-Javadoc)
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		String str = "Graph:\n";
		int i = 1;
		for(List<VertexCell> vList : _adjacencyLists)
		{
			str += _vertices.get(i-1).getID() + " | ";
			for(VertexCell v : vList)
				str += "->" + v.toString() + " f=" + v._f;
			str += "\n";
			++i;
		}
		for(Vertex v : _vertices)
			str += " {ID=" + v.getID() + " Pi=" + v.getPredecessor(0) + " Sh.Est" + v.getShortestPathEst(0) + " }" ;
		
		str += "\n" + generateGEXF();
		
		return str;
	}
	
	/**
	 * The method generates a XML representation of the graph using the GEXF schema.
	 * This is used for a GUI representation in, e.g., Gephi.
	 * @return a string containing GEXF representation of the graph.
	 */
	private String generateGEXF()
	{
		String gexf = "";
		//Header
		gexf += "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
		gexf += "<gexf xmlns=\"http://www.gexf.net/1.2draft\"\n";
		gexf += "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
		gexf += "      xsi:schemaLocation=\"http://www.gexf.net/1.2draft\n";
		gexf += "                           http://www.gexf.net/1.2draft/gexf.xsd\"\n";
		gexf += "      version=\"1.2\"\n";
		gexf += "  <meta lastmodifieddate=\"2009-03-20\">\n";
		gexf += "    <creator>Gephi.org</creator>\n";
		gexf += "    <description>Graph representation</description>\n";
		gexf += "    <keywords>Graph</keywords>\n";
		gexf += "  </meta>\n";
		
		//Vertices
		gexf += "  <nodes>\n";
		for(Vertex v : _vertices)
			gexf += "    <node id=\""+v.getID()+"\" label=\""+v.getID()+"\" />\n";
		gexf += "  </nodes>\n";
		
		//Edges
		gexf += "  <edges>\n";
		for(int i = 0; i < _vertices.size(); ++i)
		{
			Vertex src = _vertices.get(i);
			for(VertexCell vc : _adjacencyLists.get(i))
				gexf += "    <edge id=\""+src.getID()+"" + vc._v.getID()+ "\" source=\""+src.getID()+"\" target=\""+vc._v.getID()+"\" />\n";
		}
		gexf += "  </edges>\n";
		
		gexf += "</gexf>\n";
		return gexf;
	}
	
	/**
	 * The method returns the output degree of a given vertex 
	 * @param vertexId - the id of the vertex for which the output degree should be computed
	 * @return the output degree of the given vertex
	 */
	public int getOutputDegree(int vertexId)
	{
		return _adjacencyLists.get(vertexId - 1).size();
	}
	
	/**
	 * The method returns the input degree of a given vertex
	 * @param vertexId - the id of the vertex for which the input degree should be computed
	 * @return the input degree of the given vertex 
	 */
	public long getInputDegree(int vertexId)
	{
		return _adjacencyLists.stream().flatMap( adjList -> adjList.stream() ).filter( vc -> vc._v.getID() == vertexId ).count();
	}

	/**
	 * The method returns the predesessor of the vertex (obtained e.g. by Dijkstra or BFS)
	 * @param vertexId an id of the vertex
	 * @return
	 */
	public int getVertexPredecessor(int vertexID)
	{
		for(Vertex v : _vertices)
			if(v.getID() == vertexID)
				return v.getPredecessor(0);
		return 0;
	}
	
	/*
	 * The method returns a path between two vertices i.e. a list of vertices obtained by
	 * analyzing predecessors for each vertex (based for example on prior Dijkstra or BFS method calls)
	 * @param v - the first vertex in the path
	 * @param u - the last vertex of the path
	 * @return the path between u and v
	 */
	public List<VertexCell> getPath(Vertex v, Vertex u)
	{
		if(u.getPredecessor(0) == 0)
			throw new RuntimeException("No Path from v to u exists. v=" + v.getID() + " u=" + u.getID() +" " + this.toString());
		
		List<VertexCell> path = new LinkedList<VertexCell>();
		
		Vertex next = u;
		u = _vertices.get(u.getPredecessor(0)-1);
		
		while( u.getID() != v.getID())
		{
			for(VertexCell vc : _adjacencyLists.get(u.getID()-1))
				if( vc._v == next)
					path.add( vc );
			next = u;
			u = _vertices.get( u.getPredecessor(0)-1 );
		}
		for(VertexCell vc : _adjacencyLists.get(u.getID()-1))
			if( vc._v == next)
				path.add( vc );
		return path;
	}
	
	/*
	 * The method returns flow tables obtained after MCF
	 * @return flow tables
	 */
	public double[][][] getFlowTables()
	{
		return _flowTables;
	}
	
	
	/*
	 * The method returns the radius of the graph i.e. the smallest r s.t. every vertex in the graph
	 * is within distance at most r from the specified vertex
	 */
	public double getRadius(Vertex v, boolean recompute)
	{
		if( recompute == false )
			return _radius;
		
		Dijkstra(v, 0);
		_radius = 0;
		for( Vertex vrt : _vertices)
			if( vrt.getPredecessor(0) > 0 && vrt.getShortestPathEst(0) > _radius)
				_radius = vrt.getShortestPathEst(0);
		return _radius;
	}
	
	/*
	 * The method returns a subgraph induced by a subset of vertices that are at most 
	 * at distance=radius from the center vertex
	 * @param center - the center vertex of the ball
	 * @param radius - the radius of the ball
	 * @param recompute - the boolean value indicating if it is necessary to recompute shortest paths estimations
	 * @return a ball subgraph
	 */
	public Graph getBall(Vertex center, double radius, boolean recompute)
	{
		if( recompute == true )
			Dijkstra(center, 0);
		
		List<Vertex> vertices = new LinkedList<Vertex>();
		List< List<VertexCell> > adjLists = new LinkedList< List<VertexCell> >();
		List< List<VertexCell> > adjListsNew = new LinkedList< List<VertexCell> >();
		
		//Find all vertices that are close enough to the center of the ball
		int curVertex = 0;
		for(Vertex v : _vertices)
		{
			if(v.getShortestPathEst(0) < radius || Math.abs( v.getShortestPathEst(0) - radius) < 1e-6)
			{
				vertices.add(v);
				adjLists.add(_adjacencyLists.get(curVertex));
			}
			curVertex += 1;
		}

		//Remove all edges for which one vertex is outside the ball
		for( List<VertexCell> adjList : adjLists)
		{
			List<VertexCell> newAdjList = new LinkedList<VertexCell>();
			for(VertexCell vc : adjList)
				if( vertices.contains(vc._v))
					newAdjList.add(vc);
			adjListsNew.add(newAdjList);
		}
		
		Graph ball = new Graph(vertices, adjListsNew);
		return ball;
	}
	
	/*
	 * The method returns a ball shell of a graph with a center in 'center' and with the given radius
	 * @param center - the center of the corresponding ball
	 * @param radius - radius of the ball
	 * @param recompute - a boolean value indicating if it is needed to recompute the shortest path estimations
	 * @return a list of vertices distributed over the ball shell (sphere)
	 */
	public List<Vertex> getBallShell(Vertex center, double radius, boolean recompute)
	{
		if( recompute == true )
			Dijkstra(center, 0);
		
		List<Vertex> vertices = new LinkedList<Vertex>();
		List< List<VertexCell> > adjLists = new LinkedList< List<VertexCell> >();
		List<Vertex> ballShell = new LinkedList<Vertex>();
		
		//Find all vertices that are close enough to the center of the ball
		int curVertex = 0;
		for(Vertex v : _vertices)
		{
			if(v.getShortestPathEst(0) < radius || Math.abs( v.getShortestPathEst(0) - radius) < 1e-6)
			{
				vertices.add(v);
				adjLists.add(_adjacencyLists.get(curVertex));
			}
			curVertex += 1;
		}

		//Remove all edges for which one vertex is outside the ball
		for( List<VertexCell> adjList : adjLists)
			for(VertexCell vc : adjList)
				if( ! vertices.contains(vc._v) && ! ballShell.contains(vc._v))
					ballShell.add(vc._v);

		return ballShell;
	}
	
	/*
	 * O(n*n) 
	 */
	public List<Edge> getBoundary(List<Vertex> vertices)
	{
		List<Edge> boundary = new LinkedList<Edge>();
		for(Vertex v : vertices)
			for(VertexCell vc : _adjacencyLists.get(v.getAdjacencyListIndex()))
				if( ! vertices.contains(vc._v) )
					boundary.add( new Edge(v, vc._v, vc._w, 0) );
		return boundary;
	}
	
	/*
	 * The method returns the cone with a vertex in 'center' and a given radius in respect to the set
	 * of vertices S
	 * @param center - the vertex of the cone
	 * @param radius - the radius ot the cone (i.e. max distance)
	 * @param S - the set in respect to which the cone should be defined
	 * @return the cone
	 */
	public List<Vertex> getCone(Vertex center, double radius, List<Vertex> S)
	{
		List<Vertex> cone = new LinkedList<Vertex>();
		
		//For every vertex of the graph compute the distance to the 'center'
		Dijkstra(center, 0);
		
		int counter = 1;
		//For every vertex of the graph compute the distance to the set S
		for(Vertex v : S)
			if( ! v.equals(center) )
			{
				Dijkstra(v, counter);
				counter += 1;
			}

		//Add to the cone all the vertices that are closer to the 'center' than to any other point from S
		for(Vertex v : _vertices)
		{
			int localCounter = 0;
			for(int i = 1 ; i < S.size(); ++i)
				if( (v.getShortestPathEst(0) < v.getShortestPathEst(i) + radius) && (! cone.contains(v)) )
					localCounter += 1;
			if( localCounter == S.size() - 1)
				cone.add(v);
		}
		return cone;
	}
	
	/*
	 * The method computes the cost of a set of edges i.e. the sum of 1/w_i for i=1...numberOfEdges
	 * (w_i) is weights of edges
	 * @param a list of edges for which the cost should be computed
	 * @return the cost of the given set of edges
	 */
	public double computeCost(List<Edge> edges)
	{
		double cost = 0;
		for(Edge e : edges)
			cost += 1 / e.getCapacity();
		return cost;
	}
	
	/*
	 * Complexity
	 */
	public double BallCut(Vertex x, double rad, double delta)
	{
		double r = rad * delta;
		int m = _edges.size() / 2;      							//Undirected graph
		while( computeCost( getBoundary( getBall(x, r, false).getVertices() ) )  > (this.getBall(x, r, false).getVertices().size() + 1) * (Math.log(m+1) / Math.log(2)) / ( (1-2*delta)*rad ) )
		{
			double curDist = Double.MAX_VALUE;
			for(Vertex v : _vertices)
				if( ! getBall(x, r, false).getVertices().contains(v) )
					if( v.getShortestPathEst(0) < curDist)
						curDist = v.getShortestPathEst(0);
			r = curDist;
		}
		return r;
	}
	
	/*
	 * 
	 */
	public double ConeCut(Vertex center, double lambda, double lambda1, List<Vertex> S)
	{
		double r = lambda;
		double mue = 0;
		Graph Cs = induceGraph( getCone(center, lambda, S));
		
		if( Cs.getNumberOfEdges() == 0)
			mue = ( Cs.getVertices().size() + 1 ) * Math.log(this.getNumberOfEdges() + 1) / Math.log(2);
		else
			mue = Cs.getVertices().size() * Math.log( (this.getNumberOfEdges()) / (Cs.getNumberOfEdges()) ) / Math.log(2);
		
		while( computeCost( getBoundary( getCone(center, r, S) )) > mue / (lambda1 - lambda))
		{
			double curDist = Double.MAX_VALUE;
			for(Vertex v: _vertices)
				if( ! getCone(center, r, S).contains(v))
					if( v.getShortestPathEst() < curDist)
						curDist = v.getShortestPathEst();
			r = r + curDist;
		}
		return r;
	}
	
	/*
	 * The method implements the cone decomposition of the graph.
	 * @param S - the list of cone vertices
	 * @param delta - delta parameter of the algorithm
	 * @param coneVertices - a list of cone vertices (used as an additional output parameter of the method)
	 * @return a list of cones of the decomposition
	 */
	public List< List<Vertex> > coneDecomp(List<Vertex> S, double delta, List<Vertex> coneVertices)
	{
		Graph Gk = new Graph( getVertices(), getAdjacencyLists());
		List< List<Vertex> > res = new LinkedList< List<Vertex> >();
		
		while( S.size() > 0)
		{
			Vertex x = S.get(  (int) Math.floor( S.size() * Math.random()) );
			coneVertices.add(x);

			double r = Gk.ConeCut(x, 0, delta, S);

			List<Vertex> Vk = Gk.getCone(x, r, S);
			res.add(Vk);
			
			Gk.getVertices().removeAll(Vk);
			Gk = Gk.induceGraph( Gk.getVertices() );
			S.removeAll(Vk);
			if( S.size() == 1)
			{
				res.add(Gk.getVertices());
				coneVertices.add(S.get(0));
				break;
			}
		}
		return res;
	}
	
	/*
	 * The method returns the Star-decomposition of the graph
	 * @param x0 - the center of the star
	 * @param delta - delta parameter of the algorithm ( =0.333)
	 * @param epsilon - epsilon parameter of the algorithm (precision)
	 * @param y - a list of vertices within the center-ball of the star that are the closest ones to the cone vertices (used as an additional return value)
	 * @return list of cones and balls of the star decomposition 
	 */
	public List< List<Vertex> > starDecomp(Vertex x0, double delta, double epsilon, List<Vertex> y, List<Vertex> coneVertices)
	{
		double rad = getRadius(x0, true);
		double r0  = BallCut(x0, rad, delta);
		if( rad == r0 )														//No cones in this case
		{
			List< List<Vertex> > star = new LinkedList< List<Vertex> >();
			star.add( getVertices());
			return star;
		}
		
		Graph B0 = getBall(x0, r0, false);									//V0 set
		List<Vertex> BS = getBallShell(x0, r0, false);
		
		List<Vertex> graphVertices = new LinkedList<Vertex>();
		for(Vertex v : _vertices)
			if( ! B0.getVertices().contains(v))
				graphVertices.add( v );

		Graph G1 = induceGraph( graphVertices);
		
		List< List<Vertex> > star = G1.coneDecomp(BS, epsilon*rad/2, coneVertices);

		int i = 0;
		for(Vertex ballV : B0.getVertices())
			Dijkstra(ballV, i++);

		for(Vertex v: coneVertices)											//Find the ball vertices nearest to the coneVertices
		{
			int minIdx = 0;
			double minDist = Double.MAX_VALUE;
			for( int j = 0; j < B0.getVertices().size(); ++j)
				if( v.getShortestPathEst(j) < minDist)
				{
					minDist = v.getShortestPathEst(j);
					minIdx = j;
				}

			y.add(B0.getVertices().get(minIdx));							// coneVertices(i) -- y(i) is the bridge between the cone and the ball
		}
		
		star.add(B0.getVertices());
		coneVertices.add(x0);
		return star;
	}
	
	/*
	 * The method returns the graph induced from the current one by a set of vertices
	 * Complexity O(n*m)  because of adjacency lists. If to use matrix => O( n ) - just remove the missing rows and columns  TODO
	 * WARNING: the indexes of vertices in _vertices are not continuous anymore!!!
	 * WARNING: the graph contains copies of the vertices and adjacency lists of the original graph, but not the originals!!!
	 * @param vertices - the list of vertices that should be present in the induced graph (NOTE: the vertices objects should belong to THIS object
	 *                   as they refer to the adjacency lists of THIS graph)
	 * @return an induced graph
	 */
	public Graph induceGraph(List<Vertex> vertices)
	{
		List< List<VertexCell> > adjLists = new LinkedList< List<VertexCell> >();
		List< Vertex > newVertices = new LinkedList<Vertex>();
		int i = 0;
		for(Vertex v : vertices)
		{
			Vertex newV = v.cloneIt();
			newV.setAdjacencyListIndex(i);			
			List<VertexCell> adjList = new LinkedList<VertexCell>();
			for(VertexCell vc : _adjacencyLists.get(v.getAdjacencyListIndex() ) )
				if( vertices.contains(vc._v) )
					adjList.add( new VertexCell(vc._v, vc._w, vc._f) );
			adjLists.add( adjList );
			newVertices.add(newV);
			//In the induced graph the vertex ID might not coincide with the index of the associated adjacency list => a new index is required:
			i += 1;
		}
		//For every adjacency list replace an adjacent vertex with the new copy of the vertex in the induced graph
		for(List<VertexCell> adjList : adjLists)
			for(VertexCell vc : adjList)
				for(Vertex v: newVertices)
					if(vc._v.getID() == v.getID())
					{
						vc._v = v;
						break;
					}
		
		Graph g = new Graph(newVertices, adjLists);
		return g;
	}
	
	/*
	 * The method generate a Low Stretch Spanning Tree for the graph
	 * @param center - the center (the root) of the tree
	 * @param betta - betta parameter for the algorithm
	 * @param bridges - edges x(i)-y(i) that are the bridges between different cones and balls
	 * @return a low stretch spanning tree
	 */
	public List<Graph> lowStretchTree(Vertex center, double betta, List<Edge> bridges)
	{
		if( getVertices().size() <= 2)										//Trivial case
		{
			List<Graph> res = new LinkedList<Graph>();
			res.add(this);
			return res;
		}
		
		double rad = getRadius(center, true);								//TODO: Used only if to utilize that edge contraction mechanism
		List<Vertex> y = new LinkedList<Vertex>();							//The ball vertices that are close to x:
		List<Vertex> x = new LinkedList<Vertex>();							//Cone vertices
		List<Graph > T = new LinkedList<Graph >();
		
		List< List<Vertex> > V = starDecomp(center, 1./3, betta, y, x);
		if( V.size() == 1)													//Only a center-ball
			if( getNumberOfEdges()/2 == getVertices().size() - 1)			//It's a tree
			{
				T.add(this);
				return T;
			}
			else if( getNumberOfEdges()/2 == getVertices().size() )			//Remove arbitrary edge
			{
				Vertex peer = _adjacencyLists.get(0).get(0)._v;
				for( VertexCell vc : _adjacencyLists.get(peer.getAdjacencyListIndex()) )
					if(vc._v.equals( _vertices.get(0) ))
					{
						_adjacencyLists.get( peer.getAdjacencyListIndex()).remove(vc);
						_adjacencyLists.get(0).remove(0);
						break;
					}
				T.add(this);
				return T;
			}
			else
			{
				throw new RuntimeException("Not a tree");					//TODO: return any spanning tree
			}
		
		for( int i = 0; i < V.size(); ++i)
		{
			List<Vertex> newVList = new LinkedList<Vertex>();
			for( Vertex v : V.get(i) )										//Replace with the vertices of THIS graph (they contain the right references to  adjLists)
				for( Vertex curV : _vertices )
					if( curV.equals(v) )
					{
						newVList.add( curV );
						break;
					}
			Graph Gi = induceGraph( newVList );								//Reduce the problem
			List<Graph> Ti = Gi.lowStretchTree(x.get(i), betta, bridges);	//... and solve it
			T.addAll(Ti);
		}
		for( int i = 0; i < y.size(); ++i)									//Fill bridges
		{
			Edge e = new Edge(x.get(i), y.get(i), 0);
			bridges.add(e);
		}
		return T;
	}
	
	/*
	 * The method returns true if there is an edge from the vertex U to the vertex V and false otherwise
	 * @param vertexU - id of the 1st vertex
	 * @param vertexV - id of the 2nd vertex
	 * @return true if these vertices are adjacent and false otherwise
	 */
	public boolean isAdjacent(Vertex u, Vertex v)
	{
		for(VertexCell vc : _adjacencyLists.get(u.getID()-1))
			if(vc._v.getID() == v.getID())
				return true;
		
		return false;
	}

	/*
	 * The method implements the Breadth-First-Search in the graph given the source vertex
	 * @param s - the source vertex in the graph
	 */
	public void BFS(Vertex s)
	{
		for(Vertex v : _vertices)						//INITIALIZATION STEP
		{
			v.setColor(WHITE);
			v.setPredecessor(WHITE, 0);
			v.setShortestPathEst(Double.MAX_VALUE, 0);
		}
		s.setColor(GREY);
		s.setPredecessor(WHITE, 0);
		s.setShortestPathEst(WHITE, 0);
		
		List<Vertex> queue = new LinkedList<Vertex>();	//START
		queue.add(s);
		while(!queue.isEmpty())
		{
			Vertex u = queue.remove(0);					//enqueue the 1st element
			for(VertexCell v : _adjacencyLists.get(u.getID()-1))
				if( v._v.getColor() == WHITE)
				{
					v._v.setColor(GREY);
					v._v.setPredecessor(u.getID(), 0);
					v._v.setShortestPathEst(u.getShortestPathEst(0) + 1, 0);
					queue.add(v._v);
				}
			u.setColor(BLACK);
		}
	}
	
	/*
	 * The method implements Ford-Fulkerson maximum flow search method given the source vertex and the sink vertex
	 * @param s - the source vertex
	 * @param t - the sink vertex
	 */
	public void FordFulkerson(Vertex s, Vertex t)
	{
		for(Vertex v : _vertices)
			for(VertexCell vc : _adjacencyLists.get(v.getID()-1))
				vc._f = 0;
		
		Graph residualNetwork = buildResidualNetwork();
		residualNetwork.BFS(s);
		while( residualNetwork.getVertexPredecessor(t.getID()) != 0)		//While there exists a path from s to t
		{
			List<VertexCell> path = residualNetwork.getPath(s, t);
			double cf = Double.MAX_VALUE;									//Minimum residual capacity
			for(VertexCell vc : path)
				if(vc._w < cf)
					cf = vc._w;
			
			Vertex cur = s;
			Collections.reverse(path);
			for(VertexCell vc : path)										//Correct the flow values for the graph edges within the path
			{
				for(VertexCell vc1 : _adjacencyLists.get(cur.getID()-1))
					if(vc1._v.getID() == vc._v.getID())
					{
						vc1._f = vc1._f + cf;
						break;
					}
				cur = vc._v;
			}
			residualNetwork = buildResidualNetwork();						//Update residual network
			residualNetwork.BFS(s);
		}
	}
	
	/*
	 * The method implements the Bellman-Ford graph search algorithm (the complexity is O(V*E) )
	 * @param sourceID - the id of the source vertex
	 * @return true if there exist shortest paths and false otherwise. The shortest path itself i.e. predecessors
	 *         for each vertex and the weights of paths may be traced by printing the vertices
	 */
	public boolean BellmanFord(Vertex sourceID, int idx)
	{
		initSingleSource(sourceID, idx);
		
		for(int i = 1; i <= _vertices.size() - 1; ++i)
			for(Vertex u : _vertices)
				for(VertexCell vc : _adjacencyLists.get(u.getID()-1))
					relax(u, vc._v, vc._w, idx);
		
		for(Vertex u : _vertices)
			for(VertexCell vc : _adjacencyLists.get(u.getID()-1))
				if(vc._v.getShortestPathEst(0) > u.getShortestPathEst(0) + vc._w)
					return false;
		return true;
	}

	/*
	 * The method implements the Dijkstra graph search algorithm (the complexity is O(V*V) )
	 * @param - the id of the source vertex
	 */
	public void Dijkstra(Vertex source, int idx)
	{
		initSingleSource(source, idx);
		Set<Vertex> processedVertices = new HashSet<Vertex>();
		PriorityQueueMin<VertexCell> queue = new PriorityQueueMin<VertexCell>(_vertices.size());
		
		for(Vertex v : _vertices)
			queue.insert(new VertexCell(v, 0), idx);
		
		while( ! queue.isEmpty())
		{
			VertexCell u = queue.extractMin(idx);							//Extract the point with the minimum shortest path estimation
			processedVertices.add(u._v);
			for(VertexCell vc : _adjacencyLists.get(u._v.getAdjacencyListIndex()))
			{
				double newKey = relax(u._v, vc._v, vc._w, idx);
				if( newKey > 0)
					queue.decreaseKey(vc, newKey, idx);
			}
		}
	}
	
	/*
	 * Initialize the shortest paths estimations and predecessors given the source ID
	 * @param sourceID - the id of the source for the shortest paths
	 * @param idx - the index of shortest path estimations / predecessors (there might be several Dijkstra invokations from different vertices)
	 */
	private void initSingleSource(Vertex sourceID, int idx)
	{
		_source = sourceID;
		for( Vertex v : _vertices)
			if( v.getID() != _source.getID())
			{
				v.setShortestPathEst(Double.MAX_VALUE, idx);
				v.setPredecessor(0, idx);
			}
			else
			{
				v.setPredecessor(0, idx);
				v.setShortestPathEst(0, idx);
			}
	}

	/*
	 * The method performs the relaxation of the edge u-->v as one of the steps of the Bellman-Ford algorithm
	 * @param u - the 1st vertex of the edge
	 * @param v - the 2nd vertex of the edge
	 * @param w - the weight of this edge (u-->v)
	 * @param idx - the index of shortest path estimations / predecessors (there might be several Dijkstra invokations from different vertices)
	 * @return the new key or 0 in case if the key was not updated
	 */
	private double relax(Vertex u, Vertex v, double w, int idx)
	{
		if(v.getShortestPathEst(idx) > u.getShortestPathEst(idx) + w)
		{
			v.setShortestPathEst(u.getShortestPathEst(idx) + w, idx);
			v.setPredecessor(u.getID(), idx);
			return v.getShortestPathEst(idx);
		}
		return 0;
	}
	
	/*
	 * The method builds the residual network for this graph
	 */
	public Graph buildResidualNetwork()
	{
		Graph g = new Graph(_vertices);
		
		for(Vertex v : _vertices)											//Consider vertex v
		{
			List<VertexCell> adjList = new LinkedList<VertexCell>();
			for(Vertex u : _vertices)
			{
				double residualCapacity = 0;
				if(isAdjacent(v, u))										//For edge v-->u
					for(VertexCell vc : _adjacencyLists.get(v.getID()-1))
						if(vc._v == u)
							residualCapacity = vc._w - vc._f;
				if( isAdjacent(u, v))										//For edge u-->v
					for(VertexCell vc : _adjacencyLists.get(u.getID()-1))
						if(vc._v == v)
							residualCapacity = residualCapacity + vc._f;
				if(residualCapacity != 0)
					adjList.add( new VertexCell(u, residualCapacity));
			}
			g.addAdjacencyList(adjList);
		}
		return g;
	}
	
	/*
	 * The method removes one edge from the graph
	 * @param vertexId - an ide of a source vertex of the edge
	 * @param edgeIdx - an index of the edge to be removed
	 */
	public void removeEdge(int vertexId, int edgeIdx)
	{
		int sinkId = _adjacencyLists.get(vertexId - 1).get(edgeIdx)._v.getID();
		_adjacencyLists.get(vertexId - 1).remove(edgeIdx);
		
		for(int i = 0; i < _adjacencyLists.get(sinkId - 1).size(); ++i)
			if(_adjacencyLists.get(sinkId - 1).get(i)._v.getID() == vertexId)
			{
				_adjacencyLists.get(sinkId - 1).remove(i);
				break;
			}
	}
	
	/**
	 * The method adds one edge to the graph
	 * @param vertexId - an id of a source vertex of the edge
	 * @param edgeIdx - an index of the edge to be removed
	 */
	public void addEdge(int sourceId, int sinkId)
	{
		_adjacencyLists.get(sourceId - 1).add(new VertexCell( _vertices.get(sinkId-1), 1.) );
		_adjacencyLists.get(sinkId - 1).add(new VertexCell( _vertices.get(sourceId-1), 1.) );
	}

	/*
	 * The method solves the Multicommodity Flow problem on the given graph. Current implementation reduces the problem
	 * to the LP problem allowing by this fractional flows.
	 * @param sources - the list of source vertices
	 * @param sinks - the list of the corresponding sinks
	 * @param demands - demands of each source-sink pair
	 */
	/*public void MCF(List<Vertex> sources, List<Vertex> sinks, double[] demands)
	{
		if(sources.size() != sinks.size()) 
			throw new RuntimeException("The number of sources not equal to the number of sinks:"+sources.size()+"/"+sinks.size());

		if(_edges.size() <= 0)
			constructListOfEdges(demands.length);
		
		//Init problem:
		glp_prob lp;
		glp_smcp parm;
		SWIGTYPE_p_int ind;
		SWIGTYPE_p_double val, val0;
		int numberOfFlows = sources.size();
		int numberOfStructuralParameters = _edges.size() * numberOfFlows * 2;	//Number of edges * number of flows * 2 (flows in 2 opposite directions)
		
		lp = GLPK.glp_create_prob();
		System.out.println("MCF Problem created");
		GLPK.glp_set_prob_name(lp, "MCF LP problem");
		
		//Define columns (structural parameters):
		int colNumber = 1;
		GLPK.glp_add_cols(lp, numberOfStructuralParameters);
		for(int i = 0; i < numberOfFlows; ++i)
			for(Edge e: _edges)
			{
				GLPK.glp_set_col_name(lp, colNumber, "F" + i + "("+e.getName()+")");
				GLPK.glp_set_col_bnds(lp, colNumber, GLPKConstants.GLP_UP, 0.0, e.getCapacity());
				colNumber += 1;
				GLPK.glp_set_col_name(lp, colNumber, "F" + i + "("+e.getInvName()+")");
				GLPK.glp_set_col_bnds(lp, colNumber, GLPKConstants.GLP_UP, 0.0, 0.0);
				colNumber += 1;
			}
		
		ind = GLPK.new_intArray(1 + numberOfStructuralParameters);								//Init indices
		for(int j = 0; j < numberOfStructuralParameters + 1; ++j)
			GLPK.intArray_setitem(ind, j, j);

		val = GLPK.new_doubleArray(1 + numberOfStructuralParameters);
		
		//Create capacity constraints for present edges:
		int rowNumber = 1;
		int edgeCounter = 1;
		GLPK.glp_add_rows(lp, _edges.size() * 2 + numberOfFlows*_edges.size() + numberOfFlows*(_vertices.size()-2) + sinks.size()*2 + sources.size()*2);
		for(Edge e: _edges)
		{
			GLPK.glp_set_row_name(lp, rowNumber, "cap(" + e.getName() + ")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_UP, 0.0, e.getCapacity());
			GLPK.glp_set_row_name(lp, rowNumber+1, "cap(" + e.getInvName() + ")");					//For inverse edge
			GLPK.glp_set_row_bnds(lp, rowNumber+1, GLPKConstants.GLP_UP, 0.0, 0.0);					//For inverse edge

			val0 = GLPK.new_doubleArray(1 + numberOfStructuralParameters);
			for(int j = 0; j < numberOfStructuralParameters + 1; ++j)								//Init values with 0
			{
				GLPK.doubleArray_setitem(val, j, 0);
				GLPK.doubleArray_setitem(val0, j, 0);
			}
			for(int j = 0; j < numberOfFlows; ++j)													//Sum of every flow along the edge
			{
				GLPK.doubleArray_setitem(val, (2*(edgeCounter - 1) + 1) + j*numberOfStructuralParameters/numberOfFlows, 1.0);
				GLPK.doubleArray_setitem(val0,(2*edgeCounter) + j*numberOfStructuralParameters/numberOfFlows, 1.0);
			}
			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			GLPK.glp_set_mat_row(lp, rowNumber+1, numberOfStructuralParameters, ind, val0);
			GLPK.delete_doubleArray(val0);
			edgeCounter += 1;
			rowNumber += 2;
		}

		//Set flow conservation constraints per each edge:
		for(int i = 0; i < numberOfFlows; ++i)
		{
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				GLPK.glp_set_row_name(lp, rowNumber, "y(" + e.getName() + ")");
				GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, 0.0, 0.0);
				
				for(int j = 0; j < numberOfStructuralParameters; ++j)
					GLPK.doubleArray_setitem(val, j, 0.0);
				
				GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + i*numberOfStructuralParameters/numberOfFlows, 1.0);
				GLPK.doubleArray_setitem(val, 2*edgeCounter + i*numberOfStructuralParameters/numberOfFlows, 1.0);
				GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
				edgeCounter += 1;
				rowNumber += 1;
			}
		}
		
		//Set more flow conservation constraints:
		for(int i = 0; i < numberOfFlows; ++i)
		{
			for(Vertex v: _vertices)
				if( !(sources.get(i).equals(v) || sinks.get(i).equals(v)))		//If not source or sink for this flow
				{
					GLPK.glp_set_row_name(lp, rowNumber, "fc_"+i+"("+ v.getID() +")");
					GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, 0.0, 0.0);
					
					for(int j = 0; j < numberOfStructuralParameters; ++j)
						GLPK.doubleArray_setitem(val, j, 0.0);

					edgeCounter = 1;
					for(Edge e: _edges)
					{
						if(e.getSource().equals(v))
							GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + i*numberOfStructuralParameters/numberOfFlows, 1.0);

						if(e.getSink().equals(v))
							GLPK.doubleArray_setitem(val, 2*edgeCounter + i*numberOfStructuralParameters/numberOfFlows, 1.0);
						edgeCounter+=1;
					}
					GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
					rowNumber += 1;
				}
		}

		//Set demand satisfaction constraints:
		int idx = 0;
		for(Vertex s: sources)
		{
			GLPK.glp_set_row_name(lp, rowNumber, "ds("+s.getID()+")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, demands[idx], demands[idx]);

			for(int j = 0; j < numberOfStructuralParameters; ++j)
				GLPK.doubleArray_setitem(val, j, 0.0);
			
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				if(e.getSource().equals(s))
					GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + idx*numberOfStructuralParameters/numberOfFlows, 1.0);
				edgeCounter += 1;
			}
			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			idx += 1;
			rowNumber += 1;
		}
		
		idx = 0;
		for(Vertex s: sources)
		{
			GLPK.glp_set_row_name(lp, rowNumber, "dsNoLoops("+s.getID()+")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, 0, 0);		//This constraint is to avoid the loops with the source vertices

			for(int j = 0; j < numberOfStructuralParameters; ++j)
				GLPK.doubleArray_setitem(val, j, 0.0);
			
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				if(e.getSink().equals(s))
					GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + idx*numberOfStructuralParameters/numberOfFlows, 1.0);
				edgeCounter += 1;
			}
			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			idx += 1;
			rowNumber += 1;
		}
		
		//Set demand satisfaction constraints:
		idx = 0;
		for(Vertex t: sinks)
		{
			GLPK.glp_set_row_name(lp, rowNumber, "ds("+t.getID()+")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, demands[idx], demands[idx]);

			for(int j = 0; j < numberOfStructuralParameters; ++j)
				GLPK.doubleArray_setitem(val, j, 0.0);
					
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				if(e.getSink().equals(t))
					GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + idx*numberOfStructuralParameters/numberOfFlows, 1.0);
				edgeCounter += 1;
			}
			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			idx += 1;
			rowNumber += 1;
		}
		
		idx = 0;
		for(Vertex t: sinks)
		{
			GLPK.glp_set_row_name(lp, rowNumber, "dsNoLoops("+t.getID()+")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, 0.0, 0.0);		//This constraint is to avoid the loops with the sink vertices

			for(int j = 0; j < numberOfStructuralParameters; ++j)
				GLPK.doubleArray_setitem(val, j, 0.0);

			edgeCounter = 1;
			for(Edge e: _edges)
			{
				if(e.getSource().equals(t))
					GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + idx*numberOfStructuralParameters/numberOfFlows, 1.0);
				edgeCounter += 1;
			}
			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			idx += 1;
			rowNumber += 1;
		}
		
		GLPK.glp_set_obj_name(lp, "Obj");
		GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
		for(int i = 0; i < numberOfStructuralParameters; ++i)			//Minimize 0
			GLPK.glp_set_obj_coef(lp, i, 0.0);
		
		parm = new glp_smcp();
		GLPK.glp_init_smcp(parm);
		int ret = GLPK.glp_simplex(lp, parm);
		if(ret == 0)
		{
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				for(int j = 0; j < numberOfFlows; ++j)
					e.setFLow(j, GLPK.glp_get_col_prim(lp, 2*(edgeCounter-1) + 1 + j*numberOfStructuralParameters/numberOfFlows));

				edgeCounter += 1;
			}
		}

		//Remove problem
		GLPK.glp_delete_prob(lp);
		GLPK.delete_intArray(ind);
		GLPK.delete_doubleArray(val);
	}*/

	/*
	 * The method solves the Multicommodity Flow problem on the given graph. Current implementation reduces the problem
	 * to the LP problem allowing by this fractional flows.
	 * @param sources - the list of source vertices
	 * @param sinks - the list of the corresponding sinks
	 * @param demands - demands of each source-sink pair
	 */
	/*public void MCFMinFlowTimeObj(List<Vertex> sources, List<Vertex> sinks, double[] demands)
	{
		if(sources.size() != sinks.size()) 
			throw new RuntimeException("The number of sources not equal to the number of sinks:"+sources.size()+"/"+sinks.size());

		if(_edges.size() <= 0)
			constructListOfEdges(demands.length);

		//Initialize the problem:
		glp_prob lp;
		glp_smcp parm;
		SWIGTYPE_p_int ind;
		SWIGTYPE_p_double val;
		int numberOfFlows = sources.size();
		int numberOfStructuralParameters = _edges.size() * numberOfFlows * 2 + 1;//Number of edges * number of flows * 2  + 1 (flows in 2 opposite directions different in their signs)
		
		lp = GLPK.glp_create_prob();
		System.out.println("MCF Problem with Flow Time Objective created");
		GLPK.glp_set_prob_name(lp, "MCF problem with Flow Time Objective (LP problem)");
		
		//Define columns (structural parameters):
		int colNumber = 1;
		GLPK.glp_add_cols(lp, numberOfStructuralParameters);
		for(int i = 0; i < numberOfFlows; ++i)
			for(Edge e: _edges)
			{
				GLPK.glp_set_col_name(lp, colNumber, "F" + i + "("+e.getName()+")");
				GLPK.glp_set_col_bnds(lp, colNumber, GLPKConstants.GLP_UP, 0.0, e.getCapacity());
				colNumber += 1;
				GLPK.glp_set_col_name(lp, colNumber, "F" + i + "("+e.getInvName()+")");
				GLPK.glp_set_col_bnds(lp, colNumber, GLPKConstants.GLP_UP, 0.0, 0.0);
				colNumber += 1;
			}
		GLPK.glp_set_col_name(lp, colNumber, "t");
		GLPK.glp_set_col_bnds(lp, colNumber, GLPKConstants.GLP_FR, 0.0, 0.0);
		
		ind = GLPK.new_intArray(1 + numberOfStructuralParameters);								//Init indices
		for(int j = 0; j < numberOfStructuralParameters + 1; ++j)
			GLPK.intArray_setitem(ind, j, j);

		val = GLPK.new_doubleArray(1 + numberOfStructuralParameters);							//Create matrix row

		//Create capacity constraints for present edges f(u,v) <= c(u,v), f(v, u) <= 0: //sum{i=1:numberOfCommodities}{f_i(u,v)} < c(u,v):
		int rowNumber = 1;
		int edgeCounter = 1;
		GLPK.glp_add_rows(lp, _edges.size() + numberOfFlows*_edges.size() + numberOfFlows*(_vertices.size()-2) + sinks.size() + sources.size()  + sources.size());
		//                     capacity constr.     capacity constr.                flow conservation constr         obj. constr.   dem. sat. constr.    no loops
		for(Edge e: _edges)
		{
			GLPK.glp_set_row_name(lp, rowNumber, "cap(" + e.getName() + ")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_UP, 0.0, e.getCapacity());

			for(int j = 0; j < numberOfStructuralParameters + 1; ++j)								//Init values with 0
				GLPK.doubleArray_setitem(val, j, 0);

			for(int j = 0; j < numberOfFlows; ++j)													//Sum of every flow along the edge
				GLPK.doubleArray_setitem(val, (2*(edgeCounter - 1) + 1) + j*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);

			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			edgeCounter += 1;
			rowNumber += 1;
		}

		//Set flow conservation constraints per each edge - for any edge (u,v) f(u,v) = - f(v, u)
		for(int i = 0; i < numberOfFlows; ++i)
		{
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				GLPK.glp_set_row_name(lp, rowNumber, "y(" + e.getName() + ")");
				GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, 0.0, 0.0);
				
				for(int j = 0; j < numberOfStructuralParameters+1; ++j)
					GLPK.doubleArray_setitem(val, j, 0.0);
				
				GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + i*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);
				GLPK.doubleArray_setitem(val, 2*edgeCounter + i*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);
				GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
				edgeCounter += 1;
				rowNumber += 1;
			}
		}
		
		//Set more flow conservation constraints - for any commodity i for any vertex v from V\{s_i, t_i} sum{vertex w}{f_i(v,w)} = 0:
		for(int i = 0; i < numberOfFlows; ++i)
		{
			for(Vertex v: _vertices)
				if( !(sources.get(i).equals(v) || sinks.get(i).equals(v)))		//If not source or sink for this flow
				{
					GLPK.glp_set_row_name(lp, rowNumber, "fc_"+i+"("+ v.getID() +")");
					GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, 0.0, 0.0);
					
					for(int j = 0; j < numberOfStructuralParameters+1; ++j)
						GLPK.doubleArray_setitem(val, j, 0.0);

					edgeCounter = 1;
					for(Edge e: _edges)
					{
						if(e.getSource().equals(v))
							GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + i*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);

						if(e.getSink().equals(v))
							GLPK.doubleArray_setitem(val, 2*edgeCounter + i*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);
						edgeCounter+=1;
					}
					GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
					rowNumber += 1;
				}
		}

		//Set objective constraint:
		int idx = 0;
		for(Vertex s: sources)
		{
			GLPK.glp_set_row_name(lp, rowNumber, "obj("+s.getID()+")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_LO, 0.0, 0.0);

			for(int j = 0; j < numberOfStructuralParameters+1; ++j)
				GLPK.doubleArray_setitem(val, j, 0.0);
			
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				if(e.getSource().equals(s))
					GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + idx*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);
				edgeCounter += 1;
			}
			GLPK.doubleArray_setitem(val, numberOfStructuralParameters, (-1)*demands[idx] );

			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			idx += 1;
			rowNumber += 1;
		}
		
		//Set demand satisfaction constraints
		idx = 0;
		for(idx = 0; idx < sources.size(); ++idx)
		{
			GLPK.glp_set_row_name(lp, rowNumber, "ds("+sources.get(idx).getID()+")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, 0.0, 0.0);
			
			for(int j = 0; j < numberOfStructuralParameters+1; ++j)
				GLPK.doubleArray_setitem(val, j, 0.0);

			edgeCounter = 1;
			for(Edge e: _edges)
			{
				if(e.getSource().equals(sources.get(idx)) && e.getSink().equals(sinks.get(idx)) )
				{
					edgeCounter += 1;
					continue;
				}
				else if(e.getSource().equals(sources.get(idx)) )
					GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + idx*(numberOfStructuralParameters - 1)/numberOfFlows, 1.0);
				else if( e.getSink().equals(sinks.get(idx)))
					GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + idx*(numberOfStructuralParameters - 1)/numberOfFlows, -1.0);

				edgeCounter += 1;
			}

			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			idx += 1;
			rowNumber += 1;
		}
		
		idx = 0;
		for(Vertex s: sources)
		{
			GLPK.glp_set_row_name(lp, rowNumber, "dsNoLoops("+s.getID()+")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, 0, 0);		//This constraint is to avoid the loops with the source vertices

			for(int j = 0; j < numberOfStructuralParameters+1; ++j)
				GLPK.doubleArray_setitem(val, j, 0.0);
			
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				if(e.getSink().equals(s))
					GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + idx*(numberOfStructuralParameters - 1)/numberOfFlows, 1.0);
				edgeCounter += 1;
			}
			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			idx += 1;
			rowNumber += 1;
		}
		
		GLPK.glp_set_obj_name(lp, "z");
		GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
		for(int i = 0; i < numberOfStructuralParameters; ++i)
			GLPK.glp_set_obj_coef(lp, i, 0.0);
		GLPK.glp_set_obj_coef(lp, numberOfStructuralParameters, 1.0);
		
		parm = new glp_smcp();
		GLPK.glp_init_smcp(parm);
		int ret = GLPK.glp_simplex(lp, parm);
		//int ret = GLPK.glp_interior(lp, null);
		
		System.out.println("MOOR: " + ret);
		if(ret == 0)
		{
			System.out.println("Z = " + GLPK.glp_get_obj_val(lp));
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				for(int j = 0; j < numberOfFlows; ++j)
					e.setFLow(j, GLPK.glp_get_col_prim(lp, 2*(edgeCounter-1) + 1 + j*(numberOfStructuralParameters-1)/numberOfFlows));

				edgeCounter += 1;
			}
		}
		
		//Remove problem
		GLPK.glp_delete_prob(lp);
		GLPK.delete_intArray(ind);
		GLPK.delete_doubleArray(val);
		
		//Post process
		normalizeFlows();
	}*/
	
	/*
	 * The method solves the Multicommodity Flow problem on the given graph. Current implementation reduces the problem
	 * to the LP problem allowing by this fractional flows.
	 * @param sources - the list of source vertices
	 * @param sinks - the list of the corresponding sinks
	 * @param demands - demands of each source-sink pair
	 */
	/*public void MCFMinFlowTimeObjNonDirected(List<Vertex> sources, List<Vertex> sinks, double[] demands)
	{
		if(sources.size() != sinks.size()) 
			throw new RuntimeException("The number of sources not equal to the number of sinks:"+sources.size()+"/"+sinks.size());

		if(_edges.size() <= 0)
			constructListOfEdges(demands.length);

		//Initialize the problem:
		glp_prob lp;
		glp_smcp parm;
		SWIGTYPE_p_int ind;
		SWIGTYPE_p_double val;
		int numberOfFlows = sources.size();
		int numberOfStructuralParameters = _edges.size() * numberOfFlows * 2 + 1;//Number of edges * number of flows * 2  + 1 (flows in 2 opposite directions different in their signs)
		
		lp = GLPK.glp_create_prob();
		System.out.println("MCF Problem with Flow Time Objective created");
		GLPK.glp_set_prob_name(lp, "MCF problem with Flow Time Objective (LP problem)");
		
		//Define columns (structural parameters):
		int colNumber = 1;
		GLPK.glp_add_cols(lp, numberOfStructuralParameters);
		for(int i = 0; i < numberOfFlows; ++i)
			for(Edge e: _edges)
			{
				GLPK.glp_set_col_name(lp, colNumber, "F" + i + "("+e.getName()+")");
				GLPK.glp_set_col_bnds(lp, colNumber, GLPKConstants.GLP_UP, 0.0, e.getCapacity());
				colNumber += 1;
				GLPK.glp_set_col_name(lp, colNumber, "F" + i + "("+e.getInvName()+")");
				GLPK.glp_set_col_bnds(lp, colNumber, GLPKConstants.GLP_UP, 0.0, 0.0);
				colNumber += 1;
			}
		GLPK.glp_set_col_name(lp, colNumber, "t");
		GLPK.glp_set_col_bnds(lp, colNumber, GLPKConstants.GLP_FR, 0.0, 0.0);
		
		ind = GLPK.new_intArray(1 + numberOfStructuralParameters);								//Init indices
		for(int j = 0; j < numberOfStructuralParameters + 1; ++j)
			GLPK.intArray_setitem(ind, j, j);

		val = GLPK.new_doubleArray(1 + numberOfStructuralParameters);							//Create matrix row

		//Create capacity constraints for present edges f(u,v) <= c(u,v), f(v, u) <= 0: //sum{i=1:numberOfCommodities}{f_i(u,v)} < c(u,v):
		int rowNumber = 1;
		int edgeCounter = 1;
		GLPK.glp_add_rows(lp, _edges.size()/2 + numberOfFlows*_edges.size() + numberOfFlows*(_vertices.size()-2) + sinks.size() + sources.size()  + sources.size());
		//                     capacity constr.     capacity constr.                flow conservation constr         obj. constr.   dem. sat. constr.    no loops
		List<Edge> visited = new LinkedList<Edge>();
		for(Edge e : _edges)
		{
			if( visited.contains(e) )
			{
				edgeCounter += 1;
				continue;
			}
			Edge invE = new Edge(e.getSink(), e.getSource(), 0);									//The reverse edge
			visited.add(invE);																		//Do not consider this edge on the following iterations
			
			int invEdgeCounter = 1;
			for(Edge inverseE : _edges)
			{
				if( inverseE.equals(invE) )
					break;
				invEdgeCounter++;
			}
			GLPK.glp_set_row_name(lp, rowNumber, "CAP(" + e.getName() + ")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_UP, 0.0, e.getCapacity());
			
			for(int j = 0; j < numberOfStructuralParameters + 1; ++j)
				GLPK.doubleArray_setitem(val, j, 0);
			
			for(int j = 0; j < numberOfFlows; ++j)
			{
				GLPK.doubleArray_setitem(val, 2*(edgeCounter - 1) + 1 + j*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);
				GLPK.doubleArray_setitem(val, 2*(invEdgeCounter-1)+ 1 + j*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);
			}
				
			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			edgeCounter += 1;
			rowNumber += 1;
		}

		//Set flow conservation constraints per each edge - for any edge (u,v) f(u,v) = - f(v, u)
		for(int i = 0; i < numberOfFlows; ++i)
		{
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				GLPK.glp_set_row_name(lp, rowNumber, "y(" + e.getName() + ")");
				GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, 0.0, 0.0);
				
				for(int j = 0; j < numberOfStructuralParameters+1; ++j)
					GLPK.doubleArray_setitem(val, j, 0.0);
				
				GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + i*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);
				GLPK.doubleArray_setitem(val, 2*edgeCounter + i*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);
				GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
				edgeCounter += 1;
				rowNumber += 1;
			}
		}
		
		//Set more flow conservation constraints - for any commodity i for any vertex v from V\{s_i, t_i} sum{vertex w}{f_i(v,w)} = 0:
		for(int i = 0; i < numberOfFlows; ++i)
		{
			for(Vertex v: _vertices)
				if( !(sources.get(i).equals(v) || sinks.get(i).equals(v)))		//If not source or sink for this flow
				{
					GLPK.glp_set_row_name(lp, rowNumber, "fc_"+i+"("+ v.getID() +")");
					GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, 0.0, 0.0);
					
					for(int j = 0; j < numberOfStructuralParameters+1; ++j)
						GLPK.doubleArray_setitem(val, j, 0.0);

					edgeCounter = 1;
					for(Edge e: _edges)
					{
						if(e.getSource().equals(v))
							GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + i*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);

						if(e.getSink().equals(v))
							GLPK.doubleArray_setitem(val, 2*edgeCounter + i*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);
						edgeCounter+=1;
					}
					GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
					rowNumber += 1;
				}
		}

		//Set objective constraint:
		int idx = 0;
		for(Vertex s: sources)
		{
			GLPK.glp_set_row_name(lp, rowNumber, "obj("+s.getID()+")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_LO, 0.0, 0.0);

			for(int j = 0; j < numberOfStructuralParameters+1; ++j)
				GLPK.doubleArray_setitem(val, j, 0.0);
			
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				if(e.getSource().equals(s))
					GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + idx*(numberOfStructuralParameters-1)/numberOfFlows, 1.0);
				edgeCounter += 1;
			}
			GLPK.doubleArray_setitem(val, numberOfStructuralParameters, (-1)*demands[idx] );

			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			idx += 1;
			rowNumber += 1;
		}
		
		//Set demand satisfaction constraints
		idx = 0;
		for(idx = 0; idx < sources.size(); ++idx)
		{
			GLPK.glp_set_row_name(lp, rowNumber, "ds("+sources.get(idx).getID()+")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, 0.0, 0.0);
			
			for(int j = 0; j < numberOfStructuralParameters+1; ++j)
				GLPK.doubleArray_setitem(val, j, 0.0);

			edgeCounter = 1;
			for(Edge e: _edges)
			{
				if(e.getSource().equals(sources.get(idx)) && e.getSink().equals(sinks.get(idx)) )
				{
					edgeCounter += 1;
					continue;
				}
				else if(e.getSource().equals(sources.get(idx)) )
					GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + idx*(numberOfStructuralParameters - 1)/numberOfFlows, 1.0);
				else if( e.getSink().equals(sinks.get(idx)))
					GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + idx*(numberOfStructuralParameters - 1)/numberOfFlows, -1.0);

				edgeCounter += 1;
			}

			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			idx += 1;
			rowNumber += 1;
		}
		
		idx = 0;
		for(Vertex s: sources)
		{
			GLPK.glp_set_row_name(lp, rowNumber, "dsNoLoops("+s.getID()+")");
			GLPK.glp_set_row_bnds(lp, rowNumber, GLPKConstants.GLP_FX, 0, 0);		//This constraint is to avoid the loops with the source vertices

			for(int j = 0; j < numberOfStructuralParameters+1; ++j)
				GLPK.doubleArray_setitem(val, j, 0.0);
			
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				if(e.getSink().equals(s))
					GLPK.doubleArray_setitem(val, 2*(edgeCounter-1) + 1 + idx*(numberOfStructuralParameters - 1)/numberOfFlows, 1.0);
				edgeCounter += 1;
			}
			GLPK.glp_set_mat_row(lp, rowNumber, numberOfStructuralParameters, ind, val);
			idx += 1;
			rowNumber += 1;
		}
		
		GLPK.glp_set_obj_name(lp, "z");
		GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MAX);
		for(int i = 0; i < numberOfStructuralParameters; ++i)
			GLPK.glp_set_obj_coef(lp, i, 0.0);
		GLPK.glp_set_obj_coef(lp, numberOfStructuralParameters, 1.0);
		
		parm = new glp_smcp();
		GLPK.glp_init_smcp(parm);
		int ret = GLPK.glp_simplex(lp, parm);
		//int ret = GLPK.glp_interior(lp, null);
		
		System.out.println("MOOR: " + ret);
		if(ret == 0)
		{
			System.out.println("Z = " + GLPK.glp_get_obj_val(lp));
			edgeCounter = 1;
			for(Edge e: _edges)
			{
				for(int j = 0; j < numberOfFlows; ++j)
					e.setFLow(j, GLPK.glp_get_col_prim(lp, 2*(edgeCounter-1) + 1 + j*(numberOfStructuralParameters-1)/numberOfFlows));

				edgeCounter += 1;
			}
		}
		
		//Remove problem
		GLPK.glp_delete_prob(lp);
		GLPK.delete_intArray(ind);
		GLPK.delete_doubleArray(val);
		
		//Post process
		normalizeFlows();
	}*/
	
	/*
	 * The method constructs flow tables from a solution of MCF problem. 
	 * @param numberOfFlows - the number of flows in the graph
	 */
	public void constructFlowTables(int numberOfFlows)
	{
		_flowTables = new double[numberOfFlows][_vertices.size()][_vertices.size()];
		//Init flow tables with 0
		for(int i = 0; i < numberOfFlows; ++i)
			for(int j = 0; j < _vertices.size(); ++j)
				for(int k = 0; k < _vertices.size(); ++k)
					_flowTables[i][j][k] = 0;
		//Fill flow tables
		for(Edge e : _edges)
			for(int i = 0; i < numberOfFlows; ++i)
				_flowTables[i][e.getSource().getID()-1][e.getSink().getID()-1] = e.getFlow(i);
	}
	
	/*
	 * The method constructs and returns the list of edges of the graph
	 * @param numberOfFlows - the maximum possible number of flows along the edge
	 */
	private void constructListOfEdges(int numFlowsPerEdge)
	{
		for(Vertex v : _vertices)
			for(VertexCell vc : _adjacencyLists.get(v.getID()-1) )
				_edges.add(new Edge(v, vc._v, vc._w, numFlowsPerEdge));
	}
	
	/*
	 * The method is used as a post-process step of MCF solver. This method removes flow loops from the graph.
	 */
	private void normalizeFlows()
	{
		for(Edge edge : _edges)
			for(Edge otherEdge: _edges)
				if( otherEdge.getSink().equals(edge.getSource()) && otherEdge.getSource().equals(edge.getSink()))
					for(int i = 0; i < edge.getNumberOfFlows(); ++i)
						if(edge.getFlow(i) >= otherEdge.getFlow(i))
						{
							edge.setFLow(i, edge.getFlow(i) - otherEdge.getFlow(i));
							otherEdge.setFLow(i, 0);
						}
						else
						{
							otherEdge.setFLow(i, otherEdge.getFlow(i) - edge.getFlow(i));
							edge.setFLow(i, 0);
						}
	}
	
	protected List<List<VertexCell>> _adjacencyLists;			//Adjacency lists for each vertex. _adjacencyLists[i-1] - for the i-th vertex
	private Vertex _source;										//The source used for the graph search algorithms
	protected List<Vertex> _vertices;							//A set of vertices of the graph
	private List<Edge> _edges;
	private double _radius;
	
	private double[][][] _flowTables;

	private static final int WHITE = 0;
	private static final int GREY  = 1;
	private static final int BLACK = 2;
}

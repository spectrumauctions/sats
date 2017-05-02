package ch.uzh.ifi.ce.mweiss.specval.model.cats.graphalgorithms;

public class VertexCell implements KeyInterface {
	
	/*
	 * A simple constructor
	 */
	public VertexCell()
	{
		_w = 0;
		_f = 0;
	}
	
	/*
	 * A simple constructor
	 */
	public VertexCell(Vertex v, double w)
	{
		_v = v;
		_w = w;
		_f = 0;
	}
	
	/*
	 * A simple constructor
	 */
	public VertexCell(Vertex v, double w, double f)
	{
		_v = v;
		_w = w;
		_f = f;
	}
	
	public VertexCell cloneIt() 
	{
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		VertexCell vc = new VertexCell(_v.cloneIt(), _w, _f);
		return vc;
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other)
	{
		VertexCell otherVertex = (VertexCell)other;
		if( this._v.equals(otherVertex._v))
			return true;
		else
			return false;
	}
	
	/*
	 * Conversion to String
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		String str = " {" + _v.getID() + "("+ _v.getAdjacencyListIndex() +")" + "/" + _w + " "+ _f + " || " + _v.getShortestPathEst(0) +"}";
		return str;
	}
	
	/*
	 * The method returns the key for this vertex. Key is the value used to store the vertexCell in 
	 * a priority queue (used for Dijkstra graph search)
	 * @see GraphPackage.KeyInterface#getKey()
	 */
	public double getKey(int idx)
	{
		return _v.getShortestPathEst(idx);
	}
	
	/*
	 * The method setup the key for this vertexCell. Key is the value used to store the vertexCell in 
	 * a priority queue (used for Dijkstra graph search)
	 * @see GraphPackage.KeyInterface#setKey(double)
	 */
	public void setKey(double key, int idx)
	{
		_v.setShortestPathEst(key, idx);
	}
	
	@Override
	public double getKey() 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setKey(double key) 
	{
		// TODO Auto-generated method stub
		
	}
	
	public Vertex _v;
	public double _w;				//Residual capacity
	public double _f;				//Flow for the edge (used e.g. in Ford-Fulkerson for the search of the maximum flow in the graph)
}

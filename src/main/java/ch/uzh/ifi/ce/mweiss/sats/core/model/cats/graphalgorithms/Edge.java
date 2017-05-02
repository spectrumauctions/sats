package ch.uzh.ifi.ce.mweiss.sats.core.model.cats.graphalgorithms;


public class Edge {
	
	/*
	 * A simple constructor for a directed edge
	 * @param source - the source vertex of the edge
	 * @param sink - the sink vertex of the edge
	 * @param numberOfFlows - the number of flows crossing this edge (specify 0 if not used)
	 */
	public Edge(Vertex source, Vertex sink, int numberOfFlows)
	{
		init(source, sink, 0.0, numberOfFlows);
	}
	
	/*
	 * A simple constructor for a directed edge
	 * @param source - the source vertex of the edge
	 * @param sink - the sink vertex of the edge
	 * @param capacity - edge capacity
	 * @param numberOfFlows - the number of flows crossing this edge (specify 0 if not used)
	 */
	public Edge(Vertex source, Vertex sink, double capacity, int numberOfFlows)
	{
		init(source, sink, capacity, numberOfFlows);
	}

	/*
	 * Init method
	 * @param source - the source vertex of the edge
	 * @param sink - the sink vertex of the edge
	 * @param capacity - edge capacity
	 * @param numberOfFlows - the number of flows crossing this edge (specify 0 if not used)
	 */
	public void init(Vertex source, Vertex sink, double capacity, int numberOfFlows)
	{
		_source = source;
		_sink = sink;
		_capacity = capacity;
		_flow = new double[numberOfFlows];
	}
	
	@Override
	public boolean equals(Object other)
	{
		Edge otherEdge = (Edge)other;
		if(_source == otherEdge.getSource() && _sink == otherEdge.getSink())
			return true;
		else
			return false;
	}
	
	/*
	 * Conversion to string
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String res = "";
		res += _source.getID() + " -(c="+ _capacity +")-> " + _sink.getID();
		for(int i = 0; i < _flow.length; ++i)
			res += " f" + i + "=" + _flow[i] + " ";
		return res;
	}
	
	/*
	 * The method returns a string name associated with this edge
	 * @return the name of the edge
	 */
	public String getName()
	{
		String res = "";
		res += _source.getID() + "," + _sink.getID();
		return res;
	}
	
	/*
	 * The method returns a string name associated with the inverse edge i.e.
	 * with the edge for which the sink is a source and a source is a sink
	 * @return name of the edge
	 */
	public String getInvName()
	{
		String res = "";
		res += _sink.getID() + "," + _source.getID();
		return res;
	}
	
	/*
	 * The method returns the capacity of the edge
	 * @return the capacity of the edge
	 */
	public double getCapacity()
	{
		return _capacity;
	}
	
	/*
	 * The method returns the source vertex of the edge
	 */
	public Vertex getSource()
	{
		return _source;
	}
	
	/*
	 * The methid returns the sink vertex of the edge
	 */
	public Vertex getSink()
	{
		return _sink;
	}
	
	/*
	 * The method sets the i-th flow along the edge
	 * @param i - the number of flow to be set
	 * @param f - the flow value
	 */
	public void setFLow(int i, double f)
	{
		_flow[i] = f;
	}
	
	public double getFlow(int i)
	{
		return _flow[i];
	}
	
	/*
	 * The method returns the number of flows crossing the edge
	 * @return the number of flows per edge
	 */
	public int getNumberOfFlows()
	{
		return _flow.length;
	}
	
	private Vertex _source;
	private Vertex _sink;
	private double _capacity;
	private double[] _flow;
}

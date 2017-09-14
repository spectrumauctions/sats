package org.spectrumauctions.sats.core.model.cats.graphalgorithms;

import java.util.LinkedList;
import java.util.List;

/**
 * A class for a vertex of the graph.
 *
 * @author Dmitry Moor
 */
public class Vertex {

    /**
     * Constructor
     *
     * @param id - identifier of the new vertex
     */
    public Vertex(int id) {
        _id = id;
        _adjListIdx = _id - 1;
        _color = 0;
        _shortestPathEst = new LinkedList<>();
        _shortestPathEst.add(0.0);
        _predecessor = new LinkedList<>();
        _predecessor.add(0);
        _childVertices = new LinkedList<>();
    }

    /**
     * The method returns a deep copy of the object.
     *
     * @return a deep copy of the class
     */
    public Vertex cloneIt() {
        Vertex vrt = new Vertex(_id);
        vrt.setAdjacencyListIndex(_adjListIdx);
        vrt.setColor(_color);
        //int i = 0;
        vrt.setPredecessor(0, 0);
        vrt.setShortestPathEst(0, 0);
        /*for( Integer p : _predecessor)
		{
			vrt.setPredecessor(p, i);
			vrt.setShortestPathEst(getShortestPathEst(i), i);
			i += 1;
		}*/
        return vrt;
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object other) {
        Vertex otherVertex = (Vertex) other;
        if (_id == otherVertex.getID())
            return true;
        else
            return false;
    }

    /**
     * The method returns ID of the vertex
     *
     * @return vertex id
     */
    public int getID() {
        return _id;
    }

    /**
     * The method returns the index of the associated adjacency list
     *
     * @return the index of the adjacency list associated with this vertex
     */
    public int getAdjacencyListIndex() {
        return _adjListIdx;
    }

    /**
     * The method returns the estimation for the shortest path from some source to this vertex
     *
     * @param i - the index of shortest path estimation (there may be several estimations)
     * @return the estimation
     */
    public double getShortestPathEst(int i) {
        return _shortestPathEst.get(i);
    }

    /**
     * The method returns the estimation for the shortest path from some source to this vertex
     *
     * @param i - the index of shortest path estimation (there may be several estimations)
     * @return the estimation
     */
    public double getShortestPathEst() {
        double minEst = Double.MAX_VALUE;
        for (Double val : _shortestPathEst)
            if (val.doubleValue() < minEst)
                minEst = val.doubleValue();
        return minEst;
    }

    /**
     * The method returns the predecessor of the current vertex in the shortest path tree from some source
     *
     * @param i - the index of the predecessor (there may be several predecessors - each for one tree)
     * @return the predecessor
     */
    public int getPredecessor(int i) {
        return _predecessor.get(i);
    }

    /**
     * The method returns the color of the vertex
     *
     * @return the color
     */
    public int getColor() {
        return _color;
    }

    /**
     * @param i index of the child
     * @return the child vertex
     */
    public Vertex getChildVertex(int i) {
        return _childVertices.get(i);
    }

    public int getNumberOfChilds() {
        return _childVertices.size();
    }

    /**
     * The method setup the index of the adjacency list associated with this vertex
     *
     * @param the index of the adjacency list of interest
     */
    public void setAdjacencyListIndex(int idx) {
        _adjListIdx = idx;
    }

    /**
     * The method set up the estimation for the shortest path from some source to this vertex
     *
     * @param d - the estimation
     */
    public void setShortestPathEst(double d, int i) {
        if (i > _shortestPathEst.size()) throw new RuntimeException("Wrong index");

        if (i == _shortestPathEst.size())
            _shortestPathEst.add(d);
        else
            _shortestPathEst.set(i, d);
    }

    /**
     * The method set up the predecessor of the current vertex in the shortest path tree from some source
     */
    public void setPredecessor(int p, int i) {
        if (i > _predecessor.size()) throw new RuntimeException("Wrong index");

        if (i == _predecessor.size())
            _predecessor.add(p);
        else
            _predecessor.set(i, p);
    }

    /**
     * The method sets the color for the vertex
     *
     * @param color - the value of color to be set
     */
    public void setColor(int color) {
        _color = color;
    }

    public void addChildVertex(Vertex child) {
        _childVertices.add(child);
    }

    public boolean isAggregative() {
        return _childVertices.size() > 0;
    }

    private int _id;                            //The id of the vertex
    private int _adjListIdx;
    private List<Double> _shortestPathEst;        //Shortest Path estimation
    private List<Integer> _predecessor;            //Predecessor of this vertex in the shortest tree
    private int _color;                            //The color field is used for BFS

    private List<Vertex> _childVertices;        //The vertex may aggregate several vertices TODO: inherit a new class for such vertices
}

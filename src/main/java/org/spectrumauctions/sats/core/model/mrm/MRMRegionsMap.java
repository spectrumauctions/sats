/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrm;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spectrumauctions.sats.core.util.random.GaussianDistributionRNG;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.graph.UnmodifiableGraph;
import org.jgrapht.graph.UnmodifiableUndirectedGraph;

import com.google.common.base.Preconditions;

/**
 * @author Michael Weiss
 *
 */
public class MRMRegionsMap implements Serializable {

    private static final long serialVersionUID = -7539511827334949347L;
    private final UnmodifiableUndirectedGraph<Region, DefaultEdge> adjacencyGraph;
    private transient FloydWarshallShortestPaths<Region, DefaultEdge> floyedWarshallDistances = null;


    public MRMRegionsMap(MRMWorldSetup worldStructure, RNGSupplier rngSupplier){
        UndirectedGraph<MRMWorldSetup.RegionSetup, DefaultEdge> graphStructure = 
                worldStructure.drawGraphStructure(rngSupplier.getUniformDistributionRNG());
        adjacencyGraph = makeGraph(graphStructure, rngSupplier.getGaussianDistributionRNG());
        
    }


    private UnmodifiableUndirectedGraph<Region, DefaultEdge> makeGraph(
            UndirectedGraph<MRMWorldSetup.RegionSetup, DefaultEdge> graphStructure,
            GaussianDistributionRNG rng) {
        SimpleGraph<Region, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Map<MRMWorldSetup.RegionSetup, Region> regions = new HashMap<>();
        int id_count = 0;
        for (MRMWorldSetup.RegionSetup regInfo : graphStructure.vertexSet()) {
            Region reg = new Region(id_count++, regInfo, rng);
            regions.put(regInfo, reg);
            graph.addVertex(reg);
        }
        for (DefaultEdge edge : graphStructure.edgeSet()) {
            Region source = regions.get(graphStructure.getEdgeSource(edge));
            Region target = regions.get(graphStructure.getEdgeTarget(edge));
            graph.addEdge(source, target);
        }
        return new UnmodifiableUndirectedGraph<>(graph);
    }
    
    
    private FloydWarshallShortestPaths<Region, DefaultEdge> getFloyedWarshallDistances(){
        if(floyedWarshallDistances == null){
            floyedWarshallDistances = new FloydWarshallShortestPaths<>(adjacencyGraph);
        }
        return floyedWarshallDistances;
    }
    
    /**
     * Returns the length of the longest shortest path in the adjacency graph between the specified region and any other region.
     */
    public int getLongestShortestPath(Region region){
        Preconditions.checkArgument(adjacencyGraph.containsVertex(region));
        List<GraphPath<Region, DefaultEdge>> shortestPaths = getFloyedWarshallDistances().getShortestPaths();
        int max = 0;
        for(GraphPath<Region, DefaultEdge> candidatePath : shortestPaths){
            int length = candidatePath.getEdgeList().size();
            if(length > max){
                max = length;
            }
        }
        return max;
    }
    
    public Set<Region> adjacentRegions(Region region) {
        if (!adjacencyGraph.containsVertex(region)) {
            throw new RuntimeException("Region not part of this map");
        } else {
            Set<Region> adjacentRegions = new HashSet<MRMRegionsMap.Region>();
            for (Region neighborCandidate : getRegions()) {
                if(adjacencyGraph.containsEdge(region, neighborCandidate)){
                    adjacentRegions.add(neighborCandidate);
                }
            }
            return adjacentRegions;
        }
    }

    /**
     * Creates a new, unmodifiable regions-graph, consistent with the adjacency graph of this map,
     * but only containing the specified set of regions as verteces.
     * Note: The Region Instances are not copied, hence, calling {@link Region#getDistance(Region)},
     * {@link Region#isAdjacent(Region)} and similar methods on the regions in the returned graph will return the values
     * w.r.t. the
     * original graph in the map.
     * 
     * @param regions
     * @return
     */
    protected UnmodifiableGraph<Region, DefaultEdge> getSubgraph(Set<Region> regions) {
        Subgraph<Region, DefaultEdge, UndirectedGraph<Region, DefaultEdge>> subgraph = new Subgraph<MRMRegionsMap.Region, DefaultEdge, UndirectedGraph<Region, DefaultEdge>>(
                adjacencyGraph, regions);
        return new UnmodifiableGraph<Region, DefaultEdge>(subgraph);
    }


    /**
     * Returns if two regions is adjacent, i.e., if they share a border.
     * 
     * @param region
     * @param otherRegion
     * @return
     */
    public boolean areAdjacent(Region region, Region otherRegion) {
        if (this.adjacencyGraph.containsEdge(otherRegion, region)
                || this.adjacencyGraph.containsEdge(region, otherRegion)) {
            return true;
        }
        return false;
    }

    /**
     * Calculates the distance between two regions in this map.<br>
     * <br>
     * 
     * Distance is defined as the length of the shortest path in a graph representation of this map,
     * where two regions share an edge if and only if they are adjacent. All edges have weight / lenght 1.
     * <br>
     * If no path was found, {@link #getNumberOfRegions()}-1 is returned.
     * @param regionOne
     * @param regionTwo
     * @return
     */
    public int getDistance(Region regionOne, Region regionTwo) {
        if(regionOne.equals(regionTwo)){
            return 0;
        }
        GraphPath<Region, DefaultEdge> shortestPath = getFloyedWarshallDistances().getShortestPath(regionOne, regionTwo);
        if(shortestPath == null){
            //No path found, return max distance
            return getNumberOfRegions()-1;
        }
        List<DefaultEdge> path = shortestPath.getEdgeList();
        if(path == null){
            //No path found, return max distance
            return getNumberOfRegions()-1;
        }
        return path.size();
    }


    public Set<Region> getRegions() {
        return Collections.unmodifiableSet(adjacencyGraph.vertexSet());
    }
    
    public int getNumberOfRegions(){
        return adjacencyGraph.vertexSet().size();
    }
    
    public Region getRegion(int id){
        for(Region region : getRegions()){
            if(region.getId() == id){
                return region;
            }
        }
        throw new IllegalArgumentException("ID not known");
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((adjacencyGraph.vertexSet() == null) ? 0 : adjacencyGraph.vertexSet().hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MRMRegionsMap other = (MRMRegionsMap) obj;
        if (adjacencyGraph == null) {
            if (other.adjacencyGraph != null)
                return false;
        } else if (!adjacencyGraph.vertexSet().equals(other.adjacencyGraph.vertexSet()))
            return false;
        return true;
    }


    public static class Region implements Serializable {

        private static final long serialVersionUID = 6138501456844925185L;

        private final int id;
        private final int population;
        private final String note;

        private Region(int id, MRMWorldSetup.RegionSetup setup, GaussianDistributionRNG rng){
            super();
            this.id = id;
            this.population = setup.drawPopulation(rng);
            this.note = setup.getNotice();
        }
        

        public int getId() {
            return id;
        }

        public int getPopulation() {
            return population;
        }

        /**
         * @deprecated Use {@link #getNote()} instead
         */
        @Deprecated
        public String getNotice() {
            return getNote();
        }


        public String getNote() {
            return note;
        }


        @Override
        public String toString(){
            StringBuilder builder =  new StringBuilder()
                    .append(String.valueOf(id));
            if(note != null && !note.equals("")){
                builder.append(" (")
                .append(note)
                .append(")")
                .toString();
            }
            return builder.toString();
        }


        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            result = prime * result + ((note == null) ? 0 : note.hashCode());
            result = prime * result + population;
            return result;
        }


        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Region other = (Region) obj;
            if (id != other.id)
                return false;
            if (note == null) {
                if (other.note != null)
                    return false;
            } else if (!note.equals(other.note))
                return false;
            if (population != other.population)
                return false;
            return true;
        }
        

    }


}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import com.google.common.base.Preconditions;
import org.jgrapht.GraphPath;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.graph.*;
import org.spectrumauctions.sats.core.util.random.GaussianDistributionRNG;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.io.Serializable;
import java.util.*;

/**
 * @author Michael Weiss
 *
 */
public class MRVMRegionsMap implements Serializable {

    private static final long serialVersionUID = -7539511827334949347L;
    private final Graph<Region, DefaultEdge> adjacencyGraph;
    private transient FloydWarshallShortestPaths<Region, DefaultEdge> floyedWarshallDistances = null;


    public MRVMRegionsMap(MRVMWorldSetup worldStructure, RNGSupplier rngSupplier) {
        Graph<MRVMWorldSetup.RegionSetup, DefaultEdge> graphStructure =
                worldStructure.drawGraphStructure(rngSupplier.getUniformDistributionRNG());
        adjacencyGraph = makeGraph(graphStructure, rngSupplier.getGaussianDistributionRNG());

    }


    private Graph<Region, DefaultEdge> makeGraph(
            Graph<MRVMWorldSetup.RegionSetup, DefaultEdge> graphStructure,
            GaussianDistributionRNG rng) {
        SimpleGraph<Region, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Map<MRVMWorldSetup.RegionSetup, Region> regions = new HashMap<>();
        int id_count = 0;
        for (MRVMWorldSetup.RegionSetup regInfo : graphStructure.vertexSet()) {
            Region reg = new Region(id_count++, regInfo, rng);
            regions.put(regInfo, reg);
            graph.addVertex(reg);
        }
        for (DefaultEdge edge : graphStructure.edgeSet()) {
            Region source = regions.get(graphStructure.getEdgeSource(edge));
            Region target = regions.get(graphStructure.getEdgeTarget(edge));
            graph.addEdge(source, target);
        }
        return graph;
    }


    private FloydWarshallShortestPaths<Region, DefaultEdge> getFloyedWarshallDistances() {
        if (floyedWarshallDistances == null) {
            floyedWarshallDistances = new FloydWarshallShortestPaths<>(adjacencyGraph);
        }
        return floyedWarshallDistances;
    }

    /**
     * Returns the length of the longest shortest path in the adjacency graph between the specified region and any other region.
     */
    public int getLongestShortestPath(Region region) {
        Preconditions.checkArgument(adjacencyGraph.containsVertex(region));
        ShortestPathAlgorithm.SingleSourcePaths<Region, DefaultEdge> shortestPaths = getFloyedWarshallDistances().getPaths(region);
        int max = 0;
        for (Region vertex : adjacencyGraph.vertexSet()) {
            int length = shortestPaths.getPath(vertex).getLength();
            if (length > max) {
                max = length;
            }
        }
        return max;
    }

    public Set<Region> adjacentRegions(Region region) {
        if (!adjacencyGraph.containsVertex(region)) {
            throw new RuntimeException("Region not part of this map");
        } else {
            Set<Region> adjacentRegions = new HashSet<>();
            for (Region neighborCandidate : getRegions()) {
                if (adjacencyGraph.containsEdge(region, neighborCandidate)) {
                    adjacentRegions.add(neighborCandidate);
                }
            }
            return adjacentRegions;
        }
    }

//    /**
//     * Creates a new, unmodifiable regions-graph, consistent with the adjacency graph of this map,
//     * but only containing the specified set of regions as vertices.
//     * Note: The Region Instances are not copied, hence, calling {@link #getDistance(Region, Region)},
//     * {@link #adjacentRegions(Region)} and similar methods on the regions in the returned graph will return the values
//     * w.r.t. the original graph in the map.
//     */
//    protected UnmodifiableGraph<Region, DefaultEdge> getSubgraph(Set<Region> regions) {
//        Subgraph<Region, DefaultEdge, UndirectedGraph<Region, DefaultEdge>> subgraph = new Subgraph<>(
//                adjacencyGraph, regions);
//        return new UnmodifiableGraph<>(subgraph);
//    }


    /**
     * @return true if two regions is adjacent, i.e., if they share a border.
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
     */
    public int getDistance(Region regionOne, Region regionTwo) {
        if (regionOne.equals(regionTwo)) {
            return 0;
        }
        GraphPath<Region, DefaultEdge> shortestPath = getFloyedWarshallDistances().getPath(regionOne, regionTwo);
        if (shortestPath == null) {
            //No path found, return max distance
            return getNumberOfRegions() - 1;
        }
        List<DefaultEdge> path = shortestPath.getEdgeList();
        if (path == null) {
            //No path found, return max distance
            return getNumberOfRegions() - 1;
        }
        return path.size();
    }


    public Set<Region> getRegions() {
        return Collections.unmodifiableSet(adjacencyGraph.vertexSet());
    }

    public int getNumberOfRegions() {
        return adjacencyGraph.vertexSet().size();
    }

    public Region getRegion(int id) {
        for (Region region : getRegions()) {
            if (region.getId() == id) {
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
        MRVMRegionsMap other = (MRVMRegionsMap) obj;
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

        private Region(int id, MRVMWorldSetup.RegionSetup setup, GaussianDistributionRNG rng) {
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
        public String toString() {
            StringBuilder builder = new StringBuilder()
                    .append(String.valueOf(id));
            if (note != null && !note.equals("")) {
                builder.append(" (")
                        .append(note)
                        .append(")");
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

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util.file.gson;

import com.google.gson.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Michael Weiss
 *
 */
public class GraphAdapter implements JsonSerializer<Graph<MRVMRegionsMap.Region, DefaultEdge>>, JsonDeserializer<Graph<MRVMRegionsMap.Region, DefaultEdge>> {

    /* (non-Javadoc)
     * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
     */
    @Override
    public JsonElement serialize(Graph<MRVMRegionsMap.Region, DefaultEdge> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray result = new JsonArray();
        for (MRVMRegionsMap.Region region : src.vertexSet()) {
            SerializedRegion r = new SerializedRegion(region);
            for (MRVMRegionsMap.Region neighborCandidate : src.vertexSet()) {
                if (src.containsEdge(region, neighborCandidate)) {
                    r.addAdjacentRegion(neighborCandidate.getId());
                }
            }
            JsonElement serializedRegion = context.serialize(r);
            result.add(serializedRegion);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.google.gson.JsonDeserializer#deserialize(com.google.gson.JsonElement, java.lang.reflect.Type, com.google.gson.JsonDeserializationContext)
     */
    @Override
    public Graph<MRVMRegionsMap.Region, DefaultEdge> deserialize(JsonElement json, Type typeOfT,
                                                                                       JsonDeserializationContext context) throws JsonParseException {

        JsonArray jsonArray = json.getAsJsonArray();
        Set<SerializedRegion> serializedRegions = new HashSet<>();
        Map<Integer, MRVMRegionsMap.Region> regionsById = new HashMap<>();
        SimpleGraph<MRVMRegionsMap.Region, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement regionElement = jsonArray.get(i);
            SerializedRegion r = context.deserialize(regionElement, SerializedRegion.class);
            serializedRegions.add(r);
            regionsById.put(r.getNode().getId(), r.getNode());
            graph.addVertex(r.getNode());

        }
        for (SerializedRegion serializedRegion : serializedRegions) {
            Set<Integer> neighbors = serializedRegion.getNeighbors();
            for (Integer neighborId : neighbors) {
                graph.addEdge(serializedRegion.getNode(), regionsById.get(neighborId));
            }
        }

        return graph;
    }

    public static class SerializedRegion {

        private final MRVMRegionsMap.Region node;
        private final SortedSet<Integer> neighbors;

        public SerializedRegion(MRVMRegionsMap.Region node) {
            this.node = node;
            neighbors = new TreeSet<>();
        }

        public void addAdjacentRegion(int id) {
            this.neighbors.add(id);
        }

        public MRVMRegionsMap.Region getNode() {
            return node;
        }

        public SortedSet<Integer> getNeighbors() {
            return neighbors;
        }

    }


}

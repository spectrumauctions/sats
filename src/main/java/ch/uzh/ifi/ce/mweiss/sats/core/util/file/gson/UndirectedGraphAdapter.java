/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.util.file.gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.UnmodifiableUndirectedGraph;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMRegionsMap.Region;

/**
 * @author Michael Weiss
 *
 */
public class UndirectedGraphAdapter implements JsonSerializer<UnmodifiableUndirectedGraph<Region, DefaultEdge>>, JsonDeserializer<UnmodifiableUndirectedGraph<Region,DefaultEdge>>{

    /* (non-Javadoc)
     * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
     */
    @Override
    public JsonElement serialize(UnmodifiableUndirectedGraph<Region, DefaultEdge> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray result = new JsonArray();
        for(Region region : src.vertexSet()){
            SerializedRegion r = new SerializedRegion(region);
            for (Region neighborCandidate : src.vertexSet()) {
                if(src.containsEdge(region, neighborCandidate)){
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
    public UnmodifiableUndirectedGraph<Region, DefaultEdge> deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        
        JsonArray jsonArray = json.getAsJsonArray();
        Set<SerializedRegion> serializedRegions = new HashSet<>();
        Map<Integer, Region> regionsById = new HashMap<>();
        SimpleGraph<Region, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for(int i = 0; i < jsonArray.size(); i++){
           JsonElement regionElement = jsonArray.get(i);
           SerializedRegion r = context.deserialize(regionElement, SerializedRegion.class);
           serializedRegions.add(r);
           regionsById.put(r.getNode().getId(), r.getNode());
           graph.addVertex(r.getNode());   
           
        }
        for(SerializedRegion serializedRegion : serializedRegions){
            Set<Integer> neighbors = serializedRegion.getNeighbors();
            for(Integer neighborId : neighbors){
                graph.addEdge(serializedRegion.getNode(), regionsById.get(neighborId));
            }
        }
        
        return new UnmodifiableUndirectedGraph<>(graph);
    }

    public static class SerializedRegion{
        
        private final Region node;
        private final SortedSet<Integer> neighbors;

        public SerializedRegion(Region node){
            this.node = node;
            neighbors = new TreeSet<>();
        }
        
        public void addAdjacentRegion(int id){
            this.neighbors.add(id);
        }

        public Region getNode() {
            return node;
        }

        public SortedSet<Integer> getNeighbors() {
            return neighbors;
        }
        
    }

    
}

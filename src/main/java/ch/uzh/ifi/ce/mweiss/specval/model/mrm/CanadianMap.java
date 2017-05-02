/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.mrm;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.UndirectedGraphBuilder;

import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMWorldSetup.RegionSetup;

/**
 * @author Michael Weiss
 *
 */
public class CanadianMap {

    private Map<Integer, RegionSetup> regions;
    private static CanadianMap INSTANCE;
    
    public static CanadianMap getInstance(){
        if(INSTANCE == null){
            INSTANCE = new CanadianMap();
        }
        return INSTANCE;
    }
    
    private CanadianMap(){
        initRegions();
    }
    
    protected void initRegions(){
        this.regions = new HashMap<>();
        regions.put(1 ,new RegionSetup(514711, 0, "Newfoundland and Labrador"));
        regions.put(2 ,new RegionSetup(1061900, 0, "Nova Scotia and Prince Edward Island"));
        regions.put(3 ,new RegionSetup(749623, 0, "New Brunswick"));
        regions.put(4 ,new RegionSetup(1668504, 0, "Eastern Quebec"));
        regions.put(5 ,new RegionSetup(5683127, 0, "Southern Quebec"));
        regions.put(6 ,new RegionSetup(2347556, 0, "Eastern Ontario and Outaouais"));
        regions.put(7 ,new RegionSetup(190271, 0, "Northern Quebec"));
        regions.put(8 ,new RegionSetup(10091045, 0, "Southern Ontario"));
        regions.put(9 ,new RegionSetup(773104, 0, "Northern Ontario"));
        regions.put(10 ,new RegionSetup(1208253, 0, "Manitoba"));
        regions.put(11 ,new RegionSetup(1029497+1029751, 0, "Saskatchewan (incl. Province)"));
        regions.put(12 ,new RegionSetup(3648798+3648544, 0, "Alberta (incl. Province)"));
        regions.put(13 ,new RegionSetup(4399805, 0, "British Columbia"));
        regions.put(14 ,new RegionSetup(104625, 0, "Yukon, Northwest Territories and Nunavut"));
    }
    
    public UndirectedGraph<RegionSetup, DefaultEdge> createCanadianMapGraph(){
        
        UndirectedGraph<RegionSetup, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        
        UndirectedGraphBuilder<RegionSetup, DefaultEdge, UndirectedGraph<RegionSetup, DefaultEdge>> builder = new UndirectedGraphBuilder<>(graph);
        for(RegionSetup r : regions.values()){
            builder.addVertex(r);
        }
        addAdjacency(builder, 1, 2);
        addAdjacency(builder, 1, 4);
        addAdjacency(builder, 1, 7);
        addAdjacency(builder, 2, 3);
        addAdjacency(builder, 3, 4);
        addAdjacency(builder, 3, 5);
        addAdjacency(builder, 4, 5);
        addAdjacency(builder, 4, 7);
        addAdjacency(builder, 5, 7);
        addAdjacency(builder, 6, 8);
        addAdjacency(builder, 6, 9);
        addAdjacency(builder, 7, 9);
        addAdjacency(builder, 8, 9);
        addAdjacency(builder, 9, 10);
        addAdjacency(builder, 10, 11);
        addAdjacency(builder, 11, 12);
        addAdjacency(builder, 11, 14);
        addAdjacency(builder, 12, 13);
        addAdjacency(builder, 12, 14);
        addAdjacency(builder, 13, 14);
        
        return builder.build();
    }
    
    private void addAdjacency(UndirectedGraphBuilder<RegionSetup, DefaultEdge, UndirectedGraph<RegionSetup, DefaultEdge>> builder, int r1, int r2){
        builder.addEdge(regions.get(r1), regions.get(r2));
    }
    
}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.model.srm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.Band;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.GenericDefinition;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Good;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.uzh.ifi.ce.mweiss.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public final class SRMBand extends Band implements GenericDefinition {
    
    private final List<SRMLicense> licenses;
    private final long worldId;
    
    private transient SRMWorld world;
    
    //package-private 
    static Set<SRMBand> createBands(SRMWorld world, SRMWorldSetup setup, RNGSupplier rngSupplier){
        Set<SRMBand> bands = new HashSet<>();
        int startId = 0;
        for(Entry<String, Integer> bandDefinition : setup.defineBands(rngSupplier).entrySet()){
            bands.add(new SRMBand(bandDefinition.getKey(), world, bandDefinition.getValue(), startId));
            startId += bandDefinition.getValue();
        }
        Preconditions.checkArgument(bands.size() != 0, "WorldSetup has to define at least one band");
        return bands;
    }
    
    private SRMBand (String name, SRMWorld world, int numberOfLicenses, int startId){
        super(name);
        this.world = world;
        this.worldId = world.getId();
        List<SRMLicense> builder = new ArrayList<>();
        for(int i = 0; i < numberOfLicenses; i++){
            builder.add(new SRMLicense(startId++, this));
        }
        this.licenses = Collections.unmodifiableList(builder);
    }
    
    /**
     * @return
     */
    public SRMWorld getWorld() {
        return world;
    }
    
  
    public List<SRMLicense> getLicenses() {
        return Collections.unmodifiableList(licenses);
    }


    @Override
    public String getName() {
        return name;
    }


    /**
     * See {@link SRMWorld#refreshFieldBackReferences()} for purpose of this method
     */
    void refreshFieldBackReferences(SRMWorld world) {
        Preconditions.checkArgument(world.getId() == this.worldId);
        this.world = world;
        for(SRMLicense license : licenses){
            license.refreshFieldBackReferences(this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((licenses == null) ? 0 : licenses.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        SRMBand other = (SRMBand) obj;
        if (licenses == null) {
            if (other.licenses != null)
                return false;
        } else if (!licenses.equals(other.licenses))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see Band#getNumberOfLicenses()
     */
    @Override
    public int getNumberOfLicenses() {
        return licenses.size();
    }

    /* (non-Javadoc)
     * @see GenericDefinition#isPartOf(Good)
     */
    @Override
    public boolean isPartOf(Good license) {
        if(license == null){
            return false;
        }else if (! (license instanceof SRMLicense)){
            return false;
        }
        SRMLicense srmLicense = (SRMLicense) license;
        return srmLicense.getBand().equals(this);
    }

    /* (non-Javadoc)
     * @see GenericDefinition#numberOfLicenses()
     */
    @Override
    public int numberOfLicenses() {
        return licenses.size();
    }

    /* (non-Javadoc)
     * @see GenericDefinition#allLicenses()
     */
    @Override
    public Set<SRMLicense> allLicenses() {
        return new HashSet<>(licenses);
    }


    /* (non-Javadoc)
     * @see GenericDefinition#shortJson()
     */
    @Override
    public JsonElement shortJson() {
        JsonObject json = new JsonObject();
        json.addProperty("band", getName());
        return json;
    }
    
    


}

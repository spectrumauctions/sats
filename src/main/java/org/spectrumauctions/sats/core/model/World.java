/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.spectrumauctions.sats.core.model.bm.BMWorld;
import org.spectrumauctions.sats.core.util.instancehandling.InstanceHandler;

public abstract class World implements Serializable {

    private static final long serialVersionUID = -2556437180180680834L;

    protected final String modelName;
    protected final long id;
    
    public World(String modelName){
        this.id = InstanceHandler.getDefaultHandler().getNextWorldId();
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    public long getId() {
        return id;
    };

    public abstract int getNumberOfGoods();
    
    public abstract Set<? extends Good> getLicenses();
    
    protected void store(){
        InstanceHandler.getDefaultHandler().writeWorld(this);
    }
    
    protected long openNewPopulation(){
        return InstanceHandler.getDefaultHandler().getNextPopulationId(getId());
    }
    
    /**
     * @param populationId
     * @return
     */
    public abstract Collection<? extends Bidder<?>> restorePopulation(long populationId);
    
    /**
     * Advanced way to restore serialized {@link Bidder} instances, allowing to specify a custom {@link InstanceHandler} <br>
     * For most use cases, it's recommended to use {@link BMWorld#restorePopulation(long)}. <br><br>
     * 
     * Note: Bidders and World must have been serialized before, either during construction (by having set {@link InstanceHandler#setDefaultHandler(InstanceHandler)} 
     * to an appropriate handler or by manually storing them with an {@link InstanceHandler} afterwards. Restoring (deserialization) has to be done
     * with the same type of {@link InstanceHandler} as the serialization.
     * @param type
     * @param populationId
     * @param storageHandler
     * @return
     */
    public <T extends Bidder<?>> Collection<T> restorePopulation(Class<T> type, long populationId, InstanceHandler storageHandler){
        return storageHandler.readPopulationWithUnknownTypes(type, this, populationId);
    } 
    
    protected <T extends Bidder<?>> Collection<T> restorePopulation(Class<T> type, long populationId){
        return restorePopulation(type, populationId, InstanceHandler.getDefaultHandler());
    }
    
    
    /**
     * Some of the members of the World (e.g. licenses) have circular references back to the world.<br> 
     * As the used gsonSerializer cannot handle this yet, the circular references are not serialized
     * and have to be restored after deserialization by calling this method.<br><br>
     * 
     * This method will be removed in a later version and the problem be solved during deserialization.
     */
    public abstract void refreshFieldBackReferences();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((modelName == null) ? 0 : modelName.hashCode());
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
        World other = (World) obj;
        if (modelName == null) {
            if (other.modelName != null)
                return false;
        } else if (!modelName.equals(other.modelName))
            return false;
        return true;
    }

    

}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.util.instancehandling;

import java.util.Collection;

import ch.uzh.ifi.ce.mweiss.sats.core.model.World;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;

/**
 * @author Michael Weiss
 *
 */
public abstract class InstanceHandler {
    
    private static InstanceHandler defaultHandler;
    
    /**
     * Get the default instance handler. <br>
     * The default instance handler is called on every {@link World}, <b>population</b> and {@link Bidder} creation
     * for id selection and default storing.
     * @return
     */
    public static InstanceHandler getDefaultHandler(){
        if(defaultHandler == null){
            defaultHandler = JSONInstanceHandler.getInstance();
        }
        return defaultHandler;
    }

    /**
     * Set a new default instance handler.<br>
     * The default instance handler is called on every {@link World}, <b>population</b> and {@link Bidder} creation
     * for id selection and default storing.
     * @param defaultHandler
     */
    public static void setDefaultHandler(InstanceHandler defaultHandler){
        InstanceHandler.defaultHandler = defaultHandler;
    }
        
    /**
     * Writes a world instance.
     * If a world with the same id already is stored, it might be overwritten.
     * @param world
     */
    public abstract void writeWorld(World world);
    
    /**
     * Writes a world instance.
     * If a world with the same id already is stored, it might be overwritten.
     * @param world
     */
    public abstract <T extends World> T readWorld(Class<T> type, long world);
    
    /**
     * Writes a new bidder instance.
     * If a bidder with the same world and population id already is stored, it might be overwritten.
     * @param bidder
     */
    public abstract void writeBidder(Bidder<?> bidder);

    /**
     * @return A collection containing all ids of the stored population for the requested world
     */
    public abstract Collection<Long> getPopulationIds(long worldId);
    
    /**
     * Used to deserialize a bidder, if its implementing class is known 
     * @param type The class of which a new instance should be created
     * @param worldId
     * @param populationId
     * @param bidderId
     * @return
     */
    public abstract <T extends Bidder<?>> T readBidder(Class<T> type, World world, long populationId, long bidderId);
    
    /**
     * Used to deserialize a bidder, if its type is not exacly known, 
     * i.e., if there are different bidder implementations for this model.
     * @param bidderSuperType The deserialized bidder is either of class bidderSuperType or of a subclass of bidderSuperType
     * @param worldId
     * @param populationId
     * @param bidderId
     * @return
     */
    public abstract <T extends Bidder<?>> T readBidderWithUnknownType(Class<T> bidderSuperType, World world, long populationId, long bidderId);
    
    /**
     * Used to deserialize a set of bidders, if the implementing class of all bidders is known and the same 
     * @param type  The class of which the new bidder instances should be created
     * @param worldId
     * @param populationId
     * @return
     */
    public abstract <T extends Bidder<?>> Collection<T> readPopulation(Class<T> type, World world, long populationId);
    
    /**
     * Used to deserialize a set of bidders, if their type is not exacly known, 
     * i.e., if there are different bidder implementations for this model.
     * @param bidderSuperType The deserialized bidder is either of class bidderSuperType or of a subclass of bidderSuperType
     * @param worldId
     * @param populationId
     * @return
     */
    public abstract <T extends Bidder<?>> Collection<T> readPopulationWithUnknownTypes(Class<T> bidderSuperType, World world, long populationId);
    

    /**
     * Returns an new unused world id.
     * The id is unique amongst all previously generated world ids.
     * @return
     */
    public abstract long getNextWorldId();
    
    /**
     * Returns a new population id
     * The id is unique amongst all previously generated population ids with the same worldId.
     * @param worldId The id of the world to which the new population will belong
     * @return
     */
    public abstract long getNextPopulationId(long worldId);
}

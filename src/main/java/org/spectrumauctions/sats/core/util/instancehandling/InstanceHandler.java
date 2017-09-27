/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util.instancehandling;

import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.World;

import java.util.Collection;

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
     * @return the default instance handler
     */
    public static InstanceHandler getDefaultHandler() {
        if (defaultHandler == null) {
            defaultHandler = JSONInstanceHandler.getInstance();
        }
        return defaultHandler;
    }

    /**
     * Set a new default instance handler.<br>
     * The default instance handler is called on every {@link World}, <b>population</b> and {@link Bidder} creation
     * for id selection and default storing.
     * @param defaultHandler the new default instance handler
     */
    public static void setDefaultHandler(InstanceHandler defaultHandler) {
        InstanceHandler.defaultHandler = defaultHandler;
    }

    /**
     * Writes a world instance.
     * If a world with the same id already is stored, it might be overwritten.
     * @param world the world to be written
     */
    public abstract void writeWorld(World world);

    /**
     * Reads a world instance.
     */
    public abstract <T extends World> T readWorld(Class<T> type, long world);

    /**
     * Writes a new bidder instance.
     * If a bidder with the same world and population id already is stored, it might be overwritten.
     * @param bidder the bidder to be written
     */
    public abstract void writeBidder(Bidder<?> bidder);

    /**
     * @return A collection containing all ids of the stored population for the requested world
     */
    public abstract Collection<Long> getPopulationIds(long worldId);

    /**
     * Used to deserialize a bidder, if its implementing class is known 
     * @param type the class of which a new instance should be created
     * @param world the world the bidder lives in
     * @param populationId the id of the population
     * @param bidderId the id of the bidder
     * @return the deserialized bidder
     */
    public abstract <T extends Bidder<?>> T readBidder(Class<T> type, World world, long populationId, long bidderId);

    /**
     * Used to deserialize a bidder, if its type is not exactly known,
     * i.e., if there are different bidder implementations for this model.
     * @param bidderSuperType the deserialized bidder is either of class bidderSuperType or of a subclass of bidderSuperType
     * @param world the world the bidder lives in
     * @param populationId the id of the population
     * @param bidderId the id of the bidder
     * @return the deserialized bidder
     */
    public abstract <T extends Bidder<?>> T readBidderWithUnknownType(Class<T> bidderSuperType, World world, long populationId, long bidderId);

    /**
     * Used to deserialize a set of bidders, if the implementing class of all bidders is known and the same 
     * @param type the class of which the new bidder instances should be created
     * @param world the world the bidders live in
     * @param populationId the id of the population
     * @return the deserialized bidders
     */
    public abstract <T extends Bidder<?>> Collection<T> readPopulation(Class<T> type, World world, long populationId);

    /**
     * Used to deserialize a set of bidders, if their type is not exactly known,
     * i.e., if there are different bidder implementations for this model.
     * @param bidderSuperType the deserialized bidder is either of class bidderSuperType or of a subclass of bidderSuperType
     * @param world the world the bidders live in
     * @param populationId the id of the population
     * @return the deserialized bidders
     */
    public abstract <T extends Bidder<?>> Collection<T> readPopulationWithUnknownTypes(Class<T> bidderSuperType, World world, long populationId);

    /**
     * Returns an new unused world id.
     * The id is unique amongst all previously generated world ids.
     * @return an unused world id
     */
    public abstract long getNextWorldId();

    /**
     * Returns a new population id
     * The id is unique amongst all previously generated population ids with the same worldId.
     * @param worldId The id of the world to which the new population will belong
     * @return a new population id
     */
    public abstract long getNextPopulationId(long worldId);
}

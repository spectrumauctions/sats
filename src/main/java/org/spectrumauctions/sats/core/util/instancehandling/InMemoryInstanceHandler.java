/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util.instancehandling;

import java.util.Collection;

import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;
import org.spectrumauctions.sats.core.util.random.UniformJavaUtilRandomWrapper;

/**
 * A fast <b>instance handler</b> implementation.<br>
 * No files are written - all <i>write</i> methods are empty mocks.<br>
 * <br>
 * The use of this handler is only recommended if no instances are serialized or deserialized. <br>
 * Otherwise, id collisions may occure.
 * @author Michael Weiss
 *
 */
public class InMemoryInstanceHandler extends InstanceHandler {
    
    private static InMemoryInstanceHandler instance;
    private static final String UNSUPPORTED_OPERATION_MESSAGE = "The selected InstanceHandler does not support this method. Use another Instance Handler instead.";
    
    private long nextWorldId;
    private long nextPopulationId;
    
    /**
     * Choose the starting id's (which than are just steadily increased) randomly, but higher than {@link Integer#MAX_VALUE}
     */
    private InMemoryInstanceHandler() {
        UniformDistributionRNG rng = new UniformJavaUtilRandomWrapper();
        nextPopulationId = (long) Integer.MAX_VALUE + (long) rng.nextInt();
        nextWorldId = (long) Integer.MAX_VALUE + (long) rng.nextInt();
    }
    
    public static InMemoryInstanceHandler getInstance(){
        if(instance == null){
            instance = new InMemoryInstanceHandler();
        }
        return instance;
    }

    /* (non-Javadoc)
     * @see InstanceHandler#writeWorld(World)
     */
    @Override
    public void writeWorld(World world) {
        // Empty mock - this handler does not write files but method may still be called
    }

    /* (non-Javadoc)
     * @see InstanceHandler#writeBidder(Bidder)
     */
    @Override
    public void writeBidder(Bidder<?> bidder) {
        // Empty mock - this handler does not write files but method may still be called
    }

    /* (non-Javadoc)
     * @see InstanceHandler#getPopulationIds(int)
     */
    @Override
    public Collection<Long> getPopulationIds(long worldId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    /* (non-Javadoc)
     * @see InstanceHandler#readBidder(java.lang.Class, int, int, int)
     */
    @Override
    public <T extends Bidder<?>> T readBidder(Class<T> type, World world, long populationId, long bidderId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    /* (non-Javadoc)
     * @see InstanceHandler#readPopulation(java.lang.Class, int, int)
     */
    @Override
    public <T extends Bidder<?>> Collection<T> readPopulation(Class<T> type, World world, long populationId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }
    
    /* (non-Javadoc)
     * @see InstanceHandler#readBidder(java.util.Map, long, long, long)
     */
    @Override
    public <T extends Bidder<?>> T readBidderWithUnknownType(Class<T> bidderSuperType, World world, long populationId,
            long bidderId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    /* (non-Javadoc)
     * @see InstanceHandler#readPopulation(java.util.Map, long, long)
     */
    @Override
    public <T extends Bidder<?>> Collection<T> readPopulationWithUnknownTypes(Class<T> bidderSuperType, World world,
            long populationId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }
    

    /* (non-Javadoc)
     * @see InstanceHandler#getNextWorldId()
     */
    @Override
    public long getNextWorldId() {
        return nextWorldId++;
    }

    /* (non-Javadoc)
     * @see InstanceHandler#getNextPopulationId(long)
     */
    @Override
    public long getNextPopulationId(long worldId) {
        return nextPopulationId++;
    }

    /* (non-Javadoc)
     * @see InstanceHandler#readWorld(java.lang.Class, long)
     */
    @Override
    public <T extends World> T readWorld(Class<T> type, long world) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }



}

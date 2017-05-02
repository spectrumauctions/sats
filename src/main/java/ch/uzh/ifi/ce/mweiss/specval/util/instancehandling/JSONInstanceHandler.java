/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.util.instancehandling;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.World;
import ch.uzh.ifi.ce.mweiss.specval.util.CacheMap;
import ch.uzh.ifi.ce.mweiss.specval.util.file.FileException;
import ch.uzh.ifi.ce.mweiss.specval.util.file.FilePathUtils;
import ch.uzh.ifi.ce.mweiss.specval.util.file.gson.GsonWrapper;

/**
 * @author Michael Weiss
 *
 */
public class JSONInstanceHandler extends InstanceHandler {
    
    private FilePathUtils pathUtils = FilePathUtils.getInstance();
    private GsonWrapper gson = GsonWrapper.getInstance();

    private static JSONInstanceHandler instance;
    
    // Caches 100 top population id's
    private final Map<Long, Long> populationIdCache = new CacheMap<Long, Long>(100);

    private long worldIdCache = 0;
    
    
    private JSONInstanceHandler() { }
    
    public static JSONInstanceHandler getInstance(){
        if(instance == null){
            instance = new JSONInstanceHandler();
        }
        return instance;
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.util.instancehandling.InstanceHandler#writeWorld(ch.uzh.ifi.ce.mweiss.specval.model.World)
     */
    @Override
    public void writeWorld(World world) {
        File file = pathUtils.worldFilePath(world.getId());
        String json = gson.toJson(world);
        pathUtils.writeStringToFile(file, json);
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.util.instancehandling.InstanceHandler#writeBidder(ch.uzh.ifi.ce.mweiss.specval.model.Bidder)
     */
    @Override
    public void writeBidder(Bidder<?> bidder) {
        File file = pathUtils.bidderFilePath(
                bidder.getWorld().getId(), 
                bidder.getPopulation(), 
                bidder.getId());
        String json = gson.toJson(bidder);
        pathUtils.writeStringToFile(file, json);      
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.util.instancehandling.InstanceHandler#getPopulationIds(int)
     */
    @Override
    public Collection<Long> getPopulationIds(long worldId) {
        return pathUtils.getPopulationIds(worldId);
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.util.instancehandling.InstanceHandler#readBidder(java.util.Map, int, int, int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Bidder<?>> T readBidderWithUnknownType(Class<T> bidderSuperType, World world, long populationId,
            long bidderId) {
        File file = pathUtils.bidderFilePath(world.getId(), populationId, bidderId);
        String json = pathUtils.readFileToString(file);
        Class<?> type = gson.readClass(json);
        Object obj = gson.fromJson(type, json);
        
        if(bidderSuperType.isAssignableFrom(obj.getClass())){
            return (T) obj;
        }else{
            throw new FileException("generated object (" + type.getName() +") is not of specified bidder type (" + bidderSuperType.getName() +")");
        }
    }     
    
    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.util.instancehandling.InstanceHandler#readBidder(java.lang.Class, int, int, int)
     */
    @Override
    public <T extends Bidder<?>> T readBidder(Class<T> type, World world, long populationId, long bidderId) {
        File file = pathUtils.bidderFilePath(world.getId(), populationId, bidderId);
        String json = pathUtils.readFileToString(file);
        T bidder = gson.fromJson(type, json);
        bidder.refreshReference(world);
        return bidder;        
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.util.instancehandling.InstanceHandler#readPopulation(java.util.Map, int, int)
     */
    @Override
    public <T extends Bidder<?>> Collection<T> readPopulationWithUnknownTypes(Class<T> bidderSuperType, World world,
            long populationId) {
        Set<T> bidders = new HashSet<>();
        Collection<Long> bidderIds = pathUtils.getBidderIds(world.getId(), populationId);
        for(long bidderId : bidderIds){
            bidders.add(readBidderWithUnknownType(bidderSuperType, world, populationId, bidderId));
        }
        return bidders;
    }
    
    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.util.instancehandling.InstanceHandler#readPopulation(java.lang.Class, int, int)
     */
    @Override
    public <T extends Bidder<?>> Collection<T> readPopulation(Class<T> type, World world, long populationId) {
        Set<T> bidders = new HashSet<>();
        Collection<Long> bidderIds = pathUtils.getBidderIds(world.getId(), populationId);
        for(long bidderId : bidderIds){
            bidders.add(readBidder(type, world, populationId, bidderId));
        }
        return bidders;
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.util.instancehandling.InstanceHandler#getNextWorldId()
     */
    @Override
    public long getNextWorldId() {
        long idCandidate = worldIdCache;
        idCandidate = recGetAndReserveNewWorldId(idCandidate);
        worldIdCache = idCandidate + 1;
        return idCandidate;
    }
    
    /**
     * Attempts to create a new world folder with the id idCandidate. <br>
     * If the folder already exists, it tries again with a higher id
     * @param idCandidate
     * @return
     */
    private long recGetAndReserveNewWorldId(long idCandidate) {
        java.io.File potentialFolder = pathUtils.worldFolderPath(idCandidate);
        if (potentialFolder.mkdirs()) {
            return idCandidate;
        } else {
            return recGetAndReserveNewWorldId(idCandidate + 1);
        }
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.util.instancehandling.InstanceHandler#getNextPopulationId(long)
     */
    @Override
    public long getNextPopulationId(long worldId) {
        //TODO check if world exists in file system
        Long idCandidate = populationIdCache.remove(worldId);
        if (idCandidate == null){
        	idCandidate = Long.valueOf("0");
        }

        idCandidate = recOpenPopulation(worldId, 0);

        Long newCacheId = idCandidate + 1;
        // Register in Cache
        populationIdCache.put(worldId, newCacheId);

        return idCandidate;
    }
    
    /**
     * Attempts to create a new population folder with the id idCandidate. <br>
     * If the folder already exists, it tries again with a higher id
     * @param idCandidate
     * @return
     */
    private long recOpenPopulation(long worldId, long idCandidate) {
        java.io.File potentialPopulationFolder = pathUtils.populationFolderPath(worldId, idCandidate);
        if (potentialPopulationFolder.mkdir()) {
            return idCandidate;
        }else{
            return recOpenPopulation(worldId, ++idCandidate);
        }
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.util.instancehandling.InstanceHandler#readWorld(java.lang.Class, long)
     */
    @Override
    public <T extends World> T readWorld(Class<T> type, long worldId) {
        String json = pathUtils.readFileToString(pathUtils.worldFilePath(worldId));
        T world = gson.fromJson(type, json);
        world.refreshFieldBackReferences();
        return world;
    }
    
    

}

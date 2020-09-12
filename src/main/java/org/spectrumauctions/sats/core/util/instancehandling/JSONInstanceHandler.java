/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util.instancehandling;

import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.CacheMap;
import org.spectrumauctions.sats.core.util.file.FileException;
import org.spectrumauctions.sats.core.util.file.FilePathUtils;
import org.spectrumauctions.sats.core.util.file.gson.GsonWrapper;

import java.io.File;
import java.util.*;

/**
 * @author Michael Weiss
 *
 */
public class JSONInstanceHandler extends InstanceHandler {

    private FilePathUtils pathUtils = FilePathUtils.getInstance();

    private static JSONInstanceHandler instance;

    // Caches 100 top population id's
    private final Map<Long, Long> populationIdCache = new CacheMap<>(100);

    private long worldIdCache = 0;


    private JSONInstanceHandler() {
    }

    public static JSONInstanceHandler getInstance() {
        if (instance == null) {
            instance = new JSONInstanceHandler();
        }
        return instance;
    }

    /* (non-Javadoc)
     * @see InstanceHandler#writeWorld(World)
     */
    @Override
    public void writeWorld(World world) {
        File file = pathUtils.worldFilePath(world.getId());
        String json = new GsonWrapper().toJson(world);
        pathUtils.writeStringToFile(file, json);
    }

    /* (non-Javadoc)
     * @see InstanceHandler#writeBidder(SATSBidder)
     */
    @Override
    public void writeBidder(SATSBidder bidder) {
        File file = pathUtils.bidderFilePath(
                bidder.getWorld().getId(),
                bidder.getPopulation(),
                bidder.getLongId());
        String json = new GsonWrapper().toJson(bidder);
        pathUtils.writeStringToFile(file, json);
    }

    /* (non-Javadoc)
     * @see InstanceHandler#getPopulationIds(int)
     */
    @Override
    public Collection<Long> getPopulationIds(long worldId) {
        return pathUtils.getPopulationIds(worldId);
    }

    /* (non-Javadoc)
     * @see InstanceHandler#readBidder(java.util.Map, int, int, int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends SATSBidder> T readBidderWithUnknownType(Class<T> bidderSuperType, World world, long populationId,
                                                                 long bidderId) {
        File file = pathUtils.bidderFilePath(world.getId(), populationId, bidderId);
        String json = pathUtils.readFileToString(file);
        
        GsonWrapper gson = new GsonWrapper();
        gson.setWorld(world);
        Class<?> type = gson.readClass(json);
        Object obj = gson.fromJson(type, json);

        if (bidderSuperType.isAssignableFrom(obj.getClass())) {
            SATSBidder bidder = (T) obj;
            bidder.refreshReference(world);
            return (T) bidder;

        } else {
            throw new FileException("generated object (" + type.getName() + ") is not of specified bidder type (" + bidderSuperType.getName() + ")");
        }
    }

    /* (non-Javadoc)
     * @see InstanceHandler#readBidder(java.lang.Class, int, int, int)
     */
    @Override
    public <T extends SATSBidder> T readBidder(Class<T> type, World world, long populationId, long bidderId) {
        File file = pathUtils.bidderFilePath(world.getId(), populationId, bidderId);
        GsonWrapper gson = new GsonWrapper();
        gson.setWorld(world);
        String json = pathUtils.readFileToString(file);
        T bidder = gson.fromJson(type, json);
        bidder.refreshReference(world);
        return bidder;
    }

    /* (non-Javadoc)
     * @see InstanceHandler#readPopulation(java.util.Map, int, int)
     */
    @Override
    public <T extends SATSBidder> List<T> readPopulationWithUnknownTypes(Class<T> bidderSuperType, World world,
                                                                         long populationId) {
        List<T> bidders = new ArrayList<>();
        Collection<Long> bidderIds = pathUtils.getBidderIds(world.getId(), populationId);
        for (long bidderId : bidderIds) {
            bidders.add(readBidderWithUnknownType(bidderSuperType, world, populationId, bidderId));
        }
        return bidders;
    }

    /* (non-Javadoc)
     * @see InstanceHandler#readPopulation(java.lang.Class, int, int)
     */
    @Override
    public <T extends SATSBidder> Collection<T> readPopulation(Class<T> type, World world, long populationId) {
        Set<T> bidders = new HashSet<>();
        Collection<Long> bidderIds = pathUtils.getBidderIds(world.getId(), populationId);
        for (long bidderId : bidderIds) {
            bidders.add(readBidder(type, world, populationId, bidderId));
        }
        return bidders;
    }

    /* (non-Javadoc)
     * @see InstanceHandler#getNextWorldId()
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
     * @see InstanceHandler#getNextPopulationId(long)
     */
    @Override
    public long getNextPopulationId(long worldId) {
        //TODO check if world exists in file system
        Long idCandidate = populationIdCache.remove(worldId);
        if (idCandidate == null) {
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
     */
    private long recOpenPopulation(long worldId, long idCandidate) {
        java.io.File potentialPopulationFolder = pathUtils.populationFolderPath(worldId, idCandidate);
        if (potentialPopulationFolder.mkdir()) {
            return idCandidate;
        } else {
            return recOpenPopulation(worldId, ++idCandidate);
        }
    }

    /* (non-Javadoc)
     * @see InstanceHandler#readWorld(java.lang.Class, long)
     */
    @Override
    public <T extends World> T readWorld(Class<T> type, long worldId) {
        String json = pathUtils.readFileToString(pathUtils.worldFilePath(worldId));
        T world = new GsonWrapper().fromJson(type, json);
        world.refreshFieldBackReferences();
        return world;
    }


}

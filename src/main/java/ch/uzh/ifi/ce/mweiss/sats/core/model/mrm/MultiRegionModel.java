/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.model.mrm;

import java.util.List;

import com.google.common.base.Preconditions;

import ch.uzh.ifi.ce.mweiss.sats.core.model.DefaultModel;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMWorldSetup.MRMWorldSetupBuilder;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public class MultiRegionModel extends DefaultModel<MRMWorld, MRMBidder> {
   
    private MRMWorldSetupBuilder worldBuilder;
    private MRMLocalBidderSetup.Builder localBidderBuilder;
    private MRMRegionalBidderSetup.Builder regionalBidderBuilder;
    private MRMGlobalBidderSetup.Builder globalBidderBuilder;

    
    /**
     * @param worldBuilder
     * @param localBidder
     * @param regionalBidder
     * @param globalBidder
     */
    public MultiRegionModel() {
        super();
        this.worldBuilder = new MRMWorldSetupBuilder();
        this.localBidderBuilder = new MRMLocalBidderSetup.Builder();
        this.regionalBidderBuilder = new MRMRegionalBidderSetup.Builder();
        this.globalBidderBuilder = new MRMGlobalBidderSetup.Builder();
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.satscore.model.QuickDefaultAccess#createWorld(RNGSupplier)
     */
    @Override
    public MRMWorld createWorld(RNGSupplier worldSeed) {
        return new MRMWorld(worldBuilder.build(), worldSeed);
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.satscore.model.QuickDefaultAccess#createPopulation(World, RNGSupplier)
     */
    @Override
    public List<MRMBidder> createPopulation(MRMWorld world, RNGSupplier populationRNG) {
        return world.createPopulation(localBidderBuilder.build(), regionalBidderBuilder.build(), globalBidderBuilder.build(), populationRNG);
    }
    
    public void setNumberOfLocalBidders(int number){
        Preconditions.checkArgument(number >= 0);
        localBidderBuilder.setNumberOfBidders(number);
    }
    
    public void setNumberOfRegionalBidders(int number){
        Preconditions.checkArgument(number >= 0);
        regionalBidderBuilder.setNumberOfBidders(number);
    }
    
    public void setNumberOfGlobalBidders(int number){
        Preconditions.checkArgument(number >= 0);
        globalBidderBuilder.setNumberOfBidders(number);
    }

}

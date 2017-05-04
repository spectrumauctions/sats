/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import java.util.List;

import com.google.common.base.Preconditions;

import org.spectrumauctions.sats.core.model.DefaultModel;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public class MultiRegionModel extends DefaultModel<MRVMWorld, MRVMBidder> {
   
    private MRVMWorldSetup.MRMWorldSetupBuilder worldBuilder;
    private MRVMLocalBidderSetup.Builder localBidderBuilder;
    private MRVMRegionalBidderSetup.Builder regionalBidderBuilder;
    private MRVMNationalBidderSetup.Builder nationalBidderBuilder;

    public MultiRegionModel() {
        super();
        this.worldBuilder = new MRVMWorldSetup.MRMWorldSetupBuilder();
        this.localBidderBuilder = new MRVMLocalBidderSetup.Builder();
        this.regionalBidderBuilder = new MRVMRegionalBidderSetup.Builder();
        this.nationalBidderBuilder = new MRVMNationalBidderSetup.Builder();
    }

    /* (non-Javadoc)
     * @see org.spectrumauctions.sats.core.model.QuickDefaultAccess#createWorld(RNGSupplier)
     */
    @Override
    public MRVMWorld createWorld(RNGSupplier worldSeed) {
        return new MRVMWorld(worldBuilder.build(), worldSeed);
    }

    /* (non-Javadoc)
     * @see org.spectrumauctions.sats.core.model.QuickDefaultAccess#createPopulation(World, RNGSupplier)
     */
    @Override
    public List<MRVMBidder> createPopulation(MRVMWorld world, RNGSupplier populationRNG) {
        return world.createPopulation(localBidderBuilder.build(), regionalBidderBuilder.build(), nationalBidderBuilder.build(), populationRNG);
    }
    
    public void setNumberOfLocalBidders(int number){
        Preconditions.checkArgument(number >= 0);
        localBidderBuilder.setNumberOfBidders(number);
    }
    
    public void setNumberOfRegionalBidders(int number){
        Preconditions.checkArgument(number >= 0);
        regionalBidderBuilder.setNumberOfBidders(number);
    }
    
    public void setNumberOfNationalBidders(int number){
        Preconditions.checkArgument(number >= 0);
        nationalBidderBuilder.setNumberOfBidders(number);
    }

}

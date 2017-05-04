/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.srvm;

import java.util.ArrayList;
import java.util.List;

import org.spectrumauctions.sats.core.model.DefaultModel;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public class SingleRegionModel extends DefaultModel<SRVMWorld, SRVMBidder> {

    SRVMWorldSetup.Builder worldSetupBuilder = new SRVMWorldSetup.Builder();
    SRVMBidderSetup.SmallBidderBuilder smallBidderBuilder = new SRVMBidderSetup.SmallBidderBuilder();
    SRVMBidderSetup.HighFrequenceBidderBuilder highFrequencyBuilder = new SRVMBidderSetup.HighFrequenceBidderBuilder();
    SRVMBidderSetup.SecondaryBidderBuilder secondaryBidderBuilder = new SRVMBidderSetup.SecondaryBidderBuilder();
    SRVMBidderSetup.PrimaryBidderBuilder primaryBidderBuilder = new SRVMBidderSetup.PrimaryBidderBuilder();
    
    /* (non-Javadoc)
     * @see org.spectrumauctions.sats.core.model.QuickDefaultAccess#createWorld(RNGSupplier)
     */
    @Override
    public SRVMWorld createWorld(RNGSupplier worldSeed) {
        return new SRVMWorld(worldSetupBuilder.build(), worldSeed);
    }

    /* (non-Javadoc)
     * @see org.spectrumauctions.sats.core.model.QuickDefaultAccess#createPopulation(World, RNGSupplier)
     */
    @Override
    public List<SRVMBidder> createPopulation(SRVMWorld world, RNGSupplier populationRNG) {
        List<SRVMBidderSetup> setups = new ArrayList<>();
        setups.add(smallBidderBuilder.build());
        setups.add(highFrequencyBuilder.build());
        setups.add(secondaryBidderBuilder.build());
        setups.add(primaryBidderBuilder.build());
        return world.createPopulation(setups, populationRNG);
    }
    
    public void setNumberOfSmallBidders(int numberOfBidders) {
        smallBidderBuilder.setNumberOfBidders(numberOfBidders);
    }

    public void setNumberOfHighFrequencyBidders(int numberOfBidders) {
        highFrequencyBuilder.setNumberOfBidders(numberOfBidders);
    }

    public void setNumberOfSecondaryBidders(int numberOfBidders) {
        secondaryBidderBuilder.setNumberOfBidders(numberOfBidders);
    }

    public void setNumberOfPrimaryBidders(int numberOfBidders) {
        primaryBidderBuilder.setNumberOfBidders(numberOfBidders);
    }
}

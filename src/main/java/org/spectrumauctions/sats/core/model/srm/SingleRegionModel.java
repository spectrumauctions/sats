/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.srm;

import java.util.ArrayList;
import java.util.List;

import org.spectrumauctions.sats.core.model.DefaultModel;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public class SingleRegionModel extends DefaultModel<SRMWorld, SRMBidder> {

    SRMWorldSetup.Builder worldSetupBuilder = new SRMWorldSetup.Builder();
    SRMBidderSetup.SmallBidderBuilder smallBidderBuilder = new SRMBidderSetup.SmallBidderBuilder();
    SRMBidderSetup.HighFrequenceBidderBuilder highFrequencyBuilder = new SRMBidderSetup.HighFrequenceBidderBuilder();
    SRMBidderSetup.SecondaryBidderBuilder secondaryBidderBuilder = new SRMBidderSetup.SecondaryBidderBuilder();
    SRMBidderSetup.PrimaryBidderBuilder primaryBidderBuilder = new SRMBidderSetup.PrimaryBidderBuilder();
    
    /* (non-Javadoc)
     * @see org.spectrumauctions.sats.core.model.QuickDefaultAccess#createWorld(RNGSupplier)
     */
    @Override
    public SRMWorld createWorld(RNGSupplier worldSeed) {
        return new SRMWorld(worldSetupBuilder.build(), worldSeed);
    }

    /* (non-Javadoc)
     * @see org.spectrumauctions.sats.core.model.QuickDefaultAccess#createPopulation(World, RNGSupplier)
     */
    @Override
    public List<SRMBidder> createPopulation(SRMWorld world, RNGSupplier populationRNG) {
        List<SRMBidderSetup> setups = new ArrayList<>();
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

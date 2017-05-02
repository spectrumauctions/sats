/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.opt.model.srm;

import ch.uzh.ifi.ce.mweiss.sats.core.model.srm.SRMBidderSetup;
import ch.uzh.ifi.ce.mweiss.sats.core.model.srm.SRMWorldSetup;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabio Isler
 *
 */
public abstract class SRMWorldGen {

    static SRMWorldSetup getSingleBandWorldSetup() {
        SRMWorldSetup.Builder builder = new SRMWorldSetup.Builder();
        builder.removeBand("B");
        builder.removeBand("C");

        return builder.build();
    }

    static SRMWorldSetup getStandardWorldBuilder() {
        SRMWorldSetup.Builder builder = new SRMWorldSetup.Builder();
        return builder.build();
    }


    public static Set<SRMBidderSetup> getSimpleSmallBidderSetup(int numberOfBidders) {
        SRMBidderSetup.SmallBidderBuilder builder = new SRMBidderSetup.SmallBidderBuilder();
        builder.setSetupName("Simple Small Bidder");
        builder.setNumberOfBidders(numberOfBidders);
        Set<SRMBidderSetup> setups = new HashSet<>();
        setups.add(builder.build());
        return setups;
    }

    public static Set<SRMBidderSetup> getSimpleHighFrequencyBidderSetup(int numberOfBidders) {
        SRMBidderSetup.HighFrequenceBidderBuilder builder = new SRMBidderSetup.HighFrequenceBidderBuilder();
        builder.setSetupName("Simple High Frequency Bidder");
        builder.setNumberOfBidders(numberOfBidders);
        Set<SRMBidderSetup> setups = new HashSet<>();
        setups.add(builder.build());
        return setups;
    }

    public static Set<SRMBidderSetup> getStandardBidderSetups() {
        SRMBidderSetup.SmallBidderBuilder builder1 = new SRMBidderSetup.SmallBidderBuilder();
        SRMBidderSetup.HighFrequenceBidderBuilder builder2 = new SRMBidderSetup.HighFrequenceBidderBuilder();
        SRMBidderSetup.PrimaryBidderBuilder builder3 = new SRMBidderSetup.PrimaryBidderBuilder();
        SRMBidderSetup.SecondaryBidderBuilder builder4 = new SRMBidderSetup.SecondaryBidderBuilder();
        Set<SRMBidderSetup> setups = new HashSet<>();
        setups.add(builder1.build()); setups.add(builder2.build());
        setups.add(builder3.build()); setups.add(builder4.build());
        return setups;
    }
}

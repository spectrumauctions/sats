/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.srvm;


import org.spectrumauctions.sats.core.model.srvm.SRVMBidderSetup;
import org.spectrumauctions.sats.core.model.srvm.SRVMWorldSetup;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabio Isler
 */
public abstract class SRVMWorldGen {

    static SRVMWorldSetup getSingleBandWorldSetup() {
        SRVMWorldSetup.Builder builder = new SRVMWorldSetup.Builder();
        builder.removeBand("B");
        builder.removeBand("C");

        return builder.build();
    }

    static SRVMWorldSetup getStandardWorldBuilder() {
        SRVMWorldSetup.Builder builder = new SRVMWorldSetup.Builder();
        return builder.build();
    }


    public static Set<SRVMBidderSetup> getSimpleSmallBidderSetup(int numberOfBidders) {
        SRVMBidderSetup.SmallBidderBuilder builder = new SRVMBidderSetup.SmallBidderBuilder();
        builder.setSetupName("Simple Small Bidder");
        builder.setNumberOfBidders(numberOfBidders);
        Set<SRVMBidderSetup> setups = new HashSet<>();
        setups.add(builder.build());
        return setups;
    }

    public static Set<SRVMBidderSetup> getSimpleHighFrequencyBidderSetup(int numberOfBidders) {
        SRVMBidderSetup.HighFrequenceBidderBuilder builder = new SRVMBidderSetup.HighFrequenceBidderBuilder();
        builder.setSetupName("Simple High Frequency Bidder");
        builder.setNumberOfBidders(numberOfBidders);
        Set<SRVMBidderSetup> setups = new HashSet<>();
        setups.add(builder.build());
        return setups;
    }

    public static Set<SRVMBidderSetup> getStandardBidderSetups() {
        SRVMBidderSetup.SmallBidderBuilder builder1 = new SRVMBidderSetup.SmallBidderBuilder();
        SRVMBidderSetup.HighFrequenceBidderBuilder builder2 = new SRVMBidderSetup.HighFrequenceBidderBuilder();
        SRVMBidderSetup.PrimaryBidderBuilder builder3 = new SRVMBidderSetup.PrimaryBidderBuilder();
        SRVMBidderSetup.SecondaryBidderBuilder builder4 = new SRVMBidderSetup.SecondaryBidderBuilder();
        Set<SRVMBidderSetup> setups = new HashSet<>();
        setups.add(builder1.build());
        setups.add(builder2.build());
        setups.add(builder3.build());
        setups.add(builder4.build());
        return setups;
    }
}

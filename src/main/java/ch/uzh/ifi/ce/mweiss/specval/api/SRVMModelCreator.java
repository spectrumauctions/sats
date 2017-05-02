/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.api;

import java.io.File;
import java.io.IOException;

import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.specval.model.srm.SRMBidderSetup;
import ch.uzh.ifi.ce.mweiss.specval.model.srm.SingleRegionModel;

/**
 * @author Michael Weiss
 *
 */
public class SRVMModelCreator extends ModelCreator {

    private final int numSmallBidders;
    private final int numHighFrequencyBidders;
    private final int numPrimaryBidders;
    private final int numSecondaryBidders;

    protected SRVMModelCreator(Builder builder) {
        super(builder);
        numSmallBidders = builder.numSmallBidders;
        numHighFrequencyBidders = builder.numHighFrequencyBidders;
        numPrimaryBidders = builder.numPrimaryBidders;
        numSecondaryBidders = builder.numSecondaryBidders;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.uzh.ifi.ce.mweiss.specval.api.ModelCreator#generateResult(java.io.File)
     */
    @Override
    public PathResult generateResult(File outputFolder)
            throws UnsupportedBiddingLanguageException, IOException, IllegalConfigException {
        SingleRegionModel model = new SingleRegionModel();
        model.setNumberOfHighFrequencyBidders(numHighFrequencyBidders);
        model.setNumberOfPrimaryBidders(numPrimaryBidders);
        model.setNumberOfSecondaryBidders(numSecondaryBidders);
        model.setNumberOfSmallBidders(numSmallBidders);
        return appendTopLevelParamsAndSolve(model, outputFolder);
    }

    public static class Builder extends ModelCreator.Builder {

        public int numSecondaryBidders;
        public int numPrimaryBidders;
        public int numHighFrequencyBidders;
        public int numSmallBidders;

        /**
         * @param lang
         */
        public Builder() {
            super(BiddingLanguage.SIZE_DECREASING);
            numSmallBidders = new SRMBidderSetup.SmallBidderBuilder().getNumberOfBidders();
            numPrimaryBidders  = new SRMBidderSetup.PrimaryBidderBuilder().getNumberOfBidders();
            numSecondaryBidders = new SRMBidderSetup.SecondaryBidderBuilder().getNumberOfBidders();
            numHighFrequencyBidders = new SRMBidderSetup.HighFrequenceBidderBuilder().getNumberOfBidders();
        }

        /*
         * (non-Javadoc)
         * 
         * @see ch.uzh.ifi.ce.mweiss.specval.api.ModelCreator.Builder#build()
         */
        @Override
        public SRVMModelCreator build() {
            return new SRVMModelCreator(this);
        }

        public void setNumSecondaryBidders(int numSecondaryBidders) {
            this.numSecondaryBidders = numSecondaryBidders;
        }

        public void setNumPrimaryBidders(int numPrimaryBidders) {
            this.numPrimaryBidders = numPrimaryBidders;
        }

        public void setNumHighFrequencyBidders(int numHighFrequencyBidders) {
            this.numHighFrequencyBidders = numHighFrequencyBidders;
        }

        public void setNumSmallBidders(int numSmallBidders) {
            this.numSmallBidders = numSmallBidders;
        }

        public int getNumSecondaryBidders() {
            return numSecondaryBidders;
        }

        public int getNumPrimaryBidders() {
            return numPrimaryBidders;
        }

        public int getNumHighFrequencyBidders() {
            return numHighFrequencyBidders;
        }

        public int getNumSmallBidders() {
            return numSmallBidders;
        }
        
        

    }

}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.api;

import java.io.File;
import java.io.IOException;

import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMGlobalBidderSetup;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMLocalBidderSetup;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMRegionalBidderSetup;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MultiRegionModel;

/**
 * @author Michael Weiss
 *
 */
public class MRVMModelCreator extends ModelCreator {

    private final int numberOfLocalBidders;
    private final int numberOfGlobalBidders;
    private final int numberOfRegionalBidders;
    
    private MRVMModelCreator(Builder builder) {
        super(builder);
        numberOfGlobalBidders = builder.numberOfGlobalBidders;
        numberOfLocalBidders = builder.numberOfLocalBidders;
        numberOfRegionalBidders = builder.numberOfRegionalBidders;
    }
    
    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.api.ModelCreator#generateResult(java.io.File)
     */
    @Override
    public PathResult generateResult(File outputFolder) throws UnsupportedBiddingLanguageException, IOException, IllegalConfigException {
        MultiRegionModel model = new MultiRegionModel();
        model.setNumberOfGlobalBidders(numberOfGlobalBidders);
        model.setNumberOfLocalBidders(numberOfLocalBidders);
        model.setNumberOfRegionalBidders(numberOfRegionalBidders);
        return appendTopLevelParamsAndSolve(model, outputFolder);
    }

    public static class Builder extends ModelCreator.Builder{

        private int numberOfLocalBidders;
        private int numberOfGlobalBidders;
        private int numberOfRegionalBidders;
        
        public Builder() {
            super(BiddingLanguage.SIZE_INCREASING);
            numberOfLocalBidders = new MRMLocalBidderSetup.Builder().getNumberOfBidders();
            numberOfGlobalBidders = new MRMGlobalBidderSetup.Builder().getNumberOfBidders();
            numberOfRegionalBidders = new MRMRegionalBidderSetup.Builder().getNumberOfBidders();
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.api.Config.Builder#build()
         */
        @Override
        public MRVMModelCreator build() {
            return new MRVMModelCreator(this);
        }
        
        public int getNumberOfLocalBidders() {
            return numberOfLocalBidders;
        }

        public void setNumberOfLocalBidders(int numberOfLocalBidders) {
            this.numberOfLocalBidders = numberOfLocalBidders;
        }

        public int getNumberOfGlobalBidders() {
            return numberOfGlobalBidders;
        }

        public void setNumberOfGlobalBidders(int numberOfGlobalBidders) {
            this.numberOfGlobalBidders = numberOfGlobalBidders;
        }

        public int getNumberOfRegionalBidders() {
            return numberOfRegionalBidders;
        }

        public void setNumberOfRegionalBidders(int numberOfRegionalBidders) {
            this.numberOfRegionalBidders = numberOfRegionalBidders;
        }
        
    }

    
}

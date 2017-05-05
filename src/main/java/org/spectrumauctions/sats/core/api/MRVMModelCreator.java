/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.api;

import java.io.File;
import java.io.IOException;

import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.core.model.mrvm.MRVMNationalBidderSetup;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLocalBidderSetup;

/**
 * @author Michael Weiss
 *
 */
public class MRVMModelCreator extends ModelCreator {

    private final int numberOfLocalBidders;
    private final int numberOfNationalBidders;
    private final int numberOfRegionalBidders;
    
    private MRVMModelCreator(Builder builder) {
        super(builder);
        numberOfNationalBidders = builder.numberOfNationalBidders;
        numberOfLocalBidders = builder.numberOfLocalBidders;
        numberOfRegionalBidders = builder.numberOfRegionalBidders;
    }
    
    /* (non-Javadoc)
     * @see ModelCreator#generateResult(java.io.File)
     */
    @Override
    public PathResult generateResult(File outputFolder) throws UnsupportedBiddingLanguageException, IOException, IllegalConfigException {
        MultiRegionModel model = new MultiRegionModel();
        model.setNumberOfNationalBidders(numberOfNationalBidders);
        model.setNumberOfLocalBidders(numberOfLocalBidders);
        model.setNumberOfRegionalBidders(numberOfRegionalBidders);
        return appendTopLevelParamsAndSolve(model, outputFolder);
    }

    public static class Builder extends ModelCreator.Builder{

        private int numberOfLocalBidders;
        private int numberOfNationalBidders;
        private int numberOfRegionalBidders;
        
        public Builder() {
            super();
            numberOfLocalBidders = new MRVMLocalBidderSetup.Builder().getNumberOfBidders();
            numberOfNationalBidders = new MRVMNationalBidderSetup.Builder().getNumberOfBidders();
            numberOfRegionalBidders = new MRVMRegionalBidderSetup.Builder().getNumberOfBidders();
        }

        /* (non-Javadoc)
         * @see org.spectrumauctions.sats.core.api.Config.Builder#build()
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

        public int getNumberOfNationalBidders() {
            return numberOfNationalBidders;
        }

        public void setNumberOfNationalBidders(int numberOfNationalBidders) {
            this.numberOfNationalBidders = numberOfNationalBidders;
        }

        public int getNumberOfRegionalBidders() {
            return numberOfRegionalBidders;
        }

        public void setNumberOfRegionalBidders(int numberOfRegionalBidders) {
            this.numberOfRegionalBidders = numberOfRegionalBidders;
        }
        
    }

    
}

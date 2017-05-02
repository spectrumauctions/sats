/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.api;

import java.io.File;
import java.io.IOException;

import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BaseValueModel;

/**
 * @author Michael Weiss
 *
 */
public final class BVMModelCreator extends ModelCreator{

    private final int numberOfBidders;
    /**
     * @param builder
     */
    public BVMModelCreator(Builder builder) {
        super(builder);
        numberOfBidders = builder.numberOfBidders;
    }
    

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.api.ModelCreator#generateResult(java.io.File)
     */
    @Override
    public PathResult generateResult(File outputFolder) throws UnsupportedBiddingLanguageException, IOException, IllegalConfigException {
        BaseValueModel model = new BaseValueModel();
        model.setNumberOfBidders(numberOfBidders);
        return appendTopLevelParamsAndSolve(model, outputFolder);
    }


    public final static class Builder extends ModelCreator.Builder{

        public int numberOfBidders;

        /**
         * @param lang
         */
        public Builder() {
            super(BiddingLanguage.SIZE_DECREASING);
            this.numberOfBidders = new BaseValueModel().getNumberOfBidders();
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.api.ModelCreator.Builder#build()
         */
        @Override
        public BVMModelCreator build() {
            return new BVMModelCreator(this);
        }

        public int getNumberOfBidders() {
            return numberOfBidders;
        }

        public void setNumberOfBidders(int numberOfBidders) {
            this.numberOfBidders = numberOfBidders;
        }
        
        
    }

}


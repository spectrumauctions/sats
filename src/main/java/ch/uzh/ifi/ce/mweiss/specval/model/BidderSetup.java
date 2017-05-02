/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model;

import com.google.common.base.Preconditions;

/**
 * @author Michael Weiss
 *
 */
public abstract class BidderSetup {

    protected final String setupName;
    protected final int numberOfBidders;
    
    protected BidderSetup(Builder builder){
        this.setupName = builder.setupName;
        this.numberOfBidders = builder.numberOfBidders;
    }
    
    /**
     * The setupName is used to identify the bidderSetup once the bidder is created.
     * For different setups, different setupNames should be used.
     * @return
     */
    public String getSetupName() {
        return setupName;
    }

    /**
     * The number bidders to be created with this bidderSetup
     * @return
     */
    public int getNumberOfBidders() {
        return numberOfBidders;
    }
  
    
    public abstract static class Builder{
        private String setupName;
        private int numberOfBidders;
        
        
        protected Builder(String setupName, int numberOfBidders){
            this.setupName = setupName;
            this.numberOfBidders = numberOfBidders;
        }
        
        /**
         * See {@link BidderSetup#getSetupName()} for the explanation of this parameter.
         */
        public String getSetupName() {
            return setupName;
        }

        /**
         * See {@link BidderSetup#getSetupName()} for the explanation of this parameter.
         */
        public void setSetupName(String setupName) {
            Preconditions.checkNotNull(setupName);
            this.setupName = setupName;
        }
        
        
        
        /**
         * See {@link BidderSetup#getNumberOfBidders()} for the explanation of this parameter.
         */
        public int getNumberOfBidders() {
            return numberOfBidders;
        }

        /**
         * See {@link BidderSetup#getNumberOfBidders()} for the explanation of this parameter.
         */
        public void setNumberOfBidders(int numberOfBidders) {
            Preconditions.checkArgument(numberOfBidders >= 0);
            this.numberOfBidders = numberOfBidders;
        }

        /**
         * Creates a new BidderSetup instance which can then be used to create a new population
         * @return
         */
        public abstract BidderSetup build();
    }
    
}

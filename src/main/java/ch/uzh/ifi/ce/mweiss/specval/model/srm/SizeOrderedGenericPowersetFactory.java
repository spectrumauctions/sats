/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.srm;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValueBidder;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrderedPowerset.GenericPowerset;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.Good;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;

/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedGenericPowersetFactory {

    public static GenericPowerset<SRMBand> getSizeOrderedGenericLang(boolean increasing, SRMBidder bidder) throws UnsupportedBiddingLanguageException{
        List<SRMBand> bands = new ArrayList<>(bidder.getWorld().getBands());
        if(increasing){
            return new Increasing(bands, bidder);
        }else{
            return new Decreasing(bands, bidder);
        }
    }
    
    private static final class Increasing extends GenericPowersetIncreasing<SRMBand>{

        private SRMBidder bidder;

        /**
         * @param genericDefinitions
         * @throws UnsupportedBiddingLanguageException 
         */
        protected Increasing(List<SRMBand> genericDefinitions, SRMBidder bidder) throws UnsupportedBiddingLanguageException {
            super(genericDefinitions);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.BiddingLanguage#getBidder()
         */
        @Override
        public SRMBidder getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrderedPowerset.GenericPowerset#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<SRMBand> getGenericBidder() {
            return bidder;
        }
        
    }
    
    private static final class Decreasing extends GenericPowersetDecreasing<SRMBand>{

        private SRMBidder bidder;

        /**
         * @param genericDefinitions
         * @throws UnsupportedBiddingLanguageException 
         */
        protected Decreasing(List<SRMBand> genericDefinitions, SRMBidder bidder) throws UnsupportedBiddingLanguageException {
            super(genericDefinitions);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.BiddingLanguage#getBidder()
         */
        @Override
        public Bidder<? extends Good> getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrderedPowerset.GenericPowerset#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<SRMBand> getGenericBidder() {
            return bidder;
        }
        
    }
}

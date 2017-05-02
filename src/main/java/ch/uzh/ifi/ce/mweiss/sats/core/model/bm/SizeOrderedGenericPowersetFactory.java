/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.model.bm;

import java.util.List;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.GenericValueBidder;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowerset;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Good;

/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedGenericPowersetFactory {

    public static GenericPowerset<BMBand> getSizeOrderedGenericLang(boolean increasing, BMBidder bidder) throws UnsupportedBiddingLanguageException {
        List<BMBand> bands = bidder.getWorld().getBands();
        if(increasing){
            return new Increasing(bands, bidder);
        }else{
            return new Decreasing(bands, bidder);
        }
    }
    
    private static final class Increasing extends GenericPowersetIncreasing<BMBand> {

        private BMBidder bidder;

        /**
         * @param genericDefinitions
         * @throws UnsupportedBiddingLanguageException 
         */
        protected Increasing(List<BMBand> genericDefinitions, BMBidder bidder) throws UnsupportedBiddingLanguageException {
            super(genericDefinitions);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see BiddingLanguage#getBidder()
         */
        @Override
        public BMBidder getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see GenericPowerset#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<BMBand> getGenericBidder() {
            return bidder;
        }
        
    }
    
    private static final class Decreasing extends GenericPowersetDecreasing<BMBand> {

        private BMBidder bidder;

        /**
         * @param genericDefinitions
         * @throws UnsupportedBiddingLanguageException 
         */
        protected Decreasing(List<BMBand> genericDefinitions, BMBidder bidder) throws UnsupportedBiddingLanguageException {
            super(genericDefinitions);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see BiddingLanguage#getBidder()
         */
        @Override
        public Bidder<? extends Good> getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see GenericPowerset#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<BMBand> getGenericBidder() {
            return bidder;
        }
        
    }
}

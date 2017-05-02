/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.srm;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValueBidder;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.FlatSizeIterators.GenericSizeOrdered;
import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;

/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedGenericFactory {

    static BandComparator comparator = new BandComparator();
    
    public static GenericSizeOrdered<SRMBand> getSizeOrderedGenericLang(boolean increasing, SRMBidder bidder) throws UnsupportedBiddingLanguageException{
        Set<SRMBand> bands = bidder.getWorld().getBands();
        if(increasing){
            return new Increasing(bands, bidder);
        }else{
            return new Decreasing(bands, bidder);
        }
    }
    
    
    private static final class Increasing extends GenericSizeIncreasing<SRMBand> {

        
        private final SRMBidder bidder;

        /**
         * @param allPossibleGenericDefintions
         * @throws UnsupportedBiddingLanguageException
         */
        protected Increasing(Collection<SRMBand> allPossibleGenericDefintions,SRMBidder bidder)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefintions);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.BiddingLanguage#getBidder()
         */
        @Override
        public Bidder<SRMLicense> getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrdered.GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<SRMBand> getGenericBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrdered.GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<SRMBand> getDefComparator() {
            return comparator;
        }
    }

    private static final class Decreasing extends GenericSizeDecreasing<SRMBand> {

        
        private final SRMBidder bidder;

        /**
         * @param allPossibleGenericDefintions
         * @throws UnsupportedBiddingLanguageException
         */
        protected Decreasing(Collection<SRMBand> allPossibleGenericDefintions, SRMBidder bidder)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefintions);
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
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrdered.GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<SRMBand> getGenericBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrdered.GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<SRMBand> getDefComparator() {
            return comparator;
        }
    }

    
    private static class BandComparator implements Comparator<SRMBand>{

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(SRMBand o1, SRMBand o2) {
            return o1.toString().compareTo(o2.toString());
        }
        
    }
    

}

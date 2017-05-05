/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeOrdered;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedGenericFactory {

    static BandComparator comparator = new BandComparator();
    
    public static GenericSizeOrdered<BMBand> getSizeOrderedGenericLang(boolean increasing, BMBidder bidder) throws UnsupportedBiddingLanguageException{
        List<BMBand> bands = bidder.getWorld().getBands();
        if(increasing){
            return new Increasing(bands, bidder);
        }else{
            return new Decreasing(bands, bidder);
        }
    }
    
    
    private static final class Increasing extends GenericSizeIncreasing<BMBand> {

        
        private final BMBidder bidder;

        /**
         * @param allPossibleGenericDefintions
         * @throws UnsupportedBiddingLanguageException
         */
        protected Increasing(Collection<BMBand> allPossibleGenericDefintions,BMBidder bidder)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefintions);
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
         * @see org.spectrumauctions.sats.core.bidlang.generic.SizeOrdered.GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<BMBand> getGenericBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see org.spectrumauctions.sats.core.bidlang.generic.SizeOrdered.GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<BMBand> getDefComparator() {
            return comparator;
        }
    }

    private static final class Decreasing extends GenericSizeDecreasing<BMBand> {

        
        private final BMBidder bidder;

        /**
         * @param allPossibleGenericDefintions
         * @throws UnsupportedBiddingLanguageException
         */
        protected Decreasing(Collection<BMBand> allPossibleGenericDefintions,BMBidder bidder)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefintions);
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
         * @see org.spectrumauctions.sats.core.bidlang.generic.SizeOrdered.GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<BMBand> getGenericBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see org.spectrumauctions.sats.core.bidlang.generic.SizeOrdered.GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<BMBand> getDefComparator() {
            return comparator;
        }
    }

    
    private static class BandComparator implements Comparator<BMBand>{

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(BMBand o1, BMBand o2) {
            return o1.getName().compareTo(o2.getName());
        }
        
    }
    

}

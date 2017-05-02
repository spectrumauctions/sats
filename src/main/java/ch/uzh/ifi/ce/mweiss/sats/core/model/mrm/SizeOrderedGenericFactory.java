/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.model.mrm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeOrdered;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.GenericValueBidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMRegionsMap.Region;

/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedGenericFactory {

    static BandComparator comparator = new BandComparator();
    
    public static GenericSizeOrdered<MRMGenericDefinition> getSizeOrderedGenericLang(boolean increasing, MRMBidder bidder) throws UnsupportedBiddingLanguageException {
        List<MRMGenericDefinition> bands = new ArrayList<>();
        for(MRMBand band : bidder.getWorld().getBands()){
            for(Region region : bidder.getWorld().getRegionsMap().getRegions()){
                bands.add(new MRMGenericDefinition(band, region));
            }
        }
        if(increasing){
            return new Increasing(bands, bidder);
        }else{
            return new Decreasing(bands, bidder);
        }
    }
    
    
    private static final class Increasing extends GenericSizeIncreasing<MRMGenericDefinition> {

        
        private final MRMBidder bidder;

        /**
         * @param allPossibleGenericDefintions
         * @throws UnsupportedBiddingLanguageException
         */
        protected Increasing(Collection<MRMGenericDefinition> allPossibleGenericDefintions,MRMBidder bidder)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefintions);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see BiddingLanguage#getBidder()
         */
        @Override
        public Bidder<MRMLicense> getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.satscore.bidlang.generic.SizeOrdered.GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<MRMGenericDefinition> getGenericBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.satscore.bidlang.generic.SizeOrdered.GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<MRMGenericDefinition> getDefComparator() {
            return comparator;
        }
    }

    private static final class Decreasing extends GenericSizeDecreasing<MRMGenericDefinition> {

        
        private final MRMBidder bidder;

        /**
         * @param allPossibleGenericDefintions
         * @throws UnsupportedBiddingLanguageException
         */
        protected Decreasing(Collection<MRMGenericDefinition> allPossibleGenericDefintions, MRMBidder bidder)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefintions);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see BiddingLanguage#getBidder()
         */
        @Override
        public Bidder<MRMLicense> getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.satscore.bidlang.generic.SizeOrdered.GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<MRMGenericDefinition> getGenericBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.satscore.bidlang.generic.SizeOrdered.GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<MRMGenericDefinition> getDefComparator() {
            return comparator;
        }
    }

    
    private static class BandComparator implements Comparator<MRMGenericDefinition>{

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(MRMGenericDefinition o1, MRMGenericDefinition o2) {
            return o1.toString().compareTo(o2.toString());
        }
        
    }
    

}

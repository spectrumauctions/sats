/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.model.mrm;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.GenericValueBidder;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowerset;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Good;
import ch.uzh.ifi.ce.mweiss.sats.core.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMRegionsMap.Region;

/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedGenericPowersetFactory {

    public static GenericPowerset<MRMGenericDefinition> getSizeOrderedGenericLang(boolean increasing, MRMBidder bidder) throws UnsupportedBiddingLanguageException{
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
    
    private static final class Increasing extends GenericPowersetIncreasing<MRMGenericDefinition>{

        private MRMBidder bidder;

        /**
         * @param genericDefinitions
         * @throws UnsupportedBiddingLanguageException 
         */
        protected Increasing(List<MRMGenericDefinition> genericDefinitions, MRMBidder bidder) throws UnsupportedBiddingLanguageException {
            super(genericDefinitions);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see BiddingLanguage#getBidder()
         */
        @Override
        public MRMBidder getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see GenericPowerset#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<MRMGenericDefinition> getGenericBidder() {
            return bidder;
        }
        
    }
    
    private static final class Decreasing extends GenericPowersetDecreasing<MRMGenericDefinition>{

        private MRMBidder bidder;

        /**
         * @param genericDefinitions
         * @throws UnsupportedBiddingLanguageException 
         */
        protected Decreasing(List<MRMGenericDefinition> genericDefinitions, MRMBidder bidder) throws UnsupportedBiddingLanguageException {
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
        protected GenericValueBidder<MRMGenericDefinition> getGenericBidder() {
            return bidder;
        }
        
    }
}

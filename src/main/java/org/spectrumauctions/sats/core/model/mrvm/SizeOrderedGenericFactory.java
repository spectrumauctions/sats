/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeOrdered;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedGenericFactory implements Serializable {

    private static final long serialVersionUID = 4716571861676046858L;
    static BandComparator comparator = new BandComparator();

    public static GenericSizeOrdered<MRVMGenericDefinition, MRVMLicense> getSizeOrderedGenericLang(boolean increasing, MRVMBidder bidder) throws UnsupportedBiddingLanguageException {
        List<MRVMGenericDefinition> bands = new ArrayList<>();
        for (MRVMBand band : bidder.getWorld().getBands()) {
            for (MRVMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                bands.add(new MRVMGenericDefinition(band, region));
            }
        }
        if (increasing) {
            return new Increasing(bands, bidder);
        } else {
            return new Decreasing(bands, bidder);
        }
    }


    private static final class Increasing extends GenericSizeIncreasing<MRVMGenericDefinition, MRVMLicense> {


        private final MRVMBidder bidder;

        protected Increasing(Collection<MRVMGenericDefinition> allPossibleGenericDefintions, MRVMBidder bidder)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefintions);
            this.bidder = bidder;
        }

        @Override
        public Bidder<MRVMLicense> getBidder() {
            return bidder;
        }

        /**
         * @see GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<MRVMGenericDefinition> getGenericBidder() {
            return bidder;
        }

        /**
         * @see GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<MRVMGenericDefinition> getDefComparator() {
            return comparator;
        }
    }

    private static final class Decreasing extends GenericSizeDecreasing<MRVMGenericDefinition, MRVMLicense> {


        private final MRVMBidder bidder;

        protected Decreasing(Collection<MRVMGenericDefinition> allPossibleGenericDefintions, MRVMBidder bidder)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefintions);
            this.bidder = bidder;
        }

        @Override
        public Bidder<MRVMLicense> getBidder() {
            return bidder;
        }

        /**
         * @see GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<MRVMGenericDefinition> getGenericBidder() {
            return bidder;
        }

        /**
         * @see GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<MRVMGenericDefinition> getDefComparator() {
            return comparator;
        }
    }


    private static class BandComparator implements Comparator<MRVMGenericDefinition>, Serializable {

        private static final long serialVersionUID = 6544631181558946919L;

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(MRVMGenericDefinition o1, MRVMGenericDefinition o2) {
            return o1.toString().compareTo(o2.toString());
        }

    }


}

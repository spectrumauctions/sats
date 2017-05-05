/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm;

import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.model.Good;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Fabio Isler
 */
public class RandomOrderXORQFactory {

    static BandComparator comparator = new BandComparator();

    public static XORQRandomOrderSimple<BMBand> getXORQRandomOrderSimpleLang(BMBidder bidder, RNGSupplier rngSupplier) throws UnsupportedBiddingLanguageException {
        List<BMBand> bands = bidder.getWorld().getBands();
        return new SimpleRandomOrder(bands, bidder, rngSupplier);
    }

    public static XORQRandomOrderSimple<BMBand> getXORQRandomOrderSimpleLang(BMBidder bidder) throws UnsupportedBiddingLanguageException {
        List<BMBand> bands = bidder.getWorld().getBands();
        return new SimpleRandomOrder(bands, bidder, new JavaUtilRNGSupplier());
    }


    private static final class SimpleRandomOrder extends XORQRandomOrderSimple<BMBand> {


        private final BMBidder bidder;

        /**
         * @param allPossibleGenericDefintions
         * @throws UnsupportedBiddingLanguageException
         */
        SimpleRandomOrder(Collection<BMBand> allPossibleGenericDefintions, BMBidder bidder, RNGSupplier rngSupplier)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefintions, rngSupplier);
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

    private static class BandComparator implements Comparator<BMBand> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(BMBand o1, BMBand o2) {
            return o1.getName().compareTo(o2.getName());
        }

    }


}

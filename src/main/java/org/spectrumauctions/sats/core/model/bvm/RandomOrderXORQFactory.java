/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm;

import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeOrdered;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Fabio Isler
 */
public class RandomOrderXORQFactory implements Serializable {

    private static final long serialVersionUID = 3752749595977909372L;

    public static XORQRandomOrderSimple getXORQRandomOrderSimpleLang(BMBidder bidder, RNGSupplier rngSupplier) throws UnsupportedBiddingLanguageException {
        List<BMBand> bands = bidder.getWorld().getBands();
        return new SimpleRandomOrder(bands, bidder, rngSupplier);
    }

    public static XORQRandomOrderSimple getXORQRandomOrderSimpleLang(BMBidder bidder) throws UnsupportedBiddingLanguageException {
        List<BMBand> bands = bidder.getWorld().getBands();
        return new SimpleRandomOrder(bands, bidder, new JavaUtilRNGSupplier());
    }


    private static final class SimpleRandomOrder extends XORQRandomOrderSimple {

        private final BMBidder bidder;

        SimpleRandomOrder(List<BMBand> allPossibleGenericDefinitions, BMBidder bidder, RNGSupplier rngSupplier)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefinitions, rngSupplier);
            this.bidder = bidder;
        }

        @Override
        public SATSBidder getBidder() {
            return bidder;
        }

    }


}

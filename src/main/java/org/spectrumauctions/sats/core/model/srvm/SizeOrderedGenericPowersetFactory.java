/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.srvm;

import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowerset;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedGenericPowersetFactory {

    public static GenericPowerset<SRVMBand> getSizeOrderedGenericLang(boolean increasing, SRVMBidder bidder) throws UnsupportedBiddingLanguageException {
        List<SRVMBand> bands = new ArrayList<>(bidder.getWorld().getBands());
        if (increasing) {
            return new Increasing(bands, bidder);
        } else {
            return new Decreasing(bands, bidder);
        }
    }

    private static final class Increasing extends GenericPowersetIncreasing<SRVMBand> {

        private SRVMBidder bidder;

        protected Increasing(List<SRVMBand> genericDefinitions, SRVMBidder bidder) throws UnsupportedBiddingLanguageException {
            super(genericDefinitions);
            this.bidder = bidder;
        }

        @Override
        public SRVMBidder getBidder() {
            return bidder;
        }

        /**
         * @see GenericPowerset#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<SRVMBand> getGenericBidder() {
            return bidder;
        }

    }

    private static final class Decreasing extends GenericPowersetDecreasing<SRVMBand> {

        private SRVMBidder bidder;

        protected Decreasing(List<SRVMBand> genericDefinitions, SRVMBidder bidder) throws UnsupportedBiddingLanguageException {
            super(genericDefinitions);
            this.bidder = bidder;
        }

        @Override
        public Bidder<? extends Good> getBidder() {
            return bidder;
        }

        /**
         * @see GenericPowerset#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<SRVMBand> getGenericBidder() {
            return bidder;
        }

    }
}

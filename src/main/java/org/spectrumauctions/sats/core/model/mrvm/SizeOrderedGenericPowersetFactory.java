/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

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

    public static GenericPowerset<MRVMGenericDefinition> getSizeOrderedGenericLang(boolean increasing, MRVMBidder bidder) throws UnsupportedBiddingLanguageException {
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

    private static final class Increasing extends GenericPowersetIncreasing<MRVMGenericDefinition> {

        private MRVMBidder bidder;

        protected Increasing(List<MRVMGenericDefinition> genericDefinitions, MRVMBidder bidder) throws UnsupportedBiddingLanguageException {
            super(genericDefinitions);
            this.bidder = bidder;
        }

        @Override
        public MRVMBidder getBidder() {
            return bidder;
        }

        /**
         * @see GenericPowerset#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<MRVMGenericDefinition> getGenericBidder() {
            return bidder;
        }

    }

    private static final class Decreasing extends GenericPowersetDecreasing<MRVMGenericDefinition> {

        private MRVMBidder bidder;

        protected Decreasing(List<MRVMGenericDefinition> genericDefinitions, MRVMBidder bidder) throws UnsupportedBiddingLanguageException {
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
        protected GenericValueBidder<MRVMGenericDefinition> getGenericBidder() {
            return bidder;
        }

    }
}

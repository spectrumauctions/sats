/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm;

import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeOrdered;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.bvm.bvm.BaseValueModel;

import java.util.Iterator;

/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedIteratorTest {

    @Test
    public void testDecreasingIterator() throws UnsupportedBiddingLanguageException {
        BaseValueModel model = new BaseValueModel();
        BMBidder bidder = model.createNewPopulation(51465435L).iterator().next();
        @SuppressWarnings("unchecked")
        GenericSizeOrdered<BMBand, BMLicense> lang = bidder.getValueFunction(GenericSizeDecreasing.class, 351354);
        int currentSize = Integer.MAX_VALUE;
        int iteration = 0;
        Iterator<GenericValue<BMBand, BMLicense>> iter = lang.iterator();
        while (iter.hasNext()) {
            int quantity = 0;
            GenericValue<BMBand, BMLicense> val = iter.next();
            for (BMBand band : bidder.getWorld().getBands()) {
                quantity += val.getQuantity(band);
            }
            Assert.assertTrue("non-decreasing in iteration " + iteration, quantity <= currentSize);
            currentSize = quantity;
            iteration++;
        }
    }

    @Test
    public void testIncreasingIterator() throws UnsupportedBiddingLanguageException {
        BaseValueModel model = new BaseValueModel();
        BMBidder bidder = model.createNewPopulation(51465435L).iterator().next();
        @SuppressWarnings("unchecked")
        GenericSizeOrdered<BMBand, BMLicense> lang = bidder.getValueFunction(GenericSizeIncreasing.class, 351354);
        int currentSize = 0;
        int iteration = 0;
        Iterator<GenericValue<BMBand, BMLicense>> iter = lang.iterator();
        while (iter.hasNext()) {
            int quantity = 0;
            GenericValue<BMBand, BMLicense> val = iter.next();
            for (BMBand band : bidder.getWorld().getBands()) {
                quantity += val.getQuantity(band);
            }
            Assert.assertTrue("non-decreasing in iteration " + iteration, quantity >= currentSize);
            currentSize = quantity;
            iteration++;
        }
    }
}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.srvm;

import org.junit.Test;
import org.marketdesignresearch.mechlib.core.bidder.valuefunction.BundleValue;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.util.Iterator;

/**
 * @author Michael Weiss
 *
 */
public class SRVMTest {

    //TODO RealTesting

    @Test
    public void testNoRuntimeException() throws UnsupportedBiddingLanguageException {
        SingleRegionModel model = new SingleRegionModel();
        SRVMBidder bidder = model.createNewPopulation(238472).iterator().next();
        BiddingLanguage lang = bidder.getValueFunction(GenericSizeDecreasing.class);
        Iterator<BundleValue> iter = lang.iterator();
        for (int i = 0; i < 50 && iter.hasNext(); i++) {
            iter.next();
        }
    }
}

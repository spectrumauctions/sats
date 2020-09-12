/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.generic;

import org.junit.Test;
import org.marketdesignresearch.mechlib.core.bidder.valuefunction.BundleValue;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.bvm.BMBidder;
import org.spectrumauctions.sats.core.model.bvm.mbvm.MultiBandValueModel;

import java.util.Iterator;

/**
 * @author Michael Weiss
 *
 */
public class XORQtoXORTest {

    @Test
    public void testAllCombinations() throws UnsupportedBiddingLanguageException {
        MultiBandValueModel model = new MultiBandValueModel();
        BMBidder bidder = model.createNewWorldAndPopulation(51465435L).iterator().next();
        @SuppressWarnings("unchecked")
        GenericPowersetDecreasing lang =
                bidder.getValueFunction(GenericPowersetDecreasing.class, 351354);
        Iterator<BundleValue> xorqIter = lang.iterator();
        boolean didIter = false;
        int count = 0;
        while (xorqIter.hasNext()) {
            if (count++ > 300) {
                break;
            } //Don't test the full powerset.
            BundleValue xorq = xorqIter.next();
            // TODO
            // Iterator<BundleValue> xorIter = xorq.plainXorIterator();
            // while (xorIter.hasNext()) {
            //     didIter = true;
            //     XORValue<BMLicense> xor = xorIter.next();
            //     Assert.assertEquals(xorq.getSize(), xor.getLicenses().size());
            //     for (BMBand band : bidder.getWorld().getBands()) {
            //         int expectedNumberOfLicenses = xorq.getQuantity(band);
            //         int actual = 0;
            //         for (BMLicense license : xor.getLicenses()) {
            //             if (band.isPartOf(license)) {
            //                 actual++;
            //             }
            //         }
            //         Assert.assertEquals(expectedNumberOfLicenses, actual);
            //     }
            // }
        }
        // Assert.assertTrue(didIter);

    }

}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.generic;

import java.util.Iterator;

import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.bvm.BMBand;
import org.spectrumauctions.sats.core.model.bvm.BMBidder;
import org.junit.Assert;
import org.junit.Test;

import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import org.spectrumauctions.sats.core.model.bvm.mbvm.MultiBandValueModel;

/**
 * @author Michael Weiss
 *
 */
public class XORQtoXORTest {

    @Test
    public void testAllCombinations() throws UnsupportedBiddingLanguageException {
        MultiBandValueModel model = new MultiBandValueModel();
        BMBidder bidder = model.createNewPopulation(51465435L).iterator().next();
        @SuppressWarnings("unchecked")
        GenericPowersetDecreasing<BMBand> lang =
                bidder.getValueFunction(GenericPowersetDecreasing.class, 351354);
        Iterator<GenericValue<BMBand>> xorqIter = lang.iterator();
        boolean didIter = false;
        int count =0;
        while(xorqIter.hasNext()){
            if(count++ > 300){break;} //Don't test the full powerset.
            GenericValue<BMBand> xorq = xorqIter.next();
            Iterator<XORValue<?>> xorIter = xorq.plainXorIterator();
            while(xorIter.hasNext()){
                didIter = true;
                XORValue<?> xor = xorIter.next();
                Assert.assertEquals(xorq.getSize(), xor.getLicenses().size());
                for(BMBand band :  bidder.getWorld().getBands()){
                    int expectedNumberOfLicenses = xorq.getQuantity(band);
                    int actual =0;
                    for(Good license : xor.getLicenses()){
                        if(band.isPartOf(license)){
                            actual++;
                        }
                    }
                    Assert.assertEquals(expectedNumberOfLicenses, actual);
                }
            }
        }
        Assert.assertTrue(didIter);
  
    }
    
}

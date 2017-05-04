/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm;

import java.util.Iterator;

import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.junit.Assert;
import org.junit.Test;

import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import org.spectrumauctions.sats.core.model.bvm.bvm.BaseValueModel;
import org.spectrumauctions.sats.core.model.bvm.mbvm.MultiBandValueModel;

/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedPowersetTest {

    @Test
    public void testDecreasingIterator() throws UnsupportedBiddingLanguageException {
        MultiBandValueModel model = new MultiBandValueModel();
        BMBidder bidder = model.createNewPopulation(51465435L).iterator().next();
        @SuppressWarnings("unchecked")
        GenericPowersetDecreasing<BMBand> lang = bidder.getValueFunction(GenericPowersetDecreasing.class, 351354);
        int currentSize = Integer.MAX_VALUE;
        int iteration = 0;
        Iterator<GenericValue<BMBand>> iter = lang.iterator();
        while(iter.hasNext()){
            int quantity = 0;
            GenericValue<BMBand> val = iter.next();
            for(BMBand band : bidder.getWorld().getBands() ){
                quantity += val.getQuantity(band);
            }
            Assert.assertTrue("non-decreasing in iteration " + iteration, quantity <= currentSize);
            currentSize = quantity;
            iteration++;
            
        }
        Assert.assertEquals(2400, iteration);
    }
    
    @Test
    public void testIncreasingIterator() throws UnsupportedBiddingLanguageException{
        BaseValueModel model = new BaseValueModel();
        BMBidder bidder = model.createNewPopulation(51465435L).iterator().next();
        @SuppressWarnings("unchecked")
        GenericPowersetIncreasing<BMBand> lang = bidder.getValueFunction(GenericPowersetIncreasing.class, 351354);
        int currentSize = 0;
        int iteration = 0;
        Iterator<GenericValue<BMBand>> iter = lang.iterator();
        while(iter.hasNext()){
            int quantity = 0;
            GenericValue<BMBand> val = iter.next();
            for(BMBand band : bidder.getWorld().getBands() ){
                quantity += val.getQuantity(band);
            }
            Assert.assertTrue("non-decreasing in iteration " + iteration, quantity >= currentSize);
            currentSize = quantity;
            iteration++;
        }
        Assert.assertEquals((15*11)-1, iteration);
    }
}

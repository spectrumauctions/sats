/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.bm;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValue;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.FlatSizeIterators.GenericSizeOrdered;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BaseValueModel;

/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedIteratorTest {

    @Test
    public void testDecreasingIterator() throws UnsupportedBiddingLanguageException{
        BaseValueModel model = new BaseValueModel();
        BMBidder bidder = model.createNewPopulation(51465435L).iterator().next();
        @SuppressWarnings("unchecked")
        GenericSizeOrdered<BMBand> lang = bidder.getValueFunction(GenericSizeDecreasing.class, 351354);
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
    }
    
    @Test
    public void testIncreasingIterator() throws UnsupportedBiddingLanguageException{
        BaseValueModel model = new BaseValueModel();
        BMBidder bidder = model.createNewPopulation(51465435L).iterator().next();
        @SuppressWarnings("unchecked")
        GenericSizeOrdered<BMBand> lang = bidder.getValueFunction(GenericSizeIncreasing.class, 351354);
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
    }
}

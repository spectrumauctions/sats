/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrderedPowerset;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericLang;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMBidder;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MultiRegionModel;

/**
 * @author Michael Weiss
 *
 */
public class GenericPowersetTest {

    @Test
    @Ignore //TODO Re-Accept Once MRVM Model is Full Size Again
    public void testLargeAuctionMustNotStart() throws UnsupportedBiddingLanguageException{
        MultiRegionModel model = new MultiRegionModel();
        MRMBidder bidder = model.createNewPopulation(89127349).iterator().next();
        try{
            GenericLang<?> lang = bidder.getValueFunction(GenericPowersetDecreasing.class);
            Assert.fail();
        }catch (UnsupportedBiddingLanguageException e) {
            //Passed
        }
        
        
    }
}

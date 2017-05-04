/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.srvm;

import java.util.Iterator;

import org.spectrumauctions.sats.core.bidlang.generic.GenericLang;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.junit.Test;

import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;

/**
 * @author Michael Weiss
 *
 */
public class SRMTest {

    //TODO RealTesting
    
    @Test
    public void testNoRunimeException() throws UnsupportedBiddingLanguageException {
        SingleRegionModel model = new SingleRegionModel();
        SRMBidder bidder = model.createNewPopulation(238472).iterator().next();
        GenericLang<SRMBand> lang = bidder.getValueFunction(GenericSizeDecreasing.class);
        Iterator<GenericValue<SRMBand>> iter = lang.iterator();
        for(int i = 0; i < 50 && iter.hasNext(); i++){
            iter.next();
        }
    }
}

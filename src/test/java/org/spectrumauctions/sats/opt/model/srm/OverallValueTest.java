/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.srm;

import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.srm.SRMBand;
import org.spectrumauctions.sats.core.model.srm.SRMBidder;
import org.spectrumauctions.sats.core.model.srm.SingleRegionModel;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Fabio Isler
 */
public class OverallValueTest {

    @Test
    public void mipValuesEqualSATSValues() {
        List<SRMBidder> bidders = new SingleRegionModel().createNewPopulation();
        SRM_MIP mip = new SRM_MIP(bidders);
        SRMMipResult result = mip.calculateAllocation();
        for (SRMBidder bidder : bidders) {
            GenericValue<SRMBand> outcomeVal = result.getAllocation(bidder);
            BigDecimal satsVal = bidder.calculateValue(outcomeVal.getQuantities());
            Assert.assertEquals(satsVal.doubleValue(), outcomeVal.getValue().doubleValue(), 0.1);
        }
    }
}

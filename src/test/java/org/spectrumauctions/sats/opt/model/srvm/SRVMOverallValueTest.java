/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.srvm;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.srvm.SRVMBand;
import org.spectrumauctions.sats.core.model.srvm.SRVMBidder;
import org.spectrumauctions.sats.core.model.srvm.SingleRegionModel;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Fabio Isler
 */
public class SRVMOverallValueTest {

    @Test
    @Ignore // TODO: There's still a slight difference between the values from sats-core and sats-opt
    public void mipValuesEqualSATSValues() {
        List<SRVMBidder> bidders = new SingleRegionModel().createNewPopulation();
        SRVM_MIP mip = new SRVM_MIP(bidders);
        SRVMMipResult result = mip.calculateAllocation();
        for (SRVMBidder bidder : bidders) {
            GenericValue<SRVMBand> outcomeVal = result.getAllocation(bidder);
            BigDecimal satsVal = bidder.calculateValue(outcomeVal.getQuantities());
            Assert.assertEquals(satsVal.doubleValue(), outcomeVal.getValue().doubleValue(), 0.1);
        }
    }
}

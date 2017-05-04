/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.mrvm;

import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.mrvm.*;

import java.util.Collection;

/**
 * @author Michael Weiss
 *
 */
public class MipTest {

    @Test
    public void testNoException() {
        Collection<MRVMBidder> bidders = (new MultiRegionModel()).createNewPopulation();
        MRVM_MIP mip = new MRVM_MIP(bidders);
        MipResult result = mip.calculateAllocation();
        for (MRVMBidder bidder : bidders) {
            GenericValue<MRVMGenericDefinition> genVal = result.getAllocation(bidder);
            for (MRVMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                for (MRVMBand band : bidder.getWorld().getBands()) {
                    MRVMGenericDefinition def = new MRVMGenericDefinition(band, region);
                    Integer quantity = genVal.getQuantity(def);
                    System.out.println(new StringBuilder("bidder ").append(bidder.getId()).append("\t").append(def.toString()).append("\t").append(quantity));
                }
            }

        }
        System.out.println(result.getTotalValue());
        System.out.println();
    }

}

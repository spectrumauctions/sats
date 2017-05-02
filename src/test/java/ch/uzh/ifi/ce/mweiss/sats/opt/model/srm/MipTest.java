/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.opt.model.srm;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.GenericValue;
import ch.uzh.ifi.ce.mweiss.sats.core.model.srm.*;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.JavaUtilRNGSupplier;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabio Isler
 *
 */
public class MipTest {

    @Test
    public void testNoException() {
        Collection<SRMBidder> bidders = (new SingleRegionModel()).createNewPopulation();
        SRM_MIP mip = new SRM_MIP(bidders);
        SRMMipResult result = mip.calculateAllocation();
        for (SRMBidder bidder : bidders) {
            GenericValue<SRMBand> genVal = result.getAllocation(bidder);
            for (SRMBand band : bidder.getWorld().getBands()) {
                Integer quantity = genVal.getQuantity(band);
                System.out.println(new StringBuilder("bidder ").append(bidder.getId()).append("\t").append(band.toString()).append("\t").append(quantity));
            }
        }
        System.out.println(result.getTotalValue());
        System.out.println();
    }

    @Test
    public void testMinimalNoException() {
        test(1, 1);
    }

    @Test
    public void test5NoException() {
        test(5, 5);
    }

    public void test(int numberOfSmallBidders, int numberOfHighfrequencyBidders) {
        SRMWorld world = new SRMWorld(SRMWorldGen.getSingleBandWorldSetup(), new JavaUtilRNGSupplier(147258369L));
        Set<SRMBidderSetup> setups = new HashSet<>();
        setups.addAll(SRMWorldGen.getSimpleSmallBidderSetup(numberOfSmallBidders));
        setups.addAll(SRMWorldGen.getSimpleHighFrequencyBidderSetup(numberOfHighfrequencyBidders));
        Collection<SRMBidder> bidders = world.createPopulation(setups, new JavaUtilRNGSupplier(963852741L));
        SRM_MIP mip = new SRM_MIP(bidders);
        SRMMipResult result = mip.calculateAllocation();
        for (SRMBidder bidder : bidders) {
            GenericValue<SRMBand> genVal = result.getAllocation(bidder);
            for (SRMBand band : bidder.getWorld().getBands()) {
                Integer quantity = genVal.getQuantity(band);
                System.out.println(new StringBuilder("bidder ").append(bidder.getId()).append("\t").append(band.toString()).append("\t").append(quantity));
            }
        }
        System.out.println(result.getTotalValue());
        System.out.println();
    }

}

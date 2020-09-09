/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.srvm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.marketdesignresearch.mechlib.core.Allocation;
import org.marketdesignresearch.mechlib.core.Bundle;
import org.spectrumauctions.sats.core.model.srvm.*;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabio Isler
 */
public class SRVMMipTest {

    private static final Logger logger = LogManager.getLogger(SRVMMipTest.class);

    @Test
    public void testNoException() {
        Collection<SRVMBidder> bidders = (new SingleRegionModel()).createNewWorldAndPopulation();
        SRVM_MIP mip = new SRVM_MIP(bidders);
        Allocation result = mip.getAllocation();
        for (SRVMBidder bidder : bidders) {
            Bundle bundle = result.allocationOf(bidder).getBundle();
            for (SRVMBand band : bidder.getWorld().getBands()) {
                Integer quantity = bundle.countGood(band);
                logger.info(new StringBuilder("bidder ").append(bidder.getLongId()).append("\t").append(band.toString()).append("\t").append(quantity));
            }
        }
        logger.info("Total value:" + result.getTotalAllocationValue());
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
        SRVMWorld world = new SRVMWorld(SRVMWorldGen.getSingleBandWorldSetup(), new JavaUtilRNGSupplier(147258369L));
        Set<SRVMBidderSetup> setups = new HashSet<>();
        setups.addAll(SRVMWorldGen.getSimpleSmallBidderSetup(numberOfSmallBidders));
        setups.addAll(SRVMWorldGen.getSimpleHighFrequencyBidderSetup(numberOfHighfrequencyBidders));
        Collection<SRVMBidder> bidders = world.createPopulation(setups, new JavaUtilRNGSupplier(963852741L));
        SRVM_MIP mip = new SRVM_MIP(bidders);
        Allocation result = mip.getAllocation();
        for (SRVMBidder bidder : bidders) {
            Bundle bundle = result.allocationOf(bidder).getBundle();
            for (SRVMBand band : bidder.getWorld().getBands()) {
                Integer quantity = bundle.countGood(band);
                logger.info(new StringBuilder("bidder ").append(bidder.getLongId()).append("\t").append(band.toString()).append("\t").append(quantity));
            }
        }
        logger.info("Total value:" + result.getTotalAllocationValue());
    }

}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.marketdesignresearch.mechlib.domain.Bundle;
import org.spectrumauctions.sats.core.model.bvm.bvm.BVMBidderSetup;
import org.spectrumauctions.sats.core.model.bvm.bvm.BVMWorldSetup;
import org.spectrumauctions.sats.core.model.bvm.bvm.BaseValueModel;
import org.spectrumauctions.sats.core.model.bvm.mbvm.MBVMBidderSetup;
import org.spectrumauctions.sats.core.model.bvm.mbvm.MBVMWorldSetup;
import org.spectrumauctions.sats.core.model.bvm.mbvm.MultiBandValueModel;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Michael Weiss
 *
 */
@RunWith(Parameterized.class)
public class BMValueTest {

    private static final String SETUP_BVM = "Deterministic BVM Test Bidder";
    private static final String SETUP_MBVM = "Deterministic MBVM Test Bidder";

    private final BMBidder bidder;
    private final String setup;
    /**
     * Value for a bundle containing all licenses of this world
     */
    private final BigDecimal expextedValuedCompleteBundle;
    /**
     * Value for a bundle containing exactly half of the licenses available in each band
     */
    private final BigDecimal expectedValueHalfBundle;
    /**
     * Value for a bundle containing 2 licenses of band A and 1 license of all other bands
     */
    private final BigDecimal expectedValueSmallBundle;

    private final BigDecimal expectedValueEmptyBundle = BigDecimal.ZERO;

    public BMValueTest(BMBidderSetup bidderSetup,
                       BigDecimal expectedValueCompleteBundle,
                       BigDecimal expectedValueHalfBundle,
                       BigDecimal expectedValueSmallBundle) {
        super();
        this.setup = bidderSetup.getSetupName();
        if (setup.equals(SETUP_BVM)) {
            //NOTE BM World are created deterministically anyways
            bidder = new BaseValueModel().createWorld().createPopulation(bidderSetup).iterator().next();
        } else {
            bidder = new MultiBandValueModel().createWorld().createPopulation(bidderSetup).iterator().next();
        }
        this.expextedValuedCompleteBundle = expectedValueCompleteBundle;
        this.expectedValueSmallBundle = expectedValueSmallBundle;
        this.expectedValueHalfBundle = expectedValueHalfBundle;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> models() {
        List<Object[]> testInput = new ArrayList<>();
        testInput.add(new Object[]{
                deterministicBVMBidderSetup(),
                new BigDecimal("1464"),
                new BigDecimal("1404"),
                new BigDecimal("348")
        });
        testInput.add(new Object[]{
                deterministicMBVMBidderSetup(),
                new BigDecimal("2550"),
                new BigDecimal("1530"),
                new BigDecimal("560")
        });
        return testInput;
    }

    /**
     * @return a deterministic (i.e., non random) bvm bidder builder consistent with the {@link BVMWorldSetup}
     */
    private static BMBidderSetup deterministicBVMBidderSetup() {
        BMBidderSetup.BMBidderSetupBuilder builder = new BVMBidderSetup.BVMBidderSetupBuilder(SETUP_BVM, 1);
        builder.putBaseValueInterval(BVMWorldSetup.BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_A, new DoubleInterval(120));
        builder.putBaseValueInterval(BVMWorldSetup.BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_B, new DoubleInterval(60));
        return builder.build();
    }

    /**
     * @return a deterministic (i.e., non random) bvm bidder builder consistent with the {@link MBVMWorldSetup}
     */
    private static BMBidderSetup deterministicMBVMBidderSetup() {
        BMBidderSetup.BMBidderSetupBuilder builder = new MBVMBidderSetup.MBVMBidderSetupBuilder(SETUP_MBVM, 1);
        builder.putBaseValueInterval(MBVMWorldSetup.MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_A, new DoubleInterval(100));
        builder.putBaseValueInterval(MBVMWorldSetup.MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_B, new DoubleInterval(90));
        builder.putBaseValueInterval(MBVMWorldSetup.MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_C, new DoubleInterval(80));
        builder.putBaseValueInterval(MBVMWorldSetup.MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_D, new DoubleInterval(70));
        return builder.build();
    }


    @Test
    public void valueOfCompleteBundle() {
        Bundle bundle = Bundle.of(bidder.getWorld().getLicenses());
        BigDecimal value = bidder.calculateValue(bundle);
        Assert.assertEquals(bidder.getSetupType() + " value was " + value + " should be " + expextedValuedCompleteBundle, 0, value.compareTo(expextedValuedCompleteBundle));
    }

    @Test
    public void valueOfHalfBundle() {
        Set<BMLicense> licenses = new HashSet<>();
        for (BMBand band : bidder.getWorld().getBands()) {
            Iterator<BMLicense> licenseIter = band.containedGoods().iterator();
            for (int i = 0; i < band.available() / 2; i++) {
                licenses.add(licenseIter.next());
            }
        }
        Bundle bundle = Bundle.of(licenses);
        BigDecimal value = bidder.calculateValue(bundle);
        Assert.assertEquals(bidder.getSetupType() + " value was " + value + " should be " + expectedValueHalfBundle, 0, value.compareTo(expectedValueHalfBundle));
    }

    @Test
    public void valueOfSmallBundle() {
        Set<BMLicense> licenses = new HashSet<>();
        for (BMBand band : bidder.getWorld().getBands()) {
            Iterator<BMLicense> licenseIter = band.containedGoods().iterator();
            licenses.add(licenseIter.next());
            //Names for default A band is the same in both models, hence only compare to one of them
            if (band.getName().equals(BVMWorldSetup.BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_A)) {
                //Add a second license of band A
                licenses.add(licenseIter.next());
            }
        }
        Bundle bundle = Bundle.of(licenses);
        BigDecimal value = bidder.calculateValue(bundle);
        Assert.assertEquals(bidder.getSetupType() + " value was " + value + " should be " + expectedValueSmallBundle, 0, value.compareTo(expectedValueSmallBundle));
    }

    @Test
    public void valueOfEmptyBundle() {
        BigDecimal value = bidder.calculateValue(Bundle.EMPTY);
        Assert.assertEquals(bidder.getSetupType() + " value was " + value + " should be " + expectedValueEmptyBundle, 0, value.compareTo(expectedValueEmptyBundle));
    }

}

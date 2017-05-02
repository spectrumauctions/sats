/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.bm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import ch.uzh.ifi.ce.mweiss.specval.model.Bundle;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.BMBidderSetup.BMBidderSetupBuilder;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BVMBidderSetup.BVMBidderSetupBuilder;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BVMWorldSetup;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BVMWorldSetup.BVMWorldSetupBuilder;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BaseValueModel;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.mbvm.MBVMBidderSetup.MBVMBidderSetupBuilder;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.mbvm.MBVMWorldSetup;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.mbvm.MBVMWorldSetup.MBVMWorldSetupBuilder;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.mbvm.MultiBandValueModel;
import ch.uzh.ifi.ce.mweiss.specval.util.random.DoubleInterval;

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
        if(setup.equals(SETUP_BVM)){
            //NOTE BM World are created deterministically anyways
            bidder = new BaseValueModel().createWorld().createPopulation(bidderSetup).iterator().next();
        }else{
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
     * Create a deterministic (i.e., non random) bm bidder builder consistent with the {@link BVMWorldSetup}
     * @return
     */
    private static BMBidderSetup deterministicBVMBidderSetup(){
        BMBidderSetupBuilder builder = new BVMBidderSetupBuilder(SETUP_BVM, 1);
        builder.putBaseValueInterval(BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_A, new DoubleInterval(120));
        builder.putBaseValueInterval(BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_B, new DoubleInterval(60));
        return builder.build();
    }
    
    /**
     * Create a deterministic (i.e., non random) bm bidder builder consistent with the {@link MBVMWorldSetup}
     * @return
     */
    private static BMBidderSetup deterministicMBVMBidderSetup(){
        BMBidderSetupBuilder builder = new MBVMBidderSetupBuilder(SETUP_MBVM, 1);
        builder.putBaseValueInterval(MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_A, new DoubleInterval(100));
        builder.putBaseValueInterval(MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_B, new DoubleInterval(90));
        builder.putBaseValueInterval(MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_C, new DoubleInterval(80));
        builder.putBaseValueInterval(MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_D, new DoubleInterval(70));
        return builder.build();
    }
    
    
    @Test
    public void valueOfCompleteBundle(){
        Bundle<BMLicense> bundle = new Bundle<>(bidder.getWorld().getLicenses());
        BigDecimal value = bidder.calculateValue(bundle);
        Assert.assertTrue(bidder.getSetupType() + " value was " + value + " should be " + expextedValuedCompleteBundle, value.compareTo(expextedValuedCompleteBundle)==0);
    }
    
    @Test
    public void valueOfHalfBundle(){
        Bundle<BMLicense> bundle = new Bundle<>();
        for(BMBand band : bidder.getWorld().getBands()){
            Iterator<BMLicense> licenseIter = band.getLicenses().iterator();
            for(int i = 0; i< band.getNumberOfLicenses()/2; i++){
                bundle.add(licenseIter.next());
            }              
        }
        BigDecimal value = bidder.calculateValue(bundle);
        Assert.assertTrue(bidder.getSetupType() + " value was " + value + " should be " + expectedValueHalfBundle, value.compareTo(expectedValueHalfBundle)==0);
    }
    
    @Test
    public void valueOfSmallBundle(){
        Bundle<BMLicense> bundle = new Bundle<>();
        for(BMBand band : bidder.getWorld().getBands()){
            Iterator<BMLicense> licenseIter = band.getLicenses().iterator();
            bundle.add(licenseIter.next());
            //Names for default A band is the same in both models, hence only compare to one of them
            if(band.getName().equals(BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_A)){
                //Add a second license of band A
                bundle.add(licenseIter.next());
            }         
        }
        BigDecimal value = bidder.calculateValue(bundle);
        Assert.assertTrue(bidder.getSetupType() + " value was " + value + " should be " + expectedValueSmallBundle, value.compareTo(expectedValueSmallBundle)==0);
    }
    
    @Test
    public void valueOfEmptyBundle(){
        Bundle<BMLicense> bundle = new Bundle<>();
        BigDecimal value = bidder.calculateValue(bundle);
        Assert.assertTrue(bidder.getSetupType() + " value was " + value + " should be " + expectedValueEmptyBundle, value.compareTo(expectedValueEmptyBundle)==0);
    }
    
}

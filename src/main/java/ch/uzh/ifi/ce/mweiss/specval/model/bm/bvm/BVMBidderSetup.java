/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.ce.mweiss.specval.model.bm.BMBidderSetup;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BVMWorldSetup.BVMWorldSetupBuilder;
import ch.uzh.ifi.ce.mweiss.specval.util.random.DoubleInterval;
import ch.uzh.ifi.ce.mweiss.specval.util.random.IntegerInterval;



/**
 * A builder with default values of the base value model
 * @author Michael Weiss
 *
 */
public final class BVMBidderSetup extends BMBidderSetup{
    
    public static final String DEFAULT_SETUP_NAME = "DEFAULT_BASE_VALUE_MODEL_BIDDER_SETUP";
    
    private BVMBidderSetup(BVMBidderSetupBuilder builder){
        super(builder);
    }
    
    /**
     * Returns an unmodified default setup of the BVM bidder 
     * without the possibility to further change it.
     * @param numberOfBidders
     * @return
     */
    public static BVMBidderSetup getDefaultSetup(int numberOfBidders){
        return new BVMBidderSetupBuilder(DEFAULT_SETUP_NAME, numberOfBidders).build();
    }
    
    public static final class BVMBidderSetupBuilder extends BMBidderSetupBuilder{
        public BVMBidderSetupBuilder(String setupName, int numberOfBidders) {
            super(setupName, numberOfBidders);
            putBaseValueInterval(BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_A, new DoubleInterval(120,200));
            putBaseValueInterval(BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_B, new DoubleInterval(60,90));
            putValueThresholdInterval(BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_A, new IntegerInterval(6));
            putValueThresholdInterval(BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_B, new IntegerInterval(6));
            Map<Integer, BigDecimal> synFactorsA = new HashMap<Integer, BigDecimal>();
            synFactorsA.put(2, BigDecimal.valueOf(1.2));
            synFactorsA.put(3, BigDecimal.valueOf(1.4));
            synFactorsA.put(4, BigDecimal.valueOf(1.8));
            putSynergyFactors(BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_A, synFactorsA);
            //No Synergies in band B
            putSynergyFactors(BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_B, new HashMap<Integer,BigDecimal>());
        }
      
        @Override
        public void putValueThresholdInterval(String bandName, IntegerInterval interval){
            super.putValueThresholdInterval(bandName, interval);
        }
        
        @Override
        public IntegerInterval removeValueThresholdInterval(String bandName){
            return super.removeValueThresholdInterval(bandName);
        }
        
        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.model.bm.BMBidderSetupBuilder#build()
         */
        @Override
        public BVMBidderSetup build() {
            return new BVMBidderSetup(this);
        }
    }
    
}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm.bvm;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.spectrumauctions.sats.core.model.bvm.BMBidderSetup;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;


/**
 * A builder with default values of the base value model
 * @author Michael Weiss
 *
 */
public final class BVMBidderSetup extends BMBidderSetup {
    
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
            putBaseValueInterval(BVMWorldSetup.BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_A, new DoubleInterval(120,200));
            putBaseValueInterval(BVMWorldSetup.BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_B, new DoubleInterval(60,90));
            putValueThresholdInterval(BVMWorldSetup.BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_A, new IntegerInterval(6));
            putValueThresholdInterval(BVMWorldSetup.BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_B, new IntegerInterval(6));
            Map<Integer, BigDecimal> synFactorsA = new HashMap<Integer, BigDecimal>();
            synFactorsA.put(2, BigDecimal.valueOf(1.2));
            synFactorsA.put(3, BigDecimal.valueOf(1.4));
            synFactorsA.put(4, BigDecimal.valueOf(1.8));
            putSynergyFactors(BVMWorldSetup.BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_A, synFactorsA);
            //No Synergies in band B
            putSynergyFactors(BVMWorldSetup.BVMWorldSetupBuilder.BICHLER_2014_BVM_DEFAULT_BAND_NAME_B, new HashMap<Integer,BigDecimal>());
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
         * @see org.spectrumauctions.sats.core.model.bvm.BMBidderSetupBuilder#build()
         */
        @Override
        public BVMBidderSetup build() {
            return new BVMBidderSetup(this);
        }
    }
    
}

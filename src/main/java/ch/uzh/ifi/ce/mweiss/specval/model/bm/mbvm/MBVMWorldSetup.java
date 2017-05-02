/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.bm.mbvm;

import ch.uzh.ifi.ce.mweiss.specval.model.bm.BMWorldSetup;


/**
 * @author Michael Weiss
 *
 */
public final class MBVMWorldSetup extends BMWorldSetup {

    public static final String DEFAULT_SETUP_NAME = "DEFAULT_MULTIBAND_VALUE_MODEL_WORLD_SETUP";

    protected MBVMWorldSetup(MBVMWorldSetupBuilder builder) {
        super(builder);
    }
    
    /**
     * Returns an unmodified default setup of the BVM world 
     * without the possibility to further change it.
     * @param numberOfBidders
     * @return
     */
    public static MBVMWorldSetup buildDefaultSetup(){
        return new MBVMWorldSetup(new MBVMWorldSetupBuilder(DEFAULT_SETUP_NAME));
    }
    
    
    public static class MBVMWorldSetupBuilder extends BMWorldSetupBuilder {
        
        public static final String BICHLER_2014_MBVM_DEFAULT_BAND_NAME_A = "A";
        public static final String BICHLER_2014_MBVM_DEFAULT_BAND_NAME_B = "B";
        public static final String BICHLER_2014_MBVM_DEFAULT_BAND_NAME_C = "C";
        public static final String BICHLER_2014_MBVM_DEFAULT_BAND_NAME_D = "D";


        public MBVMWorldSetupBuilder(String setupName) {
            super(setupName);
            addBand(BICHLER_2014_MBVM_DEFAULT_BAND_NAME_A, 6);
            addBand(BICHLER_2014_MBVM_DEFAULT_BAND_NAME_B, 6);
            addBand(BICHLER_2014_MBVM_DEFAULT_BAND_NAME_C, 6);
            addBand(BICHLER_2014_MBVM_DEFAULT_BAND_NAME_D, 6);
        }
        
        
        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.model.bm.BMWorldSetupBuilder#build()
         */
        @Override
        public MBVMWorldSetup build() {
            return new MBVMWorldSetup(this);
        }
        
        
    }

}

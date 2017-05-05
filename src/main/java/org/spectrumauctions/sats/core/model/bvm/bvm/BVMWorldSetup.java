/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm.bvm;

import org.spectrumauctions.sats.core.model.bvm.BMWorldSetup;


/**
 * @author Michael Weiss
 *
 */
public final class BVMWorldSetup extends BMWorldSetup {


    public static final String DEFAULT_SETUP_NAME = "DEFAULT_BASE_VALUE_MODEL_WORLD_SETUP";


    /**
     * @param builder
     */
    private BVMWorldSetup(BMWorldSetupBuilder builder) {
        super(builder);
    }

    /**
     * Returns an unmodified default setup of the BVM world 
     * without the possibility to further change it.
     * @return
     */
    public static BVMWorldSetup getDefaultSetup() {
        return new BVMWorldSetup(new BVMWorldSetupBuilder(DEFAULT_SETUP_NAME));
    }


    public static class BVMWorldSetupBuilder extends BMWorldSetupBuilder {

        public static final String BICHLER_2014_BVM_DEFAULT_BAND_NAME_A = "A";
        public static final String BICHLER_2014_BVM_DEFAULT_BAND_NAME_B = "B";


        public BVMWorldSetupBuilder(String setupName) {
            super(setupName);
            addBand(BICHLER_2014_BVM_DEFAULT_BAND_NAME_A, 14);
            addBand(BICHLER_2014_BVM_DEFAULT_BAND_NAME_B, 10);
        }


        /* (non-Javadoc)
         * @see org.spectrumauctions.sats.core.model.bvm.BMWorldSetupBuilder#build()
         */
        @Override
        public BVMWorldSetup build() {
            return new BVMWorldSetup(this);
        }

    }


}

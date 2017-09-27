/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm.mbvm;

import org.spectrumauctions.sats.core.model.bvm.BMBand;
import org.spectrumauctions.sats.core.model.bvm.BMBidderSetup;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


public final class MBVMBidderSetup extends BMBidderSetup {

    public static final String DEFAULT_SETUP_NAME = "DEFAULT_MULTIBAND_VALUE_MODEL_BIDDER_SETUP";

    private MBVMBidderSetup(MBVMBidderSetupBuilder builder) {
        super(builder);
    }

    /**
     * Read javadoc of superclass of explanation of this parameter.
     * As there are no thresholds in the MBVM, this method always returns {@link BMBand#getNumberOfLicenses()}.
     */
    @Override
    public Integer drawPositiveValueThreshold(BMBand band, UniformDistributionRNG rng) {
        return band.getNumberOfLicenses();
    }


    /**
     * @return an unmodified default setup of the MBVM bidder without the possibility to further change it.
     */
    public static MBVMBidderSetup getDefaultSetup(int numberOfBidders) {
        return new MBVMBidderSetupBuilder(DEFAULT_SETUP_NAME, numberOfBidders).build();
    }


    /**
     * A builder with default values of the Multi-Band Value Model
     * @author Michael Weiss
     */
    public static final class MBVMBidderSetupBuilder extends BMBidderSetupBuilder {
        public MBVMBidderSetupBuilder(String setupName, int numberOfBidders) {
            super(setupName, numberOfBidders);
            // Base Values
            putBaseValueInterval(MBVMWorldSetup.MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_A, new DoubleInterval(100, 300));
            putBaseValueInterval(MBVMWorldSetup.MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_B, new DoubleInterval(50, 200));
            putBaseValueInterval(MBVMWorldSetup.MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_C, new DoubleInterval(50, 200));
            putBaseValueInterval(MBVMWorldSetup.MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_D, new DoubleInterval(50, 200));
            // Synergy Factors (identical for all bands)
            Map<Integer, BigDecimal> synFactors = new HashMap<>();
            synFactors.put(2, BigDecimal.valueOf(1.6));
            synFactors.put(3, BigDecimal.valueOf(1.5));
            putSynergyFactors(MBVMWorldSetup.MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_A, new HashMap<>(synFactors));
            putSynergyFactors(MBVMWorldSetup.MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_B, new HashMap<>(synFactors));
            putSynergyFactors(MBVMWorldSetup.MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_C, new HashMap<>(synFactors));
            putSynergyFactors(MBVMWorldSetup.MBVMWorldSetupBuilder.BICHLER_2014_MBVM_DEFAULT_BAND_NAME_D, new HashMap<>(synFactors));

        }


        /**
         * @see BMBidderSetupBuilder#build()
         */
        @Override
        public MBVMBidderSetup build() {
            return new MBVMBidderSetup(this);
        }

    }


}

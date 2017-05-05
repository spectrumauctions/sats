/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.mrvm;

import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.mrvm.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Michael Weiss
 *
 */
public class MRVMOverallValueTest {

    public static String LOW_PAIRED_NAME = "LOW_PAIRED";
    public static String HIGH_PAIRED_NAME = "HIGH_PAIRED";
    public static String UNPAIRED_NAME = "UNPAIRED";

    @Test
    @Ignore
    public void mipValuesEqualSATSValues() {
        List<MRVMBidder> bidders = new MultiRegionModel().createNewPopulation();
        MRVM_MIP mip = new MRVM_MIP(bidders);
        MRVMMipResult result = mip.calculateAllocation();
        for (MRVMBidder bidder : bidders) {
            GenericValue<MRVMGenericDefinition> outcomeVal = result.getAllocation(bidder);
            BigDecimal satsVal = bidder.calculateValue(outcomeVal.getQuantities());
            assertAuxiliaryVariables(outcomeVal, result.getJoptResult(), bidder, mip);
            Assert.assertEquals(satsVal.doubleValue(), outcomeVal.getValue().doubleValue(), 0.1);
        }
    }


    private void assertAuxiliaryVariables(GenericValue<MRVMGenericDefinition> val, IMIPResult imipResult, MRVMBidder bidder, MRVM_MIP mrvm_mip) {
        for (MRVMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            for (MRVMBand band : bidder.getWorld().getBands()) {
                //Check transformation to allocation is correct
                int anyBundleSize = val.anyConsistentBundle().size();
                Assert.assertEquals(val.getSize(), anyBundleSize);
                Variable xVariable = mrvm_mip.getWorldPartialMip().getXVariable(bidder, region, band);
                double xVarValue = imipResult.getValue(xVariable);
                Assert.assertEquals(xVarValue, val.getQuantity(new MRVMGenericDefinition(band, region)), 0.001);
            }
        }
        for (MRVMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            //Check c variables
            BigDecimal c = MRVMWorld.c(region, (Bundle<MRVMLicense>) val.anyConsistentBundle());
            Variable cVariable = mrvm_mip.getBidderPartialMips().get(bidder).getCVariable(region);
            double mipC = imipResult.getValue(cVariable);
            Assert.assertEquals(c.doubleValue(), mipC, 0.0001);
            //Check SV variable
            BigDecimal sv = bidder.svFunction(region, c);
            Variable svVariable = mrvm_mip.getBidderPartialMips().get(bidder).getSVVariable(region);
            double mipSV = imipResult.getValue(svVariable);
            Assert.assertEquals(sv.doubleValue(), mipSV, 0.0001);

        }
        System.out.println("===================================================");
        System.out.println("===================================================");
    }
}

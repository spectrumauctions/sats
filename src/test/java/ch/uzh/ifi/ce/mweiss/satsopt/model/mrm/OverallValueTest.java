/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.satsopt.model.mrm;

import java.math.BigDecimal;
import java.util.List;

import ch.uzh.ifi.ce.mweiss.specval.model.Bundle;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.*;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValue;

/**
 * @author Michael Weiss
 *
 */
public class OverallValueTest {

    public static String LOW_PAIRED_NAME = "LOW_PAIRED";
    public static String HIGH_PAIRED_NAME = "HIGH_PAIRED";
    public static String UNPAIRED_NAME = "UNPAIRED";
    
    @Test
    @Ignore
    public void mipValuesEqualSATSValues(){
        List<MRMBidder> bidders = new MultiRegionModel().createNewPopulation();
        MRM_MIP mip = new MRM_MIP(bidders);
        MipResult result = mip.calculateAllocation();
        for(MRMBidder bidder : bidders){
            GenericValue<MRMGenericDefinition> outcomeVal = result.getAllocation(bidder);
            BigDecimal satsVal = bidder.calculateValue(outcomeVal.getQuantities());
            assertAuxiliaryVariables(outcomeVal, result.getJoptResult(), bidder, mip);
            Assert.assertEquals(satsVal.doubleValue(), outcomeVal.getValue().doubleValue(), 0.1);
        }
    }



    private void assertAuxiliaryVariables(GenericValue<MRMGenericDefinition> val, IMIPResult imipResult, MRMBidder bidder, MRM_MIP mrm_mip){
        for(MRMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()){
            for (MRMBand band : bidder.getWorld().getBands()){
                //Check transformation to allocation is correct
                int anyBundleSize = val.anyConsistentBundle().size();
                Assert.assertEquals(val.getSize(), anyBundleSize);
                Variable xVariable = mrm_mip.getWorldPartialMip().getXVariable(bidder, region, band);
                double xVarValue = imipResult.getValue(xVariable);
                Assert.assertEquals(xVarValue, val.getQuantity(new MRMGenericDefinition(band, region)), 0.001);
            }
        }
        for(MRMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()){
            //Check c variables
            BigDecimal c = MRMWorld.c(region, (Bundle<MRMLicense>) val.anyConsistentBundle());
            Variable cVariable = mrm_mip.getBidderPartialMips().get(bidder).getCVariable(region);
            double mipC = imipResult.getValue(cVariable);
            Assert.assertEquals(c.doubleValue(), mipC, 0.0001);
            //Check SV variable
            BigDecimal sv = bidder.svFunction(region, c);
            Variable svVariable = mrm_mip.getBidderPartialMips().get(bidder).getSVVariable(region);
            double mipSV = imipResult.getValue(svVariable);
            Assert.assertEquals(sv.doubleValue(), mipSV, 0.0001);

        }
        System.out.println("===================================================");
        System.out.println("===================================================");
    }
}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.opt.model.mrm;

import java.util.Collection;

import org.junit.Test;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.GenericValue;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMBand;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMBidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMGenericDefinition;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMRegionsMap.Region;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MultiRegionModel;

/**
 * @author Michael Weiss
 *
 */
public class MipTest {

    @Test
    public void testNoException(){
        Collection<MRMBidder> bidders = (new MultiRegionModel()).createNewPopulation();
        MRM_MIP mip = new MRM_MIP(bidders);
        MipResult result = mip.calculateAllocation();
        for(MRMBidder bidder : bidders){
            GenericValue<MRMGenericDefinition> genVal = result.getAllocation(bidder);
            for(Region region : bidder.getWorld().getRegionsMap().getRegions()){
                for(MRMBand band : bidder.getWorld().getBands()){
                    MRMGenericDefinition def = new MRMGenericDefinition(band, region);
                    Integer quantity = genVal.getQuantity(def);
                    System.out.println(new StringBuilder("bidder ").append(bidder.getId()).append("\t").append(def.toString()).append("\t").append(quantity));
                }
            }
            
        }
        System.out.println(result.getTotalValue());
        System.out.println();
    }

}

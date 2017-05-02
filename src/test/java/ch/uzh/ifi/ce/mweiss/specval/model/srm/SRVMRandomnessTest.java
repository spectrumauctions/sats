/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.srm;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;

/**
 * @author Michael Weiss
 *
 */
public class SRVMRandomnessTest {

    long seed = 23498132;
    long seed2 = 23578623;
    
    @Test
    public void sameSeedSameOutcomeTestSRVM(){
        SingleRegionModel model = new SingleRegionModel();
        SRMWorld world1 = model.createWorld(seed);       
        SRMWorld world2 = model.createWorld(seed);
        Assert.assertEquals(world1, world2);
        List<? extends Bidder<?>> bidders1 = model.createPopulation(world1, seed2);
        List<? extends Bidder<?>> bidders2 = model.createPopulation(world1, seed2);
        List<? extends Bidder<?>> bidders3 = model.createPopulation(world2, seed2);
        List<? extends Bidder<?>> bidders4 = model.createPopulation(world2, seed2);
        Assert.assertEquals(bidders1, bidders2);
        Assert.assertEquals(bidders2, bidders3);
        Assert.assertEquals(bidders3, bidders4);
        bidders1 = model.createNewPopulation(seed);
        bidders2 = model.createNewPopulation(seed);
        Assert.assertEquals(bidders1, bidders2);
    }
}

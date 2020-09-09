/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.cats;

import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.SATSBidder;

import java.util.List;

/**
 * @author Fabio Isler
 *
 */
public class CATSRandomnessTest {

    long seed = 34907230;
    long seed2 = 234234;

    @Test
    public void sameSeedSameOutcomeTestCATS() {
        CATSRegionModel model = new CATSRegionModel();
        CATSWorld world1 = model.createWorld(seed);
        CATSWorld world2 = model.createWorld(seed);
        Assert.assertEquals(world1, world2);
        List<? extends SATSBidder> bidders1 = model.createNewPopulation(world1, seed2);
        List<? extends SATSBidder> bidders2 = model.createNewPopulation(world1, seed2);
        List<? extends SATSBidder> bidders3 = model.createNewPopulation(world2, seed2);
        List<? extends SATSBidder> bidders4 = model.createNewPopulation(world2, seed2);
        Assert.assertEquals(bidders1, bidders2);
        Assert.assertEquals(bidders2, bidders3);
        Assert.assertEquals(bidders3, bidders4);
        bidders1 = model.createNewWorldAndPopulation(seed);
        bidders2 = model.createNewWorldAndPopulation(seed);
        Assert.assertEquals(bidders1, bidders2);
    }
}

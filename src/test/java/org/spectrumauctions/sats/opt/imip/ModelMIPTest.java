/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.imip;

import edu.harvard.econcs.jopt.solver.MIPException;
import edu.harvard.econcs.jopt.solver.SolveParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;

import static org.junit.Assert.fail;

/**
 * @author Fabio isler
 */
public class ModelMIPTest {

    private static final Logger logger = LogManager.getLogger(ModelMIPTest.class);

    @Test
    public void setAcceptSuboptimalSolutionAtTimeout() {
        MRVM_MIP mip = new MRVM_MIP(new MultiRegionModel().createNewPopulation(5413646L));
        Assert.assertTrue(mip.getMIP().getDoubleSolveParam(SolveParam.TIME_LIMIT) > 2);
        mip.setTimeLimit(2);
        Assert.assertEquals(2, mip.getMIP().getDoubleSolveParam(SolveParam.TIME_LIMIT), 1e-6);
        mip.getAllocation();
        logger.info("Successfully accepted suboptimal solution at timeout!");
    }

    @Test
    public void setAcceptNoSuboptimalSolutionAtTimeout() {
        MRVM_MIP mip = new MRVM_MIP(new MultiRegionModel().createNewPopulation(5413646L));
        Assert.assertTrue(mip.getMIP().getDoubleSolveParam(SolveParam.TIME_LIMIT) > 2);
        mip.setTimeLimit(2);
        Assert.assertEquals(2, mip.getMIP().getDoubleSolveParam(SolveParam.TIME_LIMIT), 1e-6);
        mip.setAcceptSuboptimal(false);
        Assert.assertFalse(mip.getMIP().getBooleanSolveParam(SolveParam.ACCEPT_SUBOPTIMAL));
        try {
            mip.getAllocation();
            fail("Should have timed out and thrown an error.");
        } catch (MIPException e) {
            if (e.getMessage().contains("suboptimal")) {
                logger.info("Successfully thrown an error at timeout!");
            } else {
                logger.error("An exception occurred that was not expected:\n ");
                logger.error(e.getStackTrace());
                fail();
            }
        }
    }

    @Test
    public void setDisplayOutput() {
        MRVM_MIP mip = new MRVM_MIP(new MultiRegionModel().createNewPopulation(5413646L));
        Assert.assertFalse(mip.getMIP().getBooleanSolveParam(SolveParam.DISPLAY_OUTPUT));
        mip.setDisplayOutput(true);
        Assert.assertTrue(mip.getMIP().getBooleanSolveParam(SolveParam.DISPLAY_OUTPUT));
    }
}

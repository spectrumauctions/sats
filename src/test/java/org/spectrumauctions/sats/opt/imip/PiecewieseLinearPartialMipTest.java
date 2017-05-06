/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.imip;

import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.VarType;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.util.math.ContinuousPiecewiseLinearFunction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Weiss
 */
public class PiecewieseLinearPartialMipTest {

    @Test
    public void pieceWiseLinear() {
        Map<BigDecimal, BigDecimal> cornerPoints = new HashMap<>();
        cornerPoints.put(new BigDecimal(0), new BigDecimal(0));
        cornerPoints.put(new BigDecimal(1), new BigDecimal(1));
        cornerPoints.put(new BigDecimal(2), new BigDecimal(4));
        cornerPoints.put(new BigDecimal(3), new BigDecimal(3));
        ContinuousPiecewiseLinearFunction func = new ContinuousPiecewiseLinearFunction(cornerPoints);
        Variable x = new Variable("x", VarType.DOUBLE, 0, 3);
        Variable y = new Variable("y", VarType.DOUBLE, -MIP.MAX_VALUE, MIP.MAX_VALUE);
        PiecewiseLinearPartialMIP mipArtifacts = new PiecewiseLinearPartialMIP(func, x, y, "aux_plf_1");
        MIP mip = new MIP();
        mip.setObjectiveMax(true);
        mip.addObjectiveTerm(1, y);
        mip.add(x);
        mip.add(y);
        mipArtifacts.appendToMip(mip);
        System.out.println(mip.toString());
        SolverClient solver = new SolverClient();
        IMIPResult result = solver.solve(mip);
        System.out.println(result.toString());
        Assert.assertEquals(4, result.getObjectiveValue(), 0.00001);
    }

    @Test
    public void onePiecePiecewiseLinear() {
        Map<BigDecimal, BigDecimal> cornerPoints = new HashMap<>();
        cornerPoints.put(new BigDecimal(0), new BigDecimal(0));
        cornerPoints.put(new BigDecimal(3), new BigDecimal(3));
        ContinuousPiecewiseLinearFunction func = new ContinuousPiecewiseLinearFunction(cornerPoints);
        Variable x = new Variable("x", VarType.DOUBLE, 0, 3);
        Variable y = new Variable("y", VarType.DOUBLE, -MIP.MAX_VALUE, MIP.MAX_VALUE);
        PiecewiseLinearPartialMIP mipArtifacts = new PiecewiseLinearPartialMIP(func, x, y, "aux_plf_1");
        MIP mip = new MIP();
        mip.setObjectiveMax(true);
        mip.addObjectiveTerm(1, y);
        mip.add(x);
        mip.add(y);
        mipArtifacts.appendToMip(mip);
        System.out.println(mip.toString());
        SolverClient solver = new SolverClient();
        IMIPResult result = solver.solve(mip);
        System.out.println(result.toString());
        Assert.assertEquals(3, result.getObjectiveValue(), 0.00001);
    }
}

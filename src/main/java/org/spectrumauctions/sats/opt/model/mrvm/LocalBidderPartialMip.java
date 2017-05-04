/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.mrvm;

import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLocalBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap;

import java.math.BigDecimal;

/**
 * @author Michael Weiss
 *
 */
public class LocalBidderPartialMip extends BidderPartialMIP {

    //Strore bidder twice (in superclass and here) to avoid casting
    private final MRVMLocalBidder bidder;

    /**
     * @param bidder
     * @param worldMip
     */
    public LocalBidderPartialMip(MRVMLocalBidder bidder, double scalingFactor, WorldPartialMip worldMip) {
        super(bidder, scalingFactor, worldMip);
        this.bidder = bidder;
    }

    public Constraint constrainValue() {
        Constraint constraint = new Constraint(CompareType.EQ, 0);
        Variable biddersValue = worldPartialMip.getValueVariable(bidder);
        constraint.addTerm(-1, biddersValue);
        for (MRVMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            BigDecimal gammaFactor = bidder.gammaFactor(region, null); //Is either 0 or 1
            double constant = gammaFactor.doubleValue();
            Variable regionalOmega = getOmegaVariable(region);
            constraint.addTerm(constant, regionalOmega);
        }
        return constraint;
    }

    /* (non-Javadoc)
     * @see PartialMIP#appendToMip(edu.harvard.econcs.jopt.solver.mip.MIP)
     */
    @Override
    public void appendToMip(MIP mip) {
        super.appendToMip(mip);
        mip.add(constrainValue());
    }
}

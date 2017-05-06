/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.mrvm;

import com.google.common.base.Preconditions;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBand;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMNationalBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMWorld;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;

import java.util.Collection;

import static org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;

/**
 * @author Michael Weiss
 */
public class MRVMNationalBidderPartialMipTest {

    @Test
    public void test() {
        MIP mip = new MIP();
        MRVMWorld world = new MRVMWorld(MRVMWorldGen.getSimpleWorldBuilder(), new JavaUtilRNGSupplier(68543436434L));
        Collection<MRVMBidder> bidders =
                world.createPopulation(null, null, MRVMWorldGen.getSimpleNationalBidderSetup(), new JavaUtilRNGSupplier(57844354L));
        double scalingFactor = Scalor.scalingFactor(bidders);
        double biggestScaledValue = Scalor.biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        MRVMWorldPartialMip worldPartialMip = new MRVMWorldPartialMip(bidders, biggestScaledValue);
        worldPartialMip.appendToMip(mip);
        for (MRVMBidder bidder : bidders) {
            if (bidder instanceof MRVMNationalBidder) {
                MRVMBidderPartialMIP bidderMip = new MRVMNationalBidderPartialMip((MRVMNationalBidder) bidder, scalingFactor, worldPartialMip);
                bidderMip.appendToMip(mip);
            }
        }
        SolverClient solver = new SolverClient();
        IMIPResult result = solver.solve(mip);
        System.out.println(result);
    }

    @Test
    public void testWirAndWi() {
        MIP mip = new MIP();
        mip.setObjectiveMax(true);
        MRVMWorld world = new MRVMWorld(MRVMWorldGen.getSimpleWorldBuilder(), new JavaUtilRNGSupplier(68543436434L));
        Collection<MRVMBidder> bidders =
                world.createPopulation(null, null, MRVMWorldGen.getSimpleNationalBidderSetup(), new JavaUtilRNGSupplier(57844354L));
        double scalingFactor = Scalor.scalingFactor(bidders);
        double biggestScaledValue = Scalor.biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        MRVMWorldPartialMip worldPartialMip = new MRVMWorldPartialMip(bidders, biggestScaledValue);
        MRVMNationalBidder bidder = (MRVMNationalBidder) bidders.iterator().next();
        MRVMNationalBidderPartialMip bidderMip = new MRVMNationalBidderPartialMip(bidder, 1, worldPartialMip);
        int numberOfRegionsInBundle = 2;
        int regionCount = 0;
        // Constraint \hat{W}_{i,r}
        for (Region region : world.getRegionsMap().getRegions()) {
            if (regionCount++ < numberOfRegionsInBundle) {
                for (MRVMBand band : world.getBands()) {
                    Constraint hasALicense = new Constraint(CompareType.EQ, 2);
                    hasALicense.addTerm(1, worldPartialMip.getXVariable(bidder, region, band));
                    mip.add(hasALicense);
                }
            } else {
                for (MRVMBand band : world.getBands()) {
                    Constraint hasNoLicense = new Constraint(CompareType.EQ, 0);
                    hasNoLicense.addTerm(1, worldPartialMip.getXVariable(bidder, region, band));
                    mip.add(hasNoLicense);
                }
            }
        }
        // Constrain Wi and add to objective
        mip.add(bidderMip.constainWi());
        mip.addObjectiveTerm(1, bidderMip.getWIVariable());
        // Solve MIP
        worldPartialMip.appendVariablesToMip(mip);
        bidderMip.appendVariablesToMip(mip);
        for (Constraint constraint : bidderMip.constrainWIR()) {
            mip.add(constraint);
        }
        SolverClient solver = new SolverClient();
        IMIPResult result = solver.solve(mip);
        Assert.assertEquals(result.getObjectiveValue(), numberOfRegionsInBundle, 0.00001);
    }

    @Test
    public void testWHatWithK0() {
        testWhat(0);
    }

    @Test
    public void testWHatWithK1() {
        testWhat(1);
    }

    @Test
    public void testWHatWithK5() {
        testWhat(5);
    }

    /**
     * @param wi number of regions covered
     */
    private void testWhat(int wi) {
        MIP mip = new MIP();
        mip.setObjectiveMax(true);
        MRVMWorld world = new MRVMWorld(MRVMWorldGen.getSimpleWorldBuilder(), new JavaUtilRNGSupplier(68543436434L));
        Collection<MRVMBidder> bidders =
                world.createPopulation(null, null, MRVMWorldGen.getSimpleNationalBidderSetup(), new JavaUtilRNGSupplier(57844354L));
        double scalingFactor = Scalor.scalingFactor(bidders);
        double biggestScaledValue = Scalor.biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        MRVMWorldPartialMip worldPartialMip = new MRVMWorldPartialMip(bidders, biggestScaledValue);
        MRVMNationalBidder bidder = (MRVMNationalBidder) bidders.iterator().next();
        MRVMNationalBidderPartialMip bidderMip = new MRVMNationalBidderPartialMip(bidder, 1, worldPartialMip);
        // Constrain Wi
        Constraint wiConstraint = new Constraint(CompareType.EQ, wi);
        wiConstraint.addTerm(1, bidderMip.getWIVariable());
        mip.add(wiConstraint);
        // Append Variables
        worldPartialMip.appendVariablesToMip(mip);
        bidderMip.appendVariablesToMip(mip);
        // Append Constraints
        for (Constraint constraint : bidderMip.constrainWHat()) {
            mip.add(constraint);
        }
        // Append objective (sum all \hat{W}, making sure none get ignored)
        for (int k = 0; k <= bidder.getKMax(); k++) {
            mip.addObjectiveTerm(1, bidderMip.getWHatIKVariable(k));
        }
        // Solve MIP
        SolverClient solver = new SolverClient();
        IMIPResult result = solver.solve(mip);
        // Check correctness
        int numberOfUncovered = bidder.getWorld().getRegionsMap().getNumberOfRegions() - wi;
        for (int k = 0; k <= bidder.getKMax(); k++) {
            double varValue = result.getValue(bidderMip.getWHatIKVariable(k));
            if (k == numberOfUncovered) {
                Assert.assertEquals(1, varValue, 0.000001);
            } else if (k == bidder.getKMax() && numberOfUncovered > bidder.getKMax()) {
                Assert.assertEquals(1, varValue, 0.000001);
            } else {
                Assert.assertEquals(0, varValue, 0.000001);
            }
        }
    }

    @Test
    public void testPsiWithK0() {
        testPsi(0);
    }

    @Test
    public void testPsiWithK1() {
        testPsi(1);
    }

    @Test
    public void testPsiWithK5() {
        testPsi(5);
    }

    private void testPsi(int numberOfRegionsUncovered) {
        Preconditions.checkArgument(numberOfRegionsUncovered >= 0);
        MIP mip = new MIP();
        mip.setObjectiveMax(true);
        MRVMWorld world = new MRVMWorld(MRVMWorldGen.getSimpleWorldBuilder(), new JavaUtilRNGSupplier(68543436434L));
        Collection<MRVMBidder> bidders = world.createPopulation(null, null, MRVMWorldGen.getSimpleNationalBidderSetup(), new JavaUtilRNGSupplier(57844354L));
        double scalingFactor = Scalor.scalingFactor(bidders);
        double biggestScaledValue = Scalor.biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        MRVMWorldPartialMip worldPartialMip = new MRVMWorldPartialMip(bidders, biggestScaledValue);
        MRVMNationalBidder bidder = (MRVMNationalBidder) bidders.iterator().next();
        MRVMNationalBidderPartialMip bidderMip = new MRVMNationalBidderPartialMip(bidder, 1, worldPartialMip);

        Preconditions.checkArgument(numberOfRegionsUncovered <= bidder.getWorld().getRegionsMap().getNumberOfRegions());

        // Constrain Omegas (let the omega for every region 1,2,3,...)
        // Note: Values have to be chosen s.t. the sum of the omegas does not exceed the highest possible value
        int totalValue = 0;
        int currentValue = 1;
        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            totalValue += currentValue;
            Variable omegaVar = bidderMip.getOmegaVariable(region);
            Constraint omegaConstraint = new Constraint(CompareType.EQ, currentValue++);
            omegaConstraint.addTerm(1, omegaVar);
            mip.add(omegaConstraint);
        }
        // Constrain \hat{W}_{i,k}
        for (int k = 0; k <= bidder.getKMax(); k++) {
            Constraint constraint;
            if (k == numberOfRegionsUncovered) {
                constraint = new Constraint(CompareType.EQ, 1);
            } else if (k == bidder.getKMax() && numberOfRegionsUncovered > bidder.getKMax()) {
                constraint = new Constraint(CompareType.EQ, 1);
            } else {
                constraint = new Constraint(CompareType.EQ, 0);
            }
            constraint.addTerm(1, bidderMip.getWHatIKVariable(k));
            mip.add(constraint);
        }

        //Append objective (sum all psi, making sure none get ignored)
        for (int k = 0; k <= bidder.getKMax(); k++) {
            mip.addObjectiveTerm(1, bidderMip.getPsi(k));
        }

        // Append Variables and Constraints
        worldPartialMip.appendVariablesToMip(mip);
        bidderMip.appendVariablesToMip(mip);
        for (Constraint constraint : bidderMip.constrainPsi()) {
            mip.add(constraint);
        }

        // Solve MIP
        SolverClient solver = new SolverClient();
        IMIPResult result = solver.solve(mip);
        System.out.println(result);

        // Check correctness
        for (int k = 0; k <= bidder.getKMax(); k++) {
            double varValue = result.getValue(bidderMip.getPsi(k));
            if (k == numberOfRegionsUncovered) {
                Assert.assertEquals(totalValue, varValue, 0.000001);
            } else if (k == bidder.getKMax() && numberOfRegionsUncovered > bidder.getKMax()) {
                Assert.assertEquals(totalValue, varValue, 0.000001);
            } else {
                Assert.assertEquals(0, varValue, 0.000001);
            }
        }
    }
}

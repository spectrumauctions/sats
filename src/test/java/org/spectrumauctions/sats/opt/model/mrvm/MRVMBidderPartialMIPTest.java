/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.mrvm;

import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;
import org.spectrumauctions.sats.core.util.math.ContinuousPiecewiseLinearFunction;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.opt.imip.PartialMIP;
import org.spectrumauctions.sats.opt.imip.PiecewiseLinearPartialMIP;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.fail;

/**
 * @author Michael Weiss
 */
public class MRVMBidderPartialMIPTest {

    private static List<MRVMBidder> bidders;
    private static MRVMWorldPartialMip worldPartialMip;
    private static Map<MRVMBidder, MRVMBidderPartialMIP> bidderPartialMips;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        MRVMWorld world = new MRVMWorld(MRVMWorldGen.getSimpleWorldBuilder(), new JavaUtilRNGSupplier(153578351L));
        MRVMLocalBidderSetup setup = MRVMWorldGen.getSimpleLocalBidderSetup();
        bidders = world.createPopulation(setup, null, null, new JavaUtilRNGSupplier(15434684L));
        double scalingFactor = Scalor.scalingFactor(bidders);
        double biggestScaledValue = Scalor.biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        worldPartialMip = new MRVMWorldPartialMip(bidders, biggestScaledValue);

        bidderPartialMips = new HashMap<>();
        for (MRVMBidder bidder : bidders) {
            MRVMLocalBidder localBidder = (MRVMLocalBidder) bidder;
            bidderPartialMips.put(bidder, new MRVMLocalBidderPartialMip(localBidder, 1, worldPartialMip));
        }
    }

    /**
     * Test method for {@link MRVMBidderPartialMIP#generateOmegaConstraints()}.
     */
    @Test
    public void testGenerateOmegaConstraints() {
        MIP mip = new MIP();
        worldPartialMip.appendVariablesToMip(mip);
        mip.setObjectiveMax(true);

        Map<MRVMBidder, MRVMBidderPartialMIP> partialMips = new HashMap<>();

        for (MRVMBidder bidder : bidders) {
            MRVMBidderPartialMIP bidderPartialMIP = bidderPartialMips.get(bidder);
            partialMips.put(bidder, bidderPartialMIP);
            // Fix SV variables (put quality to 1/(1+regionId))
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                Constraint svConstraint = new Constraint(CompareType.EQ, 1. / (1 + region.getId()));
                svConstraint.addTerm(1, bidderPartialMIP.getSVVariable(region));
                mip.add(svConstraint);
            }
            // Append Omega Variables   
            for (Constraint constraint : bidderPartialMIP.generateOmegaConstraints()) {
                mip.add(constraint);
            }
            bidderPartialMIP.appendVariablesToMip(mip);
        }

        // Append Omegas to Objective
        for (Entry<MRVMBidder, MRVMBidderPartialMIP> partialMip : partialMips.entrySet()) {
            for (Region region : partialMip.getKey().getWorld().getRegionsMap().getRegions()) {
                mip.addObjectiveTerm(1, partialMip.getValue().getOmegaVariable(region));
            }
        }

        // Solve MIP
        SolverClient solverClient = new SolverClient();
        IMIPResult result = solverClient.solve(mip);

        // Test omega values        
        boolean noAssertions = true;
        for (Entry<MRVMBidder, MRVMBidderPartialMIP> partialMip : partialMips.entrySet()) {
            MRVMBidder bidder = partialMip.getKey();
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                double omega = result.getValue(partialMip.getValue().getOmegaVariable(region));
                double alpha = bidder.getAlpha().doubleValue();
                double beta = bidder.getBeta(region).doubleValue();
                double population = region.getPopulation();
                double expected = 1. / (1 + region.getId()) * beta * population * partialMip.getValue().getScalingFactor();
                Assert.assertEquals(expected, omega, 0.0000001);
                noAssertions = false;
            }
        }
        Assert.assertFalse(noAssertions);
    }

    @Test
    public void testSVWithMinCapacity() {
        testSV(0.);
    }


    @Test
    public void testSVWithCapacityMedium() {
        double maximumRegionalCapacity = bidders.get(0).getWorld().getMaximumRegionalCapacity().doubleValue();
        testSV(maximumRegionalCapacity * .2);
        testSV(maximumRegionalCapacity * .3);
        testSV(maximumRegionalCapacity * .5);
    }

    @Test
    public void testSVWithMaxCapacity() {
        double maximumRegionalCapacity = bidders.get(0).getWorld().getMaximumRegionalCapacity().doubleValue();
        testSV(maximumRegionalCapacity);
    }

    private void testSV(double capacityShare) {
        MIP mip = new MIP();
        worldPartialMip.appendVariablesToMip(mip);
        mip.setObjectiveMax(true);

        Map<MRVMBidder, MRVMBidderPartialMIP> partialMips = new HashMap<>();

        for (MRVMBidder bidder : bidders) {
            MRVMBidderPartialMIP bidderPartialMIP = bidderPartialMips.get(bidder);
            partialMips.put(bidder, bidderPartialMIP);
            // Fix c-variable to equal capacity share
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                Constraint c = new Constraint(CompareType.EQ, capacityShare);
                c.addTerm(1, bidderPartialMIP.getCVariable(region));
                mip.add(c);
            }
            // Add constraints under test
            for (PartialMIP svPartial : bidderPartialMIP.generateSVConstraints()) {
                // Append generated constraints to MIP. Variables are already added through MRVMBidderPartialMIP
                svPartial.appendConstraintsToMip(mip);
            }
            bidderPartialMIP.appendVariablesToMip(mip);
        }

        // Append Qualities to Objective
        for (Entry<MRVMBidder, MRVMBidderPartialMIP> partialMip : partialMips.entrySet()) {
            for (Region region : partialMip.getKey().getWorld().getRegionsMap().getRegions()) {
                mip.addObjectiveTerm(1, partialMip.getValue().getSVVariable(region));
            }
        }

        // Solve MIP
        SolverClient solverClient = new SolverClient();
        IMIPResult result = solverClient.solve(mip);

        // Test SVFunction
        boolean noAssertions = true;
        for (Entry<MRVMBidder, MRVMBidderPartialMIP> partialMip : partialMips.entrySet()) {
            MRVMBidder bidder = partialMip.getKey();
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                BigDecimal functionInput = BigDecimal.valueOf(capacityShare);
                BigDecimal populationTimesBeta = BigDecimal.valueOf(region.getPopulation()).multiply(bidder.getBeta(region));
                BigDecimal maxVal;
                BigDecimal minVal;
                BigDecimal minX;
                BigDecimal maxX;
                if (functionInput.compareTo(bidder.getzLow(region).multiply(populationTimesBeta)) <= 0) {
                    //Input in first section of piecewise linear function
                    maxVal = BigDecimal.valueOf(0.27).multiply(bidder.getAlpha());
                    minVal = BigDecimal.ZERO;
                    minX = BigDecimal.ZERO;
                    maxX = bidder.getzLow(region).multiply(populationTimesBeta);

                } else if (functionInput.compareTo(bidder.getzHigh(region).multiply(populationTimesBeta)) <= 0) {
                    //Input in second section of piecewise linear function
                    maxVal = BigDecimal.valueOf(0.73).multiply(bidder.getAlpha());
                    minVal = BigDecimal.valueOf(0.27).multiply(bidder.getAlpha());
                    minX = bidder.getzLow(region).multiply(populationTimesBeta);
                    maxX = bidder.getzHigh(region).multiply(populationTimesBeta);
                } else {
                    //Input in third section of piecewise linear function
                    maxVal = bidder.getAlpha();
                    minVal = BigDecimal.valueOf(0.73).multiply(bidder.getAlpha());
                    minX = bidder.getzHigh(region).multiply(populationTimesBeta);
                    maxX = bidder.getWorld().getMaximumRegionalCapacity();
                }
                //Calculate linar section function output
                BigDecimal m = maxVal.subtract(minVal).divide(maxX.subtract(minX), RoundingMode.HALF_UP);
                BigDecimal partialX = functionInput.subtract(minX);
                BigDecimal expected = partialX.multiply(m).add(minVal);
                Double actual = result.getValue(partialMip.getValue().getSVVariable(region));
                Assert.assertEquals(expected.doubleValue(), actual, 0.00001);
                noAssertions = false;
            }
        }
        Assert.assertFalse(noAssertions);
    }

    /**
     * Test method for {@link MRVMBidderPartialMIP#generateCConstraints()}.
     */
    @Test
    public void testGenerateCConstraints() {
        MIP mip = new MIP();
        worldPartialMip.appendVariablesToMip(mip);
        worldPartialMip.appendConstraintsToMip(mip);
        mip.setObjectiveMax(true);
        for (MRVMBidder bidder : bidders) {
            MRVMBidderPartialMIP bidderPartialMIP = bidderPartialMips.get(bidder);
            bidderPartialMIP.appendVariablesToMip(mip);
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                mip.addObjectiveTerm(1, bidderPartialMIP.getCVariable(region));
            }
            for (PartialMIP capConstrainingMip : bidderPartialMIP.generateCapConstraints()) {
                capConstrainingMip.appendConstraintsToMip(mip);
            }
            for (Constraint constraint : bidderPartialMIP.generateCConstraints()) {
                mip.add(constraint);
            }
        }


        SolverClient solverClient = new SolverClient();
        IMIPResult result = solverClient.solve(mip);
        // Since the synergies in this very simple setting induce weakly super-additive value functions, the (possibly added) shares in each region are exacly one, hence the objective value should be 5
        Assert.assertEquals(700, result.getObjectiveValue(), 0.00001);
    }

    /**
     * Test method for {@link MRVMBidderPartialMIP#generateCapConstraints()}.
     */
    @Test
    public void testGenerateCapConstraints() {
        MIP mip = new MIP();
        worldPartialMip.appendVariablesToMip(mip);
        worldPartialMip.appendConstraintsToMip(mip);

        mip.setObjectiveMax(true);
        for (MRVMBidder bidder : bidders) {
            MRVMBidderPartialMIP bidderPartialMIP = bidderPartialMips.get(bidder);
            bidderPartialMIP.appendVariablesToMip(mip);
            bidderPartialMIP.appendConstraintsToMip(mip);
        }

        //Add objective function to create a (arbitrary) solution
        for (MRVMBidder bidder : bidders) {
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                for (MRVMBand band : bidder.getWorld().getBands()) {
                    Variable capVariable = bidderPartialMips.get(bidder).getCapVariable(region, band);
                    mip.addObjectiveTerm(1, capVariable);
                }
            }
        }

        SolverClient solverClient = new SolverClient();
        IMIPResult result = solverClient.solve(mip);
        for (MRVMBidder bidder : bidders) {
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                for (MRVMBand band : bidder.getWorld().getBands()) {
                    double baseCapacity = band.getBaseCapacity().doubleValue();
                    Variable assignedQuantityVariable = worldPartialMip.getXVariable(bidder, region, band);
                    Double assigned = result.getValue(assignedQuantityVariable);
                    double syn = band.getSynergy(assigned.intValue()).doubleValue();
                    double expected = baseCapacity * assigned * syn;
                    Variable capVariable = bidderPartialMips.get(bidder).getCapVariable(region, band);
                    double actual = result.getValue(capVariable);
                    Assert.assertEquals(expected, actual, 0.00001);
                }
            }
        }
    }

    /**
     * Test method for {@link MRVMBidderPartialMIP#generateCapConstraints()}.
     */
    @Test
    public void testPerBandCapacitiesWithoutPreconstrainedInput() {
        for (MRVMBand band : bidders.iterator().next().getWorld().getBands()) {
            double maxCap = capacityOfOneBandWithoutPreconstrainedInput(band);
            if (band.getName().equals(MRVMWorldGen.BAND_A_NAME)) {
                Assert.assertEquals(80.0, maxCap, 0.00001);
            } else if (band.getName().equals(MRVMWorldGen.BAND_B_NAME)) {
                Assert.assertEquals(60.0, maxCap, 0.00001);
            } else {
                fail("unknown band");
            }
        }
    }


    @Test
    public void testCapVariablesOfTwoBandWithoutPreConstrainedInput() {
        MIP mip = new MIP();
        mip.setObjectiveMax(true);
        worldPartialMip.appendVariablesToMip(mip);

        MRVMBidder bidder = bidders.iterator().next();
        Region region = bidder.getWorld().getRegionsMap().getRegion(0);
        MRVMBidderPartialMIP bidderPartialMIP = bidderPartialMips.get(bidder);

        for (MRVMBand band : bidder.getWorld().getBands()) {
            ContinuousPiecewiseLinearFunction fct = bidderPartialMIP.capLinearFunction(band);
            Variable input = worldPartialMip.getXVariable(bidder, region, band);
            Variable output = bidderPartialMIP.getCapVariable(region, band);
            String auxiliaryVariableName = new StringBuilder("aux_cap_helper_")
                    .append("twoBandTest_")
                    .append(band.getName())
                    .toString();
            PiecewiseLinearPartialMIP partialMip =
                    new PiecewiseLinearPartialMIP(fct,
                            input,
                            output,
                            auxiliaryVariableName);
            partialMip.addVariable(output);
            mip.addObjectiveTerm(1, output);
            partialMip.appendVariablesToMip(mip);
            partialMip.appendConstraintsToMip(mip);
        }

        SolverClient solverClient = new SolverClient();
        IMIPResult result = solverClient.solve(mip);
        Assert.assertEquals(140, result.getObjectiveValue(), 0.00001);
    }


    private double capacityOfOneBandWithoutPreconstrainedInput(MRVMBand band) {
        MIP mip = new MIP();
        mip.setObjectiveMax(true);
        worldPartialMip.appendVariablesToMip(mip);

        MRVMBidder bidder = bidders.iterator().next();
        Region region = band.getWorld().getRegionsMap().getRegion(0);
        MRVMBidderPartialMIP bidderPartialMIP = bidderPartialMips.get(bidder);


        ContinuousPiecewiseLinearFunction fct = bidderPartialMIP.capLinearFunction(band);
        Variable input = worldPartialMip.getXVariable(bidder, region, band);
        Variable output = bidderPartialMIP.getCapVariable(region, band);
        String auxiliaryVariableName = new StringBuilder("aux_cap_helper_")
                .append("ONE-SETTING-TEST_").
                        toString();
        PiecewiseLinearPartialMIP partialMip =
                new PiecewiseLinearPartialMIP(fct,
                        input,
                        output,
                        auxiliaryVariableName);
        partialMip.addVariable(output);
        mip.addObjectiveTerm(1, output);
        partialMip.appendVariablesToMip(mip);
        partialMip.appendConstraintsToMip(mip);

        SolverClient solverClient = new SolverClient();
        IMIPResult result = solverClient.solve(mip);
        return result.getObjectiveValue();
    }


}

package org.spectrumauctions.sats.opt.model.srvm;

import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.srvm.*;
import org.spectrumauctions.sats.core.util.PreconditionUtils;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.opt.domain.PartialMIP;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Fabio Isler
 */
public class SRVMBidderPartialMIPTest {

    private static Map<SRVMBidder, SRVMBidderPartialMIP> singleBandSingleBidderPartialMips;
    private static Map<SRVMBidder, SRVMBidderPartialMIP> singleBidderPartialMips;
    private static Map<SRVMBidder, SRVMBidderPartialMIP> singleBandPartialMips;
    private static Map<SRVMBidder, SRVMBidderPartialMIP> singleBandTenBiddersPartialMips;
    private static Map<SRVMBidder, SRVMBidderPartialMIP> standardBidderPartialMips;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Set<SRVMBidderSetup> setups = SRVMWorldGen.getSimpleSmallBidderSetup(1);
        // Single band, single bidder
        singleBandSingleBidderPartialMips = createBidderPartialMIPs(SRVMWorldGen.getSingleBandWorldSetup(), setups);
        // Standard world, single bidder
        singleBidderPartialMips = createBidderPartialMIPs(SRVMWorldGen.getStandardWorldBuilder(), setups);
        // Single band
        singleBandPartialMips = createBidderPartialMIPs(SRVMWorldGen.getSingleBandWorldSetup(), SRVMWorldGen.getStandardBidderSetups());
        // Single band, three bidders
        singleBandTenBiddersPartialMips = createBidderPartialMIPs(SRVMWorldGen.getSingleBandWorldSetup(), SRVMWorldGen.getSimpleSmallBidderSetup(10));
        // Standard world
        standardBidderPartialMips = createBidderPartialMIPs(SRVMWorldGen.getStandardWorldBuilder(), SRVMWorldGen.getStandardBidderSetups());
    }

    private static Map<SRVMBidder, SRVMBidderPartialMIP> createBidderPartialMIPs(SRVMWorldSetup worldSetup, Set<SRVMBidderSetup> bidderSetups) {
        // Standard world, single bidder
        SRVMWorld standardWorld = new SRVMWorld(worldSetup, new JavaUtilRNGSupplier(153578351L));
        List<SRVMBidder> bidders = standardWorld.createPopulation(bidderSetups, new JavaUtilRNGSupplier(15434684L));
        double scalingFactor = SRVM_MIP.calculateScalingFactor(bidders);
        double biggestScaledValue = SRVM_MIP.biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        SRVMWorldPartialMip worldPartialMip = new SRVMWorldPartialMip(bidders, biggestScaledValue, scalingFactor);
        Map<SRVMBidder, SRVMBidderPartialMIP> bidderPartialMIPs = new HashMap<>();
        for (SRVMBidder bidder : bidders) {
            bidderPartialMIPs.put(bidder, new SRVMBidderPartialMIP(bidder, worldPartialMip));
        }
        return bidderPartialMIPs;
    }

    /**
     * Test method for {@link SRVMBidderPartialMIP#generatezConstraints()}.
     */
    @Test
    public void testZConstraintsSetups() {
        testZConstraints(singleBandSingleBidderPartialMips, 0); // Plenty of licenses
        testZConstraints(singleBidderPartialMips, 0); // Plenty of licenses
        testZConstraints(singleBandPartialMips, 1); // 6 Licenses, 7 bidders
        testZConstraints(singleBandTenBiddersPartialMips, 4); // 6 Licsnese, 10 bidders
        testZConstraints(standardBidderPartialMips, 1); // 6 Licenses in Band A, 7 bidders
    }

    @Test
    public void testWConstraintsSetups() {
        testWConstraints(standardBidderPartialMips);
        testWConstraints(singleBandPartialMips);
    }

    @Test
    public void testInterBandSynergyConstraintsSetup() {
        testInterBandSynergyConstraints(standardBidderPartialMips);
        testInterBandSynergyConstraints(singleBandPartialMips);
    }

    @Test
    public void testValueConstraints() {
        testValueConstraintsSetup(standardBidderPartialMips);
        testValueConstraintsSetup(singleBandPartialMips);
    }

    @Test
    public void testPiecewiseLinearFunctionConstraints() {
        testPiecewiseLinearFunctionSetup(standardBidderPartialMips);
        testPiecewiseLinearFunctionSetup(singleBandPartialMips);
        testPiecewiseLinearFunctionSetup(singleBidderPartialMips);
        testPiecewiseLinearFunctionSetup(singleBandTenBiddersPartialMips);
        testPiecewiseLinearFunctionSetup(standardBidderPartialMips);
    }

    private void testZConstraints(Map<SRVMBidder, SRVMBidderPartialMIP> bidderPartialMips, int falseZ) {
        PreconditionUtils.checkNotNegative(falseZ);
        SRVMWorldPartialMip worldPartialMip = bidderPartialMips.values().stream().findFirst().get().worldPartialMip;
        MIP mip = new MIP();
        worldPartialMip.appendVariablesToMip(mip);
        worldPartialMip.appendConstraintsToMip(mip);
        mip.setObjectiveMax(true);

        for (Map.Entry<SRVMBidder, SRVMBidderPartialMIP> entry : bidderPartialMips.entrySet()) {
            SRVMBidder bidder = entry.getKey();
            SRVMBidderPartialMIP bidderPartialMIP = entry.getValue();
            bidderPartialMIP.appendVariablesToMip(mip);

            addConstraints(mip, bidderPartialMIP.generatezConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generatewConstraints(), true);
            addConstraints(mip, bidderPartialMIP.generateValueConstraints(), true);
            addConstraints(mip, bidderPartialMIP.generateInterBandSynergyConstraints(), true);
            addPartialMipConstraints(mip, bidderPartialMIP.generatePiecewiseLinearFunctionConstraints(), true);

            for (SRVMBand band : bidder.getWorld().getBands()) {
                mip.addObjectiveTerm(1, bidderPartialMIP.getzVariable(band));
            }
        }

        // Solve MIP
        SolverClient solverClient = new SolverClient();
        IMIPResult result = solverClient.solve(mip);

        // Test z values
        boolean noAssertions = true;
        int totalZ = 0;
        int possibleZ = 0;
        for (Entry<SRVMBidder, SRVMBidderPartialMIP> partialMip : bidderPartialMips.entrySet()) {
            SRVMBidder bidder = partialMip.getKey();
            for (SRVMBand band : bidder.getWorld().getBands()) {
                possibleZ++;
                Variable var = partialMip.getValue().getzVariable(band);
                double value = result.getValue(var);
                if (falseZ == 0) {
                    Assert.assertEquals("Expected " + var + " to be 1.0, found " + value + ".", value, 1.0, 0);
                    noAssertions = false;
                } else {
                    totalZ += result.getValue(var) == 1.0 ? 1 : 0;
                }
            }
        }
        if (falseZ > 0) {
            Assert.assertEquals("Expected " + (possibleZ - falseZ) + " Z variables to be 1.0, found " + totalZ + ".", totalZ, (possibleZ - falseZ));
            noAssertions = false;
        }
        Assert.assertFalse(noAssertions);
    }

    private void testWConstraints(Map<SRVMBidder, SRVMBidderPartialMIP> bidderPartialMips) {
        SRVMWorldPartialMip worldPartialMip = bidderPartialMips.values().stream().findFirst().get().worldPartialMip;
        MIP mip = new MIP();
        worldPartialMip.appendVariablesToMip(mip);
        worldPartialMip.appendConstraintsToMip(mip);
        mip.setObjectiveMax(true);

        for (Map.Entry<SRVMBidder, SRVMBidderPartialMIP> entry : bidderPartialMips.entrySet()) {
            SRVMBidder bidder = entry.getKey();
            SRVMBidderPartialMIP bidderPartialMIP = entry.getValue();
            bidderPartialMIP.appendVariablesToMip(mip);

            addConstraints(mip, bidderPartialMIP.generatezConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generatewConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generateInterBandSynergyConstraints(), true);
            addConstraints(mip, bidderPartialMIP.generateValueConstraints(), true);
            addPartialMipConstraints(mip, bidderPartialMIP.generatePiecewiseLinearFunctionConstraints(), true);
            mip.addObjectiveTerm(1, bidderPartialMIP.getwVariable());

            for (SRVMBand band : bidder.getWorld().getBands()) {
                mip.addObjectiveTerm(1, bidderPartialMIP.getzVariable(band));
            }
        }

        // Solve MIP
        SolverClient solverClient = new SolverClient();
        IMIPResult result = solverClient.solve(mip);

        // Test w values
        for (Entry<SRVMBidder, SRVMBidderPartialMIP> partialMip : bidderPartialMips.entrySet()) {
            SRVMBidder bidder = partialMip.getKey();
            int count = 0;
            for (SRVMBand band : bidder.getWorld().getBands()) {
                Variable zVar = partialMip.getValue().getzVariable(band);
                if (result.getValue(zVar) == 1.0) count++;
            }
            boolean expected = count >= 2;
            boolean resultW = result.getValue(partialMip.getValue().getwVariable()) == 1.0;
            Assert.assertEquals("Expected W to be true, but it's false.", resultW, expected);
        }
    }

    private void testInterBandSynergyConstraints(Map<SRVMBidder, SRVMBidderPartialMIP> bidderPartialMips) {
        SRVMWorldPartialMip worldPartialMip = bidderPartialMips.values().stream().findFirst().get().worldPartialMip;
        MIP mip = new MIP();
        // Append also objective terms
        worldPartialMip.appendToMip(mip);
        mip.setObjectiveMax(true);

        for (Map.Entry<SRVMBidder, SRVMBidderPartialMIP> entry : bidderPartialMips.entrySet()) {
            SRVMBidder bidder = entry.getKey();
            SRVMBidderPartialMIP bidderPartialMIP = entry.getValue();
            bidderPartialMIP.appendVariablesToMip(mip);

            addConstraints(mip, bidderPartialMIP.generatezConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generatewConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generateInterBandSynergyConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generateValueConstraints(), true);
            addPartialMipConstraints(mip, bidderPartialMIP.generatePiecewiseLinearFunctionConstraints(), true);

            // Fix values for v_i_b
            int count = 0;
            for (SRVMBand band : bidder.getWorld().getBands()) {
                Constraint c = new Constraint(CompareType.EQ, bidder.getLongId() * 100 + count++);
                c.addTerm(1, bidderPartialMIP.getValueVariable(band));
                mip.add(c);
            }

        }

        // Solve MIP
        SolverClient solverClient = new SolverClient();
        IMIPResult result = solverClient.solve(mip);

        // Test VM and VO values
        boolean noAssertions = true;
        for (Entry<SRVMBidder, SRVMBidderPartialMIP> partialMip : bidderPartialMips.entrySet()) {
            SRVMBidder bidder = partialMip.getKey();
            Variable wVar = partialMip.getValue().getwVariable();
            int count = 0;
            for (SRVMBand band : bidder.getWorld().getBands()) {
                double expected = bidder.getLongId() * 100 + count;
                Variable vmVar = worldPartialMip.getVmVariable(bidder, band);
                Variable voVar = worldPartialMip.getVoVariable(bidder, band);
                double vmResult = result.getValue(vmVar);
                double voResult = result.getValue(voVar);
                if (result.getValue(wVar) == 1.0) {
                    Assert.assertEquals("Expected " + vmVar.getName() + " to be " + expected + ", but it's " + vmResult, vmResult, expected, 0.000001);
                    Assert.assertEquals("Expected " + voVar.getName() + " to be 0.0, but it's " + voResult, voResult, 0.0, 0.0000001);
                    noAssertions = false;
                } else if (result.getValue(wVar) == 0.0) {
                    Assert.assertEquals("Expected " + voVar.getName() + " to be " + expected + ", but it's " + voResult, voResult, expected, 0.0000001);
                    Assert.assertEquals("Expected " + vmVar.getName() + " to be 0.0, but it's " + vmResult, vmResult, 0.0, 0.0000001);
                    noAssertions = false;
                } else {
                    Assert.fail("wVar is neither 1.0 nor 0.0!");
                }
                count++;
            }
        }
        Assert.assertFalse(noAssertions);
    }

    private void testValueConstraintsSetup(Map<SRVMBidder, SRVMBidderPartialMIP> bidderPartialMips) {
        SRVMWorldPartialMip worldPartialMip = bidderPartialMips.values().stream().findFirst().get().worldPartialMip;
        MIP mip = new MIP();
        // Append also objective terms
        worldPartialMip.appendToMip(mip);
        mip.setObjectiveMax(true);

        for (Map.Entry<SRVMBidder, SRVMBidderPartialMIP> entry : bidderPartialMips.entrySet()) {
            SRVMBidder bidder = entry.getKey();
            SRVMBidderPartialMIP bidderPartialMIP = entry.getValue();
            bidderPartialMIP.appendVariablesToMip(mip);

            addConstraints(mip, bidderPartialMIP.generatezConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generatewConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generateInterBandSynergyConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generateValueConstraints(), false);
            addPartialMipConstraints(mip, bidderPartialMIP.generatePiecewiseLinearFunctionConstraints(), true);

            // Fix values for alpha, beta and gamma
            for (SRVMBand band : bidder.getWorld().getBands()) {
                Constraint c1 = new Constraint(CompareType.EQ, 2);
                c1.addTerm(1, bidderPartialMIP.getAlphaVariable(band));
                mip.add(c1);
                Constraint c2 = new Constraint(CompareType.EQ, 3);
                c2.addTerm(1, bidderPartialMIP.getBetaVariable(band));
                mip.add(c2);
                Constraint c3 = new Constraint(CompareType.EQ, 4);
                c3.addTerm(1, bidderPartialMIP.getGammaVariable(band));
                mip.add(c3);
            }
        }

        // Solve MIP
        SolverClient solverClient = new SolverClient();
        IMIPResult result = solverClient.solve(mip);

        // Test v_i_b values
        boolean noAssertions = true;
        for (Entry<SRVMBidder, SRVMBidderPartialMIP> partialMip : bidderPartialMips.entrySet()) {
            SRVMBidder bidder = partialMip.getKey();
            for (SRVMBand band : bidder.getWorld().getBands()) {
                Variable alpha = partialMip.getValue().getAlphaVariable(band);
                Variable beta = partialMip.getValue().getBetaVariable(band);
                Variable gamma = partialMip.getValue().getGammaVariable(band);
                Variable v_i_b = partialMip.getValue().getValueVariable(band);
                double valueResult = result.getValue(v_i_b);
                double alphaResult = result.getValue(alpha);
                double betaResult = result.getValue(beta);
                double gammaResult = result.getValue(gamma);
                Assert.assertEquals("Alpha should be 2, it's " + alphaResult, alphaResult, 2, 0);
                Assert.assertEquals("Beta should be 3, it's " + betaResult, betaResult, 3, 0);
                Assert.assertEquals("Gamma should be 4, it's " + gammaResult, gammaResult, 4, 0);
                double baseValue = bidder.getBaseValues().get(band.getName()).doubleValue();
                double syn_i_b = bidder.getIntrabandSynergyFactors().get(band.getName()).doubleValue();
                double expected = baseValue * alphaResult + baseValue * syn_i_b * betaResult + baseValue * gammaResult;
                Assert.assertEquals(valueResult, expected, 0.001);
                noAssertions = false;
            }
        }
        Assert.assertFalse(noAssertions);
    }

    private void testPiecewiseLinearFunctionSetup(Map<SRVMBidder, SRVMBidderPartialMIP> bidderPartialMips) {
        SRVMWorldPartialMip worldPartialMip = bidderPartialMips.values().stream().findFirst().get().worldPartialMip;
        MIP mip = new MIP();
        // Append also objective terms
        worldPartialMip.appendToMip(mip);
        mip.setObjectiveMax(true);

        for (Map.Entry<SRVMBidder, SRVMBidderPartialMIP> entry : bidderPartialMips.entrySet()) {
            SRVMBidder bidder = entry.getKey();
            SRVMBidderPartialMIP bidderPartialMIP = entry.getValue();
            bidderPartialMIP.appendVariablesToMip(mip);

            addConstraints(mip, bidderPartialMIP.generatezConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generatewConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generateInterBandSynergyConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generateValueConstraints(), false);
            addPartialMipConstraints(mip, bidderPartialMIP.generatePiecewiseLinearFunctionConstraints(), false);
        }

        // Solve MIP
        SolverClient solverClient = new SolverClient();
        IMIPResult result = solverClient.solve(mip);

        // Test alpha, beta and gamma values
        boolean noAssertions = true;
        for (Entry<SRVMBidder, SRVMBidderPartialMIP> partialMip : bidderPartialMips.entrySet()) {
            SRVMBidder bidder = partialMip.getKey();
            for (SRVMBand band : bidder.getWorld().getBands()) {
                Variable alpha = partialMip.getValue().getAlphaVariable(band);
                Variable beta = partialMip.getValue().getBetaVariable(band);
                Variable gamma = partialMip.getValue().getGammaVariable(band);
                Variable x = worldPartialMip.getXVariable(bidder, band);
                double alphaResult = result.getValue(alpha);
                double betaResult = result.getValue(beta);
                double gammaResult = result.getValue(gamma);
                int xResult = (int) result.getValue(x);
                double alphaExpected = getAlpha(bidder.getSynergyThreshold().get(band.getName()), xResult);
                double betaExpected = getBeta(bidder.getSynergyThreshold().get(band.getName()), xResult);
                double gammaExpected = getGamma(bidder.getSynergyThreshold().get(band.getName()), xResult);
                Assert.assertEquals("Alpha should be " + alphaExpected + ", it's " + alphaResult, alphaExpected, alphaResult, 0.00000001);
                Assert.assertEquals("Beta should be " + betaExpected + ", it's " + betaResult, betaExpected, betaResult, 0.0000001);
                Assert.assertEquals("Gamma should be " + gammaExpected + ", it's " + gammaResult, gammaExpected, gammaResult, 0.0000001);
                noAssertions = false;
            }
        }
        Assert.assertFalse(noAssertions);
    }

    private void addPartialMipConstraints(MIP mip, Set<PartialMIP> partialMIPs, boolean mock) {
        if (!mock) {
            partialMIPs.forEach(partialMIP -> partialMIP.appendConstraintsToMip(mip));
            return;
        }
        for (PartialMIP partialMIP : partialMIPs) {
            for (Variable var : partialMIP.getVariables()) {
                Constraint c = new Constraint(CompareType.LEQ, MIP.MAX_VALUE);
                c.addTerm(1, var);
                mip.add(c);
            }
        }
    }

    private void addConstraints(MIP mip, Set<Constraint> constraints, boolean mock) {
        if (!mock) {
            constraints.forEach(mip::add);
            return;
        }
        Set<Variable> vars = new HashSet<>();
        for (Constraint constraint : constraints) {
            for (LinearTerm term : constraint.getLinearTerms()) {
                vars.add(mip.getVar(term.getVarName()));
            }
        }
        for (Variable var : vars) {
            Constraint c = new Constraint(CompareType.LEQ, MIP.MAX_VALUE);
            c.addTerm(1, var);
            mip.add(c);
        }
    }

    private double getAlpha(int threshold, int xResult) {
        return Math.min(threshold, xResult);
    }

    private double getBeta(int threshold, int xResult) {
        double firstTerm = 0;
        if (threshold > 0) firstTerm = (threshold - 1.0) / threshold;
        double secondTerm = 0;
        if (xResult > 0) secondTerm = (xResult - 1.0) / xResult;
        return Math.min(firstTerm, secondTerm);
    }

    private double getGamma(int threshold, int xResult) {
        if (threshold <= xResult) return Math.max(0, Math.log(xResult - threshold + 1));
        else return 0.0;
    }
}

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
import org.spectrumauctions.sats.opt.imip.PartialMIP;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Fabio Isler
 */
public class BidderPartialMIPTest {

    private static Map<SRVMBidder, SRVMBidderPartialMIP> singleBandSingleBidderPartialMips;
    private static Map<SRVMBidder, SRVMBidderPartialMIP> singleBidderPartialMips;
    private static Map<SRVMBidder, SRVMBidderPartialMIP> singleBandPartialMips;
    private static Map<SRVMBidder, SRVMBidderPartialMIP> singleBandTenBiddersPrtialMips;
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
        singleBandTenBiddersPrtialMips = createBidderPartialMIPs(SRVMWorldGen.getSingleBandWorldSetup(), SRVMWorldGen.getSimpleSmallBidderSetup(10));
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
        testZConstraints(singleBandTenBiddersPrtialMips, 4); // 6 Licsnese, 10 bidders
        testZConstraints(standardBidderPartialMips, 1); // 6 Licenses in Band A, 7 bidders
    }

    @Test
    public void testWConstraintsSetups() {
        testWConstraints(standardBidderPartialMips);
        testWConstraints(singleBandPartialMips);
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
            addConstraints(mip, bidderPartialMIP.generateValueConstraints(), true);
            addConstraints(mip, bidderPartialMIP.generateInterBandSynergyConstraints(), true);
            addConstraints(mip, bidderPartialMIP.generatePiecewiseLinearFunctionConstraints());
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
            addConstraints(mip, bidderPartialMIP.generatePiecewiseLinearFunctionConstraints());

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

    private void addConstraints(MIP mip, Set<PartialMIP> partialMIPs) {
        partialMIPs.forEach(partialMIP -> partialMIP.appendConstraintsToMip(mip));
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
}

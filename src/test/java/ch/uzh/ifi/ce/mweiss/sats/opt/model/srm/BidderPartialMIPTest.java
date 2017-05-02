package ch.uzh.ifi.ce.mweiss.sats.opt.model.srm;

import ch.uzh.ifi.ce.mweiss.specval.model.srm.*;
import ch.uzh.ifi.ce.mweiss.specval.util.PreconditionUtils;
import ch.uzh.ifi.ce.mweiss.specval.util.random.JavaUtilRNGSupplier;
import ch.uzh.ifi.ce.mweiss.sats.opt.imip.PartialMIP;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Fabio Isler
 */
public class BidderPartialMIPTest {

    private static Map<SRMBidder, SRMBidderPartialMIP> singleBandSingleBidderPartialMips;
    private static Map<SRMBidder, SRMBidderPartialMIP> singleBidderPartialMips;
    private static Map<SRMBidder, SRMBidderPartialMIP> singleBandPartialMips;
    private static Map<SRMBidder, SRMBidderPartialMIP> singleBandTenBiddersPrtialMips;
    private static Map<SRMBidder, SRMBidderPartialMIP> standardBidderPartialMips;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Set<SRMBidderSetup> setups = SRMWorldGen.getSimpleSmallBidderSetup(1);
        // Single band, single bidder
        singleBandSingleBidderPartialMips = createBidderPartialMIPs(SRMWorldGen.getSingleBandWorldSetup(), setups);
        // Standard world, single bidder
        singleBidderPartialMips = createBidderPartialMIPs(SRMWorldGen.getStandardWorldBuilder(), setups);
        // Single band
        singleBandPartialMips = createBidderPartialMIPs(SRMWorldGen.getSingleBandWorldSetup(), SRMWorldGen.getStandardBidderSetups());
        // Single band, three bidders
        singleBandTenBiddersPrtialMips = createBidderPartialMIPs(SRMWorldGen.getSingleBandWorldSetup(), SRMWorldGen.getSimpleSmallBidderSetup(10));
        // Standard world
        standardBidderPartialMips = createBidderPartialMIPs(SRMWorldGen.getStandardWorldBuilder(), SRMWorldGen.getStandardBidderSetups());
    }

    private static Map<SRMBidder, SRMBidderPartialMIP> createBidderPartialMIPs(SRMWorldSetup worldSetup, Set<SRMBidderSetup> bidderSetups) {
        // Standard world, single bidder
        SRMWorld standardWorld = new SRMWorld(worldSetup, new JavaUtilRNGSupplier(153578351L));
        List<SRMBidder> bidders = standardWorld.createPopulation(bidderSetups, new JavaUtilRNGSupplier(15434684L));
        double scalingFactor = SRM_MIP.calculateScalingFactor(bidders);
        double biggestScaledValue = SRM_MIP.biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        SRMWorldPartialMip worldPartialMip = new SRMWorldPartialMip(bidders, biggestScaledValue, scalingFactor);
        Map<SRMBidder, SRMBidderPartialMIP> bidderPartialMIPs = new HashMap<>();
        for (SRMBidder bidder : bidders) {
            bidderPartialMIPs.put(bidder, new SRMBidderPartialMIP(bidder, worldPartialMip));
        }
        return bidderPartialMIPs;
    }

    /**
     * Test method for {@link SRMBidderPartialMIP#generatezConstraints()}.
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

    private void testWConstraints(Map<SRMBidder, SRMBidderPartialMIP> bidderPartialMips) {
        SRMWorldPartialMip worldPartialMip = bidderPartialMips.values().stream().findFirst().get().worldPartialMip;
        MIP mip = new MIP();
        worldPartialMip.appendVariablesToMip(mip);
        worldPartialMip.appendConstraintsToMip(mip);
        mip.setObjectiveMax(true);

        for (Map.Entry<SRMBidder, SRMBidderPartialMIP> entry : bidderPartialMips.entrySet()) {
            SRMBidder bidder = entry.getKey();
            SRMBidderPartialMIP bidderPartialMIP = entry.getValue();
            bidderPartialMIP.appendVariablesToMip(mip);

            addConstraints(mip, bidderPartialMIP.generatezConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generatewConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generateValueConstraints(), true);
            addConstraints(mip, bidderPartialMIP.generateInterBandSynergyConstraints(), true);
            addConstraints(mip, bidderPartialMIP.generatePiecewiseLinearFunctionConstraints());
            mip.addObjectiveTerm(1, bidderPartialMIP.getwVariable());

            for (SRMBand band : bidder.getWorld().getBands()) {
                mip.addObjectiveTerm(1, bidderPartialMIP.getzVariable(band));
            }
        }

        // Solve MIP
        SolverClient solverClient = new SolverClient();
        IMIPResult result = solverClient.solve(mip);

        // Test w values
        for (Entry<SRMBidder, SRMBidderPartialMIP> partialMip : bidderPartialMips.entrySet()) {
            SRMBidder bidder = partialMip.getKey();
            int count = 0;
            for (SRMBand band : bidder.getWorld().getBands()) {
                Variable zVar = partialMip.getValue().getzVariable(band);
                if (result.getValue(zVar) == 1.0) count++;
            }
            boolean expected = count >= 2;
            boolean resultW = result.getValue(partialMip.getValue().getwVariable()) == 1.0;
            Assert.assertEquals("Expected W to be true, but it's false.", resultW, expected);
        }
    }

    private void testZConstraints(Map<SRMBidder, SRMBidderPartialMIP> bidderPartialMips, int falseZ) {
        PreconditionUtils.checkNotNegative(falseZ);
        SRMWorldPartialMip worldPartialMip = bidderPartialMips.values().stream().findFirst().get().worldPartialMip;
        MIP mip = new MIP();
        worldPartialMip.appendVariablesToMip(mip);
        worldPartialMip.appendConstraintsToMip(mip);
        mip.setObjectiveMax(true);

        for (Map.Entry<SRMBidder, SRMBidderPartialMIP> entry : bidderPartialMips.entrySet()) {
            SRMBidder bidder = entry.getKey();
            SRMBidderPartialMIP bidderPartialMIP = entry.getValue();
            bidderPartialMIP.appendVariablesToMip(mip);

            addConstraints(mip, bidderPartialMIP.generatezConstraints(), false);
            addConstraints(mip, bidderPartialMIP.generatewConstraints(), true);
            addConstraints(mip, bidderPartialMIP.generateValueConstraints(), true);
            addConstraints(mip, bidderPartialMIP.generateInterBandSynergyConstraints(), true);
            addConstraints(mip, bidderPartialMIP.generatePiecewiseLinearFunctionConstraints());

            for (SRMBand band : bidder.getWorld().getBands()) {
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
        for (Entry<SRMBidder, SRMBidderPartialMIP> partialMip : bidderPartialMips.entrySet()) {
            SRMBidder bidder = partialMip.getKey();
            for (SRMBand band : bidder.getWorld().getBands()) {
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

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.mrvm;

import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * @author Michael Weiss
 */
public class MRVMOverallValueTest {

    private static final double RELATIVE_ALLOWED_ASSERTION_DELTA = 0.001;

    private boolean hasFailed = false;
    private static boolean LOG_ASSERTS_OF_FAILED_STEP = true;
    private static boolean LOG_ALL_STAGES = false;

    @Test
    public void mipValuesEqualSATSValues() {
        List<MRVMBidder> bidders = new MultiRegionModel().createNewPopulation();
        //Sort by bidder type
        bidders.sort(Comparator.comparing(b -> b.getClass().getSimpleName()));
        MRVM_MIP mip = new MRVM_MIP(bidders);
        MRVMMipResult result = mip.calculateAllocation();
        List<String> errors;
        errors = assertAllocatonTransformationAndCapCalculation(result, result.getJoptResult(), bidders, mip);
        failIfFailed(errors);
        errors = assertCValues(result, result.getJoptResult(), bidders, mip);
        failIfFailed(errors);
        errors = assertSubscriberValues(result, result.getJoptResult(), bidders, mip);
        failIfFailed(errors);
        errors = assertOmegaValues(result, result.getJoptResult(), bidders, mip);
        failIfFailed(errors);
        errors = assertGammaFactorsValue(result, result.getJoptResult(), bidders, mip);
        failIfFailed(errors);
        errors = assertOverallValue(result, result.getJoptResult(), bidders, mip);
        failIfFailed(errors);
    }

    private List<String> assertAllocatonTransformationAndCapCalculation(MRVMMipResult result, IMIPResult imipResult, List<MRVMBidder> bidders, MRVM_MIP mrvm_mip) {
        List<String> assertionResults = new ArrayList<>();
        for (MRVMBidder bidder : bidders) {
            GenericValue<MRVMGenericDefinition> val = result.getAllocation(bidder);
            int cplexBundleSize = 0;
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                for (MRVMBand band : bidder.getWorld().getBands()) {
                    //Check transformation to allocation is correct
                    int anyBundleSize = val.anyConsistentBundle().size();
                    Assert.assertEquals(val.getSize(), anyBundleSize);
                    Variable xVariable = mrvm_mip.getWorldPartialMip().getXVariable(bidder, region, band);
                    double xVarValue = imipResult.getValue(xVariable);
                    if (xVarValue % 1 != 0) {
                        fail("xVarValue not integer " + xVarValue);
                    }
                    cplexBundleSize += Math.rint(xVarValue);
                    int thisDefQuantity = val.getQuantity(new MRVMGenericDefinition(band, region));
                    assertionResults.add(assertEqualsAndGiveResult(xVarValue, (double) thisDefQuantity, bidder, "allocated-bundle-quantity"));
                    //Check CAP Variable is correct
                    BigDecimal modelCapOfBand = MRVMWorld.capOfBand(band, thisDefQuantity);
                    Variable capVariable = mrvm_mip.getBidderPartialMips().get(bidder).getCapVariable(region, band);
                    double capVarValue = imipResult.getValue(capVariable);
                    assertionResults.add(assertEqualsAndGiveResult(modelCapOfBand.doubleValue(), capVarValue, bidder, "cap-value"));
                }
            }
            Assert.assertEquals(val.getSize(), cplexBundleSize);
        }
        return assertionResults;
    }


    private List<String> assertCValues(MRVMMipResult result, IMIPResult imipResult, List<MRVMBidder> bidders, MRVM_MIP mrvm_mip) {
        List<String> assertionResults = new ArrayList<>();
        for (MRVMBidder bidder : bidders) {
            GenericValue<MRVMGenericDefinition> val = result.getAllocation(bidder);
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                //Check c variables
                BigDecimal c = MRVMWorld.c(region, (Bundle<MRVMLicense>) val.anyConsistentBundle());
                Variable cVariable = mrvm_mip.getBidderPartialMips().get(bidder).getCVariable(region);
                double mipC = imipResult.getValue(cVariable);
                assertionResults.add(assertEqualsAndGiveResult(c.doubleValue(), mipC, bidder, "c-variable"));
            }
        }
        return assertionResults;
    }

    private List<String> assertSubscriberValues(MRVMMipResult result, IMIPResult imipResult, List<MRVMBidder> bidders, MRVM_MIP mrvm_mip) {
        List<String> assertionResults = new ArrayList<>();
        for (MRVMBidder bidder : bidders) {
            GenericValue<MRVMGenericDefinition> val = result.getAllocation(bidder);
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                //Equivalence of MIP and Model c value tested in previous step of this mip.
                BigDecimal c = MRVMWorld.c(region, (Bundle<MRVMLicense>) val.anyConsistentBundle());
                BigDecimal sv = bidder.svFunction(region, c);
                Variable svVariable = mrvm_mip.getBidderPartialMips().get(bidder).getSVVariable(region);
                double mipSV = imipResult.getValue(svVariable);
                //Note: Sv scaling is not yet done in this step, but in the next one, where Omega is created
                assertionResults.add(assertEqualsAndGiveResult(sv.doubleValue(), mipSV, bidder, "sv-value"));
            }
        }
        return assertionResults;
    }

    private List<String> assertOmegaValues(MRVMMipResult result, IMIPResult imipResult, List<MRVMBidder> bidders, MRVM_MIP mrvm_mip) {
        List<String> assertionResults = new ArrayList<>();
        for (MRVMBidder bidder : bidders) {
            GenericValue<MRVMGenericDefinition> val = result.getAllocation(bidder);
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                //Equivalence of MIP and Model sv value tested in previous step of this mip.
                BigDecimal c = MRVMWorld.c(region, (Bundle<MRVMLicense>) val.anyConsistentBundle());
                BigDecimal sv = bidder.svFunction(region, c);
                BigDecimal expectedOmega = bidder.omegaFactor(region, sv);
                Variable omegaVariable = mrvm_mip.getBidderPartialMips().get(bidder).getOmegaVariable(region);
                double omegaVal = imipResult.getValue(omegaVariable);

                //Unscale SV-Scaling
                double svScalingFactor = mrvm_mip.getBidderPartialMips().get(bidder).getScalingFactor();
                double oWithSvUnscaled = svScalingFactor * omegaVal;
                // Unscaling of OmegaScaling is only done in next step
                assertionResults.add(assertEqualsAndGiveResult(expectedOmega.doubleValue(), oWithSvUnscaled, bidder, "omega"));
            }
        }
        return assertionResults;
    }

    private List<String> assertGammaFactorsValue(MRVMMipResult result, IMIPResult joptResult, List<MRVMBidder> bidders, MRVM_MIP mip) {
        List<String> assertionResults = new ArrayList<>();
        for (MRVMBidder bidder : bidders) {
            if (MRVMLocalBidder.class.isAssignableFrom(bidder.getClass())) {
                assertionResults.addAll(assertLocalValueAndThusGammaFactors(result, joptResult, bidder, mip));
            } else if (MRVMRegionalBidder.class.isAssignableFrom(bidder.getClass())) {
                assertionResults.addAll(assertRegionalGammaFactorsValue(result, joptResult, bidder, mip));
            } else if (MRVMNationalBidder.class.isAssignableFrom(bidder.getClass())) {
                //TODO Test National
                assertionResults.addAll(assertNationalGammaFactorsValue(result, joptResult, bidder, mip));
            } else {
                throw new NullPointerException("Unknown Bidder Type");
            }
        }
        return assertionResults;
    }

    private List<String> assertLocalValueAndThusGammaFactors(MRVMMipResult result, IMIPResult joptResult, MRVMBidder bidder, MRVM_MIP mip) {
        List<String> assertionResults = new ArrayList<>();
        MRVMLocalBidderPartialMip localBidderPartialMip = (MRVMLocalBidderPartialMip) mip.getBidderPartialMips().get(bidder);
        //There is no gamma variable, thus we check if the sum is made correctly
        BigDecimal cplexOmegaSum = BigDecimal.ZERO;
        BigDecimal manualOmegaSum = BigDecimal.ZERO;
        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            Variable omegaVariable = localBidderPartialMip.getOmegaVariable(region);
            double mipOmega = joptResult.getValue(omegaVariable);
            BigDecimal bdMipOmega = BigDecimal.valueOf(mipOmega);
            cplexOmegaSum = cplexOmegaSum.add(cplexOmegaSum);
            if (bidder.gammaFactor(region, (Bundle<MRVMLicense>) result.getAllocation(bidder).anyConsistentBundle()) == BigDecimal.ONE) {
                BigDecimal c = MRVMWorld.c(region, (Bundle<MRVMLicense>) result.getAllocation(bidder).anyConsistentBundle());
                BigDecimal sv = bidder.svFunction(region, c);
                BigDecimal omega = bidder.omegaFactor(region, sv);
                manualOmegaSum.add(omega);
            }
        }
        double svScaling = localBidderPartialMip.getScalingFactor();
        BigDecimal unscaledValue = cplexOmegaSum.multiply(BigDecimal.valueOf(svScaling));
        BigDecimal satsValue = bidder.calculateValue(result.getAllocation(bidder).getQuantities());
        assertionResults.add(assertEqualsAndGiveResult(unscaledValue, satsValue, bidder, "local bidder gamma test "));
        assertionResults.add(assertEqualsAndGiveResult(unscaledValue, manualOmegaSum, bidder, "local bidder gamma test (manual)"));
        return assertionResults;
    }

    /**
     * Doesn't really test anything of the mip (as no gamma variable), but makes sure sats-core is consistent with what mip is supposed to encode
     *
     * @param result
     * @param joptResult
     * @param bidder
     * @param mip
     * @return
     */
    private List<String> assertRegionalGammaFactorsValue(MRVMMipResult result, IMIPResult joptResult, MRVMBidder bidder, MRVM_MIP mip) {
        List<String> assertionResults = new ArrayList<>();
        MRVMRegionalBidderPartialMip localBidderPartialMip = (MRVMRegionalBidderPartialMip) mip.getBidderPartialMips().get(bidder);
        //There is no gamma variable, thus we only check if sats-core impl is consistent with was mip is supposed to do
        BigDecimal manualOmegaSum = BigDecimal.ZERO;
        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            BigDecimal gammaFactor = bidder.gammaFactor(region, (Bundle<MRVMLicense>) result.getAllocation(bidder).anyConsistentBundle());
            BigDecimal c = MRVMWorld.c(region, (Bundle<MRVMLicense>) result.getAllocation(bidder).anyConsistentBundle());
            BigDecimal sv = bidder.svFunction(region, c);
            BigDecimal omega = bidder.omegaFactor(region, sv);
            BigDecimal discountedOmega = gammaFactor.multiply(omega);
            manualOmegaSum.add(discountedOmega);
        }
        assertionResults.add(assertEqualsAndGiveResult(manualOmegaSum, manualOmegaSum, bidder, "local bidder sats-core gamma test (manual)"));
        return assertionResults;
    }


    private List<String> assertNationalGammaFactorsValue(MRVMMipResult result, IMIPResult joptResult, MRVMBidder bidders, MRVM_MIP mip) {
        List<String> assertionResults = new ArrayList<>();
        //TODO
        return assertionResults;
    }


    private List<String> assertOverallValue(MRVMMipResult result, IMIPResult joptResult, List<MRVMBidder> bidders, MRVM_MIP mip) {
        List<String> assertionResults = new ArrayList<>();
        for (MRVMBidder bidder : bidders) {
            GenericValue<MRVMGenericDefinition> outcomeVal = result.getAllocation(bidder);
            BigDecimal satsVal = bidder.calculateValue(outcomeVal.getQuantities());
            BigDecimal nonGenericSatsVal = bidder.calculateValue((Bundle<MRVMLicense>) outcomeVal.anyConsistentBundle());
            assertionResults.add(assertEqualsAndGiveResult(satsVal.doubleValue(), nonGenericSatsVal.doubleValue(), bidder, "test-anyconsistentbundle-values"));
            Variable valVariable = mip.getWorldPartialMip().getValueVariable(bidder);
            double cplexVal = joptResult.getValue(valVariable);
            //Unscaling
            double svScalingFactor = mip.getBidderPartialMips().get(bidder).getScalingFactor();
            double unscaledCplexVal = cplexVal * svScalingFactor;
            assertionResults.add(assertEqualsAndGiveResult(satsVal.doubleValue(), unscaledCplexVal, bidder, "overall-value-cplex"));
            //Check allocation transformation is made correctly
            BigDecimal actual = outcomeVal.getValue();
            assertionResults.add(assertEqualsAndGiveResult(satsVal.doubleValue(), actual.doubleValue(), bidder, "overall-value-allocation"));
        }
        return assertionResults;
    }

    private void failIfFailed(List<String> errors) {
        if (hasFailed) {
            if (LOG_ASSERTS_OF_FAILED_STEP) {
                for (String error : errors) {
                    System.out.println(error);
                }
            }
            fail();
        } else {
            if (LOG_ALL_STAGES) {
                for (String noerror : errors) {
                    System.out.println(noerror);
                }
            }
        }
    }

    private String assertEqualsAndGiveResult(BigDecimal expected, BigDecimal actual, MRVMBidder bidder, String logName) {
        //Set procentual margin, while ensuring minimal margin.
        BigDecimal allowedMargin = expected.multiply(BigDecimal.valueOf(RELATIVE_ALLOWED_ASSERTION_DELTA));
        if (allowedMargin.compareTo(BigDecimal.valueOf(0.0001)) < 0) {
            allowedMargin = BigDecimal.valueOf(0.0001);
        }
        //Do assertion and logging
        try {
            Assert.assertTrue(
                    expected.add(allowedMargin).compareTo(actual) >= 0 &&
                            expected.subtract(allowedMargin).compareTo(actual) <= 0
            );
        } catch (AssertionError e) {
            hasFailed = true;
            return ("     " + logName + " FAILED for " + bidder.getSetupType() + " " + bidder.getId() + ". Expected:" + expected.toString() + " actual " + actual.toString());
        }
        return ("     " + logName + " PASSED for " + bidder.getSetupType() + " " + bidder.getId() + ". Value:" + expected);
    }

    private String assertEqualsAndGiveResult(double expected, double actual, MRVMBidder bidder, String logName) {
        double allowedMargin = expected * RELATIVE_ALLOWED_ASSERTION_DELTA;
        if (allowedMargin < 0.0001) {
            allowedMargin = 0.0001;
        }
        try {
            Assert.assertEquals(expected, actual, allowedMargin);
        } catch (AssertionError e) {
            hasFailed = true;
            return ("     " + logName + " FAILED for " + bidder.getSetupType() + " " + bidder.getId() + ". Cause:" + e.getMessage());
        }
        return ("     " + logName + " PASSED for " + bidder.getSetupType() + " " + bidder.getId() + ". Value:" + expected);
    }
}

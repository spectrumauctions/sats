/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.srm;

import com.google.common.base.Preconditions;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.spectrumauctions.sats.core.bidlang.generic.Band;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.srm.SRMBidder;
import org.spectrumauctions.sats.core.util.math.ContinuousPiecewiseLinearFunction;
import org.spectrumauctions.sats.opt.imip.PartialMIP;
import org.spectrumauctions.sats.opt.imip.PiecewiseLinearPartialMIP;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Weiss
 */
public class SRMBidderPartialMIP extends PartialMIP {

    private static final String zVariablePrefix = "Z";
    private static final String wVariablePrefix = "W";
    private static final String valueVariablePrefix = "v";

    private Map<Band, Variable> zVariables;
    private Variable wVariable;
    private Map<Band, Variable> valueVariables;
    private Map<Band, Variable> alphaVariables;
    private Map<Band, Variable> betaVariables;
    private Map<Band, Variable> gammaVariables;
    protected final SRMWorldPartialMip worldPartialMip;
    private final SRMBidder bidder;

    public SRMBidderPartialMIP(SRMBidder bidder, SRMWorldPartialMip worldMip) {
        this.bidder = bidder;
        this.worldPartialMip = worldMip;
        initVariables();
    }

    private void initVariables() {
        this.zVariables = createZVariables();
        this.wVariable = new Variable(wVariablePrefix + "_" + bidder.getId(), VarType.BOOLEAN, 0, 1);
        this.valueVariables = createValueVariables();
        this.alphaVariables = createHelperVariables("alpha");
        this.betaVariables = createHelperVariables("beta");
        this.gammaVariables = createHelperVariables("gamma");
    }

    private Map<Band, Variable> createZVariables() {
        Map<Band, Variable> result = new HashMap<>();
        for (Band band : bidder.getWorld().getBands()) {
            String varName = zVariablePrefix.concat(createIndex(bidder, band));
            Variable var = new Variable(varName, VarType.BOOLEAN, 0, 1);
            result.put(band, var);
        }
        return result;
    }

    private Map<Band, Variable> createHelperVariables(String prefix) {
        Map<Band, Variable> result = new HashMap<>();
        for (Band band : bidder.getWorld().getBands()) {
            String varName = prefix.concat(createIndex(bidder, band));
            Variable var = new Variable(varName, VarType.DOUBLE, 0, MIP.MAX_VALUE);
            result.put(band, var);
        }
        return result;
    }

    private Map<Band, Variable> createValueVariables() {
        Map<Band, Variable> result = new HashMap<>();
        for (Band band : bidder.getWorld().getBands()) {
            String varName = valueVariablePrefix.concat(createIndex(bidder, band));
            Variable var = new Variable(varName, VarType.DOUBLE, 0, MIP.MAX_VALUE);
            result.put(band, var);
        }
        return result;
    }

    /**
     * @return
     * @throws NullPointerException if no variable is defined for this region
     */
    Variable getzVariable(Band band) {
        Variable var = zVariables.get(band);
        Preconditions.checkNotNull(var);
        return var;
    }

    /**
     * @return
     * @throws NullPointerException if no variable is defined for this region
     */
    Variable getwVariable() {
        return wVariable;
    }

    Variable getValueVariable(Band band) {
        Variable var = valueVariables.get(band);
        Preconditions.checkNotNull(var);
        return var;
    }

    Variable getAlphaVariable(Band band) {
        Variable var = alphaVariables.get(band);
        Preconditions.checkNotNull(var);
        return var;
    }

    Variable getBetaVariable(Band band) {
        Variable var = betaVariables.get(band);
        Preconditions.checkNotNull(var);
        return var;
    }

    Variable getGammaVariable(Band band) {
        Variable var = gammaVariables.get(band);
        Preconditions.checkNotNull(var);
        return var;
    }

    static String createIndex(Bidder<?> bidder, Band band) {
        StringBuilder builder = new StringBuilder("_b");
        builder.append(bidder.getId());
        builder.append(",band_");
        builder.append(band.getName());
        return builder.toString();
    }

    /**
     * @return
     */
    Set<Constraint> generatezConstraints() {
        Set<Constraint> result = new HashSet<>();
        for (Band band : bidder.getWorld().getBands()) {
            Variable x = worldPartialMip.getXVariable(bidder, band);
            // Add Z_i_b >= 1/n_b * X_b constraint
            Constraint zgeq = new Constraint(CompareType.LEQ, 0);
            zgeq.addTerm(-1, getzVariable(band));
            zgeq.addTerm(1.0 / band.getNumberOfLicenses(), x);
            result.add(zgeq);
            // Add Z_i_b <= X_i_b constraint
            Constraint zleq = new Constraint(CompareType.GEQ, 0);
            zleq.addTerm(-1, getzVariable(band));
            zleq.addTerm(1, x);
            result.add(zleq);
        }
        return result;
    }

    Set<Constraint> generatewConstraints() {
        int sumNb = bidder.getWorld().getBands().size();
        Set<Constraint> result = new HashSet<>();
        Constraint wgeq = new Constraint(CompareType.LEQ, 1.0 / sumNb);
        wgeq.addTerm(-1, getwVariable());
        Constraint wleq = new Constraint(CompareType.GEQ, 0);
        wleq.addTerm(-1, getwVariable());

        for (Band band : bidder.getWorld().getBands()) {
            wgeq.addTerm(1.0 / band.getNumberOfLicenses(), getzVariable(band));
            wleq.addTerm(0.5, getzVariable(band));
        }
        result.add(wgeq);
        result.add(wleq);

        return result;
    }

    /**
     * @return
     */
    Set<Constraint> generateInterBandSynergyConstraints() {
        Set<Constraint> result = new HashSet<>();
        for (Band band : bidder.getWorld().getBands()) {
            Variable vm = worldPartialMip.getVmVariable(bidder, band);
            // Add C.10
            Constraint c1 = new Constraint(CompareType.GEQ, -MIP.MAX_VALUE);
            c1.addTerm(-1, vm);
            c1.addTerm(1, getValueVariable(band));
            c1.addTerm(-MIP.MAX_VALUE, getwVariable());
            result.add(c1);
            // Add C.11
            Constraint c2 = new Constraint(CompareType.LEQ, MIP.MAX_VALUE);
            c2.addTerm(-1, vm);
            c2.addTerm(1, getValueVariable(band));
            c2.addTerm(MIP.MAX_VALUE, getwVariable());
            result.add(c2);
            // Add C.12
            Constraint c3 = new Constraint(CompareType.GEQ, 0);
            c3.addTerm(-1, vm);
            c3.addTerm(MIP.MAX_VALUE, getwVariable());
            result.add(c3);
            // Add C.13
            Constraint c4 = new Constraint(CompareType.EQ, 0);
            c4.addTerm(-1, worldPartialMip.getVoVariable(bidder, band));
            c4.addTerm(1, getValueVariable(band));
            c4.addTerm(-1, vm);
            result.add(c4);
        }
        return result;
    }

    Set<Constraint> generateValueConstraints() {
        Set<Constraint> result = new HashSet<>();
        for (Band band : bidder.getWorld().getBands()) {
            double baseValue = getBaseValue(bidder, band);
            // Add C.17
            Constraint c = new Constraint(CompareType.EQ, 0);
            c.addTerm(-1, getValueVariable(band));
            c.addTerm(baseValue, getAlphaVariable(band));
            c.addTerm(baseValue * getIntrabandSynergyFactor(bidder, band), getBetaVariable(band));
            c.addTerm(baseValue, getGammaVariable(band));
            result.add(c);
        }
        return result;
    }

    Set<PartialMIP> generatePiecewiseLinearFunctionConstraints() {
        Set<PartialMIP> result = new HashSet<>();
        for (Band band : bidder.getWorld().getBands()) {
            // Add C.14
            ContinuousPiecewiseLinearFunction funcAlpha = alpha(band);
            Variable inputAlpha = worldPartialMip.getXVariable(bidder, band);
            Variable outputAlpha = getAlphaVariable(band);
            PiecewiseLinearPartialMIP partialMipAlpha =
                    new PiecewiseLinearPartialMIP(funcAlpha,
                            inputAlpha,
                            outputAlpha,
                            "aux_alpha_helper_" + createIndex(bidder, band) + "_");
            result.add(partialMipAlpha);

            // Add C.15
            ContinuousPiecewiseLinearFunction funcBeta = beta(band);
            Variable inputBeta = worldPartialMip.getXVariable(bidder, band);
            Variable outputBeta = getBetaVariable(band);
            PiecewiseLinearPartialMIP partialMipBeta =
                    new PiecewiseLinearPartialMIP(funcBeta,
                            inputBeta,
                            outputBeta,
                            "aux_beta_helper_" + createIndex(bidder, band) + "_");
            result.add(partialMipBeta);

            // Add C.16
            ContinuousPiecewiseLinearFunction funcGamma = gamma(band);
            Variable inputGamma = worldPartialMip.getXVariable(bidder, band);
            Variable outputGamma = getGammaVariable(band);
            PiecewiseLinearPartialMIP partialMipGamma =
                    new PiecewiseLinearPartialMIP(funcGamma,
                            inputGamma,
                            outputGamma,
                            "aux_gamma_helper_" + createIndex(bidder, band) + "_");
            result.add(partialMipGamma);
        }
        return result;
    }

    private ContinuousPiecewiseLinearFunction alpha(Band band) {
        int threshold = bidder.getSynergyThreshold().get(band.getName());
        // Must ensure all BigDecimals have the same scale, as they are used as keys in a Map
        final int scale = 0;
        Map<BigDecimal, BigDecimal> breakpoints = new HashMap<>();
        // First breakpoint
        breakpoints.put(BigDecimal.ZERO.setScale(scale), BigDecimal.ZERO);
        // Middle breakpoint
        breakpoints.put(new BigDecimal(threshold).setScale(scale), new BigDecimal(threshold));
        // Last breakpoint
        BigDecimal key = new BigDecimal(band.getNumberOfLicenses()).setScale(scale);
        breakpoints.put(key, new BigDecimal(threshold));

        ContinuousPiecewiseLinearFunction result = new ContinuousPiecewiseLinearFunction(breakpoints);
        return result;
    }

    private ContinuousPiecewiseLinearFunction beta(Band band) {
        int threshold = bidder.getSynergyThreshold().get(band.getName());
        // Must ensure all BigDecimals have the same scale, as they are used as keys in a Map
        final int scale = 0;
        Map<BigDecimal, BigDecimal> breakpoints = new HashMap<>();
        // Add breakpoints
        for (int x = 0; x <= band.getNumberOfLicenses(); x++) {
            double value = 0;
            if (x > 0) value = Math.min((threshold - 1.0) / threshold, (x - 1.0) / x);
            breakpoints.put(new BigDecimal(x).setScale(scale), new BigDecimal(value));
        }

        ContinuousPiecewiseLinearFunction result = new ContinuousPiecewiseLinearFunction(breakpoints);
        return result;
    }

    private ContinuousPiecewiseLinearFunction gamma(Band band) {
        int threshold = bidder.getSynergyThreshold().get(band.getName());
        // Must ensure all BigDecimals have the same scale, as they are used as keys in a Map
        final int scale = 0;
        Map<BigDecimal, BigDecimal> breakpoints = new HashMap<>();
        // Add breakpoints
        for (int x = 0; x <= band.getNumberOfLicenses(); x++) {
            double log = 0;
            if (x - threshold >= 0) log = Math.log(x - threshold + 1);
            double value = Math.max(0, log);
            breakpoints.put(new BigDecimal(x).setScale(scale), new BigDecimal(value));
        }
        ContinuousPiecewiseLinearFunction result = new ContinuousPiecewiseLinearFunction(breakpoints);
        return result;
    }


    private double getBaseValue(SRMBidder bidder, Band band) {
        Preconditions.checkArgument(bidder.getBaseValues().containsKey(band.getName()));
        BigDecimal value = bidder.getBaseValues().get(band.getName());
        return value.floatValue();
    }

    private double getIntrabandSynergyFactor(SRMBidder bidder, Band band) {
        Preconditions.checkArgument(bidder.getIntrabandSynergyFactors().containsKey(band.getName()));
        BigDecimal value = bidder.getIntrabandSynergyFactors().get(band.getName());
        return value.floatValue();
    }


    public void appendVariablesToMip(MIP mip) {
        super.appendVariablesToMip(mip);
        for (Variable var : zVariables.values()) {
            mip.add(var);
        }
        mip.add(wVariable);
        for (Variable var : valueVariables.values()) {
            mip.add(var);
        }
        for (Variable var : alphaVariables.values()) {
            mip.add(var);
        }
        for (Variable var : betaVariables.values()) {
            mip.add(var);
        }
        for (Variable var : gammaVariables.values()) {
            mip.add(var);
        }
        for (PartialMIP partialMIP : generatePiecewiseLinearFunctionConstraints()) {
            partialMIP.appendVariablesToMip(mip);
        }
    }

    public void appendConstraintsToMip(MIP mip) {
        super.appendConstraintsToMip(mip);
        for (Constraint constraint : generatewConstraints()) {
            mip.add(constraint);
        }
        for (Constraint constraint : generatezConstraints()) {
            mip.add(constraint);
        }
        for (Constraint constraint : generateInterBandSynergyConstraints()) {
            mip.add(constraint);
        }
        for (Constraint constraint : generateValueConstraints()) {
            mip.add(constraint);
        }
        for (PartialMIP partialMip : generatePiecewiseLinearFunctionConstraints()) {
            partialMip.appendConstraintsToMip(mip);
        }
    }

}

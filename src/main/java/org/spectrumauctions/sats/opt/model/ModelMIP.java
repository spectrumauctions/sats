package org.spectrumauctions.sats.opt.model;

import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.ISolution;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.marketdesignresearch.mechlib.core.Allocation;
import org.marketdesignresearch.mechlib.core.bidder.Bidder;
import org.marketdesignresearch.mechlib.instrumentation.MipInstrumentation;
import org.marketdesignresearch.mechlib.winnerdetermination.WinnerDetermination;

import java.util.Collection;

/**
 * @author Fabio Isler
 */
public abstract class ModelMIP extends WinnerDetermination {

    protected ModelMIP() {
        super(MipInstrumentation.MipPurpose.ALLOCATION, new MipInstrumentation());
    }

    protected ModelMIP(MipInstrumentation.MipPurpose purpose) {
        super(purpose, new MipInstrumentation());
    }

    protected ModelMIP(MipInstrumentation.MipPurpose purpose, MipInstrumentation mipInstrumentation) {
        super(purpose, mipInstrumentation);
    }

    @Override
    protected abstract Allocation adaptMIPResult(ISolution mipResult); // Subclasses must implement this

    @Deprecated
    public Allocation calculateAllocation() {
        return getAllocation();
    }

    public abstract ModelMIP getMIPWithout(Bidder bidder);
    public abstract ModelMIP copyOf();

    private IMIP mip = new MIP();

    @Override
    public IMIP getMIP() {
        return mip;
    }

    /**
     * ModelMIPs have to explicitly set variables of interest
     */
    @Override
    protected abstract Collection<Collection<Variable>> getVariablesOfInterest();


}

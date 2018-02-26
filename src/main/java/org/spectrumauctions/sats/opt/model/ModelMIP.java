package org.spectrumauctions.sats.opt.model;

import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.mip.MIP;

/**
 * @author Fabio Isler
 */
public abstract class ModelMIP {

    private MIP mip = new MIP();

    /**
     * This is mainly used for testing or if you need to access special SolveParams.
     * In most cases the setters from this class are sufficient.
     *
     * @return reference to the JOpt mip
     */
    public MIP getMip() {
        return mip;
    }

    /**
     * Defines whether or not the solver output should be displayed.
     * Default is false.
     *
     * @param displayOutput If the output of the solver should be displayed, set this to true.
     */
    public void setDisplayOutput(boolean displayOutput) {
        mip.setSolveParam(SolveParam.DISPLAY_OUTPUT, displayOutput);
    }

    /**
     * Defines the behaviour in case the solver hits the defined timeout.
     *
     * @param acceptSuboptimal true: accept a suboptimal solution at timeout; false: throw an exception at timeout
     */
    public void setAcceptSuboptimal(boolean acceptSuboptimal) {
        mip.setSolveParam(SolveParam.ACCEPT_SUBOPTIMAL, acceptSuboptimal);
    }

    /**
     * Defines the time limit for the solver.
     * What happens after the time limit is defined via {@link #setAcceptSuboptimal(boolean)}.
     *
     * @param timeLimit the time limit in seconds
     */
    public void setTimeLimit(double timeLimit) {
        mip.setSolveParam(SolveParam.TIME_LIMIT, timeLimit);
    }

}

package org.spectrumauctions.sats.opt.domain;

import edu.harvard.econcs.jopt.solver.SolveParam;
import org.spectrumauctions.sats.opt.model.ModelMIP;

public interface DemandQueryMIP {
    ModelMIP getMip();
    default void setTimeLimit(double timeLimit) {
        getMip().getMip().setSolveParam(SolveParam.TIME_LIMIT, timeLimit);
    }

    default void setRelativeResultPoolTolerance(double resultPoolTolerance) {
        getMip().getMip().setSolveParam(SolveParam.SOLUTION_POOL_MODE_4_RELATIVE_GAP_TOLERANCE, resultPoolTolerance);
    }

    default void setAbsoluteResultPoolTolerance(double resultPoolTolerance) {
        getMip().getMip().setSolveParam(SolveParam.SOLUTION_POOL_MODE_4_ABSOLUTE_GAP_TOLERANCE, resultPoolTolerance);
    }
}

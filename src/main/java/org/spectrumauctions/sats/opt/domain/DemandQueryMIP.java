package org.spectrumauctions.sats.opt.domain;

import org.spectrumauctions.sats.core.model.Good;

public interface DemandQueryMIP<T extends Good> {
    DemandQueryResult<T> getResult();
}

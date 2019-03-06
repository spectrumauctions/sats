package org.spectrumauctions.sats.opt.domain;

import org.spectrumauctions.sats.core.model.Good;

import java.util.List;

public interface NonGenericDemandQueryMIP<S extends Good> extends DemandQueryMIP {
    NonGenericDemandQueryResult<S> getResult();
    List<? extends NonGenericDemandQueryResult<S>> getResultPool(int numberOfResults);
}

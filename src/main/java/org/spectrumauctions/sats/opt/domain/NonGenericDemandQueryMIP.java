package org.spectrumauctions.sats.opt.domain;

import org.spectrumauctions.sats.core.model.Good;

import java.util.Set;

public interface NonGenericDemandQueryMIP<S extends Good> {
    NonGenericDemandQueryResult<S> getResult();
    Set<? extends NonGenericDemandQueryResult<S>> getResultPool(int numberOfResults);
}

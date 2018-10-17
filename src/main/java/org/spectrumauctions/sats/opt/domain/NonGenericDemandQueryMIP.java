package org.spectrumauctions.sats.opt.domain;

import org.spectrumauctions.sats.core.model.Good;

import java.util.List;
import java.util.Set;

public interface NonGenericDemandQueryMIP<S extends Good> {
    NonGenericDemandQueryResult<S> getResult();
    List<? extends NonGenericDemandQueryResult<S>> getResultPool(int numberOfResults);
}

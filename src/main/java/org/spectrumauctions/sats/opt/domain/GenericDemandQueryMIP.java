package org.spectrumauctions.sats.opt.domain;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.Good;

import java.util.List;
import java.util.Set;

public interface GenericDemandQueryMIP<T extends GenericDefinition<S>, S extends Good> {
    GenericDemandQueryResult<T, S> getResult();
    List<? extends GenericDemandQueryResult<T, S>> getResultPool(int numberOfResults);
}

package org.spectrumauctions.sats.opt.domain;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.Good;

import java.util.Set;

public interface DemandQueryMIP<T extends GenericDefinition<S>, S extends Good> {
    DemandQueryResult<T, S> getResult();
    Set<? extends DemandQueryResult<T, S>> getResultPool(int numberOfResults);
}

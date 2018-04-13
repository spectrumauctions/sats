package org.spectrumauctions.sats.opt.domain;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.Good;

public interface DemandQueryMIP<T extends GenericDefinition<S>, S extends Good> {
    DemandQueryResult<T, S> getResult();
}

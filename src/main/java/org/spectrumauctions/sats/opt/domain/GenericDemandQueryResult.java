package org.spectrumauctions.sats.opt.domain;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Good;

public interface GenericDemandQueryResult<S extends GenericDefinition<T>, T extends Good> {
    GenericValue<S, T> getResultingBundle();
}

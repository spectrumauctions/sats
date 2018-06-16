package org.spectrumauctions.sats.opt.domain;

import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Good;

public interface NonGenericDemandQueryResult<T extends Good> {
    XORValue<T> getResultingBundle();
}

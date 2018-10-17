package org.spectrumauctions.sats.opt.domain;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;

import java.math.BigDecimal;
import java.util.Map;

public interface GenericDemandQueryMIPBuilder<S extends GenericDefinition<T>, T extends Good> {
    GenericDemandQueryMIP<S, T> getDemandQueryMipFor(Bidder<T> bidder, Map<S, BigDecimal> prices, double epsilon);
}

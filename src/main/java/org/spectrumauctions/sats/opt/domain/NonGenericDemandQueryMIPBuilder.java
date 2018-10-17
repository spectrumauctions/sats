package org.spectrumauctions.sats.opt.domain;

import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;

import java.math.BigDecimal;
import java.util.Map;

public interface NonGenericDemandQueryMIPBuilder<T extends Good> {
    NonGenericDemandQueryMIP<T> getDemandQueryMipFor(Bidder<T> bidder, Map<T, BigDecimal> prices, double epsilon);
}

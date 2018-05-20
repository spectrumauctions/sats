package org.spectrumauctions.sats.mechanism.cca.priceupdate;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.Good;

import java.math.BigDecimal;
import java.util.Map;

public interface PriceUpdater<T extends Good> {
    Map<GenericDefinition<T>, BigDecimal> updatePrices(Map<GenericDefinition<T>, BigDecimal> oldPrices, Map<GenericDefinition<T>, Integer> demand);
}

package org.spectrumauctions.sats.mechanism.cca.priceupdate;

import org.spectrumauctions.sats.core.model.Good;

import java.math.BigDecimal;
import java.util.Map;

public interface NonGenericPriceUpdater<T extends Good> {
    Map<T, BigDecimal> updatePrices(Map<T, BigDecimal> oldPrices, Map<T, Integer> demand);
}

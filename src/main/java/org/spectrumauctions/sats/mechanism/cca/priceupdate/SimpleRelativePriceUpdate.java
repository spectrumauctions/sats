package org.spectrumauctions.sats.mechanism.cca.priceupdate;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.Good;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SimpleRelativePriceUpdate<T extends Good> implements PriceUpdater<T> {

    private static final BigDecimal DEFAULT_PRICE_UPDATE = BigDecimal.valueOf(0.1);

    private BigDecimal priceUpdate = DEFAULT_PRICE_UPDATE;

    @Override
    public Map<GenericDefinition<T>, BigDecimal> updatePrices(Map<GenericDefinition<T>, BigDecimal> oldPrices, Map<GenericDefinition<T>, Integer> demand) {
        Map<GenericDefinition<T>, BigDecimal> newPrices = new HashMap<>();

        for (Map.Entry<GenericDefinition<T>, BigDecimal> oldPriceEntry : oldPrices.entrySet()) {
            GenericDefinition<T> def = oldPriceEntry.getKey();
            if (def.numberOfLicenses() < demand.getOrDefault(def, 0)) {
                if (oldPriceEntry.getValue().equals(BigDecimal.ZERO))
                    newPrices.put(def, BigDecimal.valueOf(1e5));
                else
                    newPrices.put(def, oldPriceEntry.getValue().add(oldPriceEntry.getValue().multiply(priceUpdate)));
            } else {
                newPrices.put(def, oldPriceEntry.getValue());
            }

        }

        return newPrices;
    }

    public void setPriceUpdate(BigDecimal priceUpdate) {
        this.priceUpdate = priceUpdate;
    }

    public SimpleRelativePriceUpdate<T> withPriceUpdate(BigDecimal priceUpdate) {
        setPriceUpdate(priceUpdate);
        return this;
    }
}

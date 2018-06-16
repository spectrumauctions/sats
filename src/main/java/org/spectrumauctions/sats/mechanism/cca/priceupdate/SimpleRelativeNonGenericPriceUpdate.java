package org.spectrumauctions.sats.mechanism.cca.priceupdate;

import org.spectrumauctions.sats.core.model.Good;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SimpleRelativeNonGenericPriceUpdate<T extends Good> implements NonGenericPriceUpdater<T> {

    private static final BigDecimal DEFAULT_PRICE_UPDATE = BigDecimal.valueOf(0.1);
    private static final BigDecimal DEFAULT_INITIAL_UPDATE = BigDecimal.valueOf(1e5);

    private BigDecimal priceUpdate = DEFAULT_PRICE_UPDATE;
    private BigDecimal initialUpdate = DEFAULT_INITIAL_UPDATE;

    @Override
    public Map<T, BigDecimal> updatePrices(Map<T, BigDecimal> oldPrices, Map<T, Integer> demand) {
        Map<T, BigDecimal> newPrices = new HashMap<>();

        for (Map.Entry<T, BigDecimal> oldPriceEntry : oldPrices.entrySet()) {
            T good = oldPriceEntry.getKey();
            if (demand.getOrDefault(good, 0) > 1) {
                if (oldPriceEntry.getValue().equals(BigDecimal.ZERO))
                    newPrices.put(good, initialUpdate);
                else
                    newPrices.put(good, oldPriceEntry.getValue().add(oldPriceEntry.getValue().multiply(priceUpdate)));
            } else {
                newPrices.put(good, oldPriceEntry.getValue());
            }

        }

        return newPrices;
    }

    public void setPriceUpdate(BigDecimal priceUpdate) {
        this.priceUpdate = priceUpdate;
    }

    public void setInitialUpdate(BigDecimal initialUpdate) {
        this.initialUpdate = initialUpdate;
    }

    public SimpleRelativeNonGenericPriceUpdate<T> withPriceUpdate(BigDecimal priceUpdate) {
        setPriceUpdate(priceUpdate);
        return this;
    }

    public SimpleRelativeNonGenericPriceUpdate<T> withInitialUpdate(BigDecimal initialUpdate) {
        setInitialUpdate(initialUpdate);
        return this;
    }
}

package org.spectrumauctions.sats.mechanism.cca.priceupdate;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.Good;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SimpleRelativeGenericPriceUpdate<G extends GenericDefinition<T>, T extends Good> implements GenericPriceUpdater<G, T> {

    private static final BigDecimal DEFAULT_PRICE_UPDATE = BigDecimal.valueOf(0.1);
    private static final BigDecimal DEFAULT_INITIAL_UPDATE = BigDecimal.valueOf(1e5);

    private BigDecimal priceUpdate = DEFAULT_PRICE_UPDATE;
    private BigDecimal initialUpdate = DEFAULT_INITIAL_UPDATE;

    @Override
    public Map<G, BigDecimal> updatePrices(Map<G, BigDecimal> oldPrices, Map<G, Integer> demand) {
        Map<G, BigDecimal> newPrices = new HashMap<>();

        for (Map.Entry<G, BigDecimal> oldPriceEntry : oldPrices.entrySet()) {
            G def = oldPriceEntry.getKey();
            if (def.numberOfLicenses() < demand.getOrDefault(def, 0)) {
                if (oldPriceEntry.getValue().equals(BigDecimal.ZERO))
                    newPrices.put(def, initialUpdate);
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

    public void setInitialUpdate(BigDecimal initialUpdate) {
        this.initialUpdate = initialUpdate;
    }

    public SimpleRelativeGenericPriceUpdate<G, T> withPriceUpdate(BigDecimal priceUpdate) {
        setPriceUpdate(priceUpdate);
        return this;
    }

    public SimpleRelativeGenericPriceUpdate<G, T> withInitialUpdate(BigDecimal initialUpdate) {
        setInitialUpdate(initialUpdate);
        return this;
    }
}

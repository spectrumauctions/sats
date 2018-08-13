package org.spectrumauctions.sats.mechanism.cca.priceupdate;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.Good;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class DemandDependentGenericPriceUpdate<G extends GenericDefinition<T>, T extends Good> implements GenericPriceUpdater<G, T> {

    private static final BigDecimal DEFAULT_CONSTANT = BigDecimal.valueOf(1e6);

    private BigDecimal constant = DEFAULT_CONSTANT;
    private int round = 1;

    private Map<G, BigDecimal> lastPrices = new HashMap<>();

    @Override
    public Map<G, BigDecimal> updatePrices(Map<G, BigDecimal> oldPrices, Map<G, Integer> demand) {
        // Fill the last prices map with initial values
        if (lastPrices.isEmpty()) {
            for (Map.Entry<G, BigDecimal> oldPriceEntry : oldPrices.entrySet()) {
                lastPrices.put(oldPriceEntry.getKey(), oldPriceEntry.getValue());
            }
        }

        Map<G, BigDecimal> newPrices = new HashMap<>();
        for (Map.Entry<G, BigDecimal> oldPriceEntry : oldPrices.entrySet()) {
            G def = oldPriceEntry.getKey();
            BigDecimal diff = BigDecimal.valueOf(demand.getOrDefault(def, 0) - def.numberOfLicenses());
            BigDecimal factor = constant.divide(BigDecimal.valueOf(Math.sqrt(round)), RoundingMode.HALF_UP);
            BigDecimal newPrice = oldPriceEntry.getValue().add(factor.multiply(diff));
            newPrices.put(def, newPrice);

            // Overdemanded
            if (newPrice.compareTo(oldPriceEntry.getValue()) > 0) {
                lastPrices.put(def, oldPriceEntry.getValue());
            }
        }

        round++;
        return newPrices;
    }

    @Override
    public Map<G, BigDecimal> getLastPrices() {
        return lastPrices;
    }

    public void setConstant(BigDecimal priceUpdate) {
        this.constant = priceUpdate;
    }

}

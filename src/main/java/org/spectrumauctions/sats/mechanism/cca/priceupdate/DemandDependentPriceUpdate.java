package org.spectrumauctions.sats.mechanism.cca.priceupdate;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.Good;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class DemandDependentPriceUpdate<T extends Good> implements PriceUpdater<T> {

    private static final BigDecimal DEFAULT_CONSTANT = BigDecimal.valueOf(1e6);

    private BigDecimal constant = DEFAULT_CONSTANT;
    private int round = 1;

    @Override
    public Map<GenericDefinition<T>, BigDecimal> updatePrices(Map<GenericDefinition<T>, BigDecimal> oldPrices, Map<GenericDefinition<T>, Integer> demand) {
        Map<GenericDefinition<T>, BigDecimal> newPrices = new HashMap<>();

        for (Map.Entry<GenericDefinition<T>, BigDecimal> oldPriceEntry : oldPrices.entrySet()) {
            GenericDefinition<T> def = oldPriceEntry.getKey();
            BigDecimal diff = BigDecimal.valueOf(demand.getOrDefault(def, 0) - def.numberOfLicenses());
            BigDecimal factor = constant.divide(BigDecimal.valueOf(Math.sqrt(round)), RoundingMode.HALF_UP);
            BigDecimal newPrice = oldPriceEntry.getValue().add(factor.multiply(diff));
            newPrices.put(def, newPrice);
        }

        round++;
        return newPrices;
    }

    public void setConstant(BigDecimal priceUpdate) {
        this.constant = priceUpdate;
    }

}

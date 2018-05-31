package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.opt.domain.DemandQueryMIP;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public class ProfitMaximizingSupplementaryRound<T extends Good> implements SupplementaryRound<T> {

    private static final int DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS = 500;

    private int numberOfSupplementaryBids = DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS;

    @Override
    public Set<GenericValue<?, T>> getSupplementaryBids(Bidder<T> bidder, Map<GenericDefinition<T>, BigDecimal> prices, DemandQueryMIP<GenericDefinition<T>, T> demandQueryMIP) {
        return null;
    }
}

package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.opt.domain.DemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.DemandQueryResult;

import java.util.Set;
import java.util.stream.Collectors;

public class ProfitMaximizingSupplementaryRound<T extends Good> implements SupplementaryRound<T> {

    private static final int DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS = 500;

    private int numberOfSupplementaryBids = DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS;

    @Override
    public Set<GenericValue<? extends GenericDefinition<T>, T>> getSupplementaryBids(Bidder<T> bidder, DemandQueryMIP<GenericDefinition<T>, T> demandQueryMIP) {
        Set<? extends DemandQueryResult<GenericDefinition<T>, T>> resultSet = demandQueryMIP.getResultPool(numberOfSupplementaryBids);
        return resultSet.stream().map(DemandQueryResult::getResultingBundle).collect(Collectors.toSet());
    }

    public void setNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
    }

    public ProfitMaximizingSupplementaryRound<T> withNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
        return this;
    }
}

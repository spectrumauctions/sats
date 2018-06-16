package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryResult;

import java.util.Set;
import java.util.stream.Collectors;

public class ProfitMaximizingGenericSupplementaryRound<G extends GenericDefinition<T>, T extends Good> implements GenericSupplementaryRound<G, T> {

    private static final int DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS = 500;

    private int numberOfSupplementaryBids = DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS;

    @Override
    public Set<GenericValue<G, T>> getSupplementaryBids(Bidder<T> bidder, GenericDemandQueryMIP<G, T> genericDemandQueryMIP) {
        Set<? extends GenericDemandQueryResult<G, T>> resultSet = genericDemandQueryMIP.getResultPool(numberOfSupplementaryBids);
        return resultSet.stream().map(GenericDemandQueryResult::getResultingBundle).collect(Collectors.toSet());
    }

    public void setNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
    }

    public ProfitMaximizingGenericSupplementaryRound<G, T> withNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
        return this;
    }
}

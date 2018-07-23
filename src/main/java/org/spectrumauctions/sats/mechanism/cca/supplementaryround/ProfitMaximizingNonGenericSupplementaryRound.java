package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfitMaximizingNonGenericSupplementaryRound<T extends Good> implements NonGenericSupplementaryRound<T> {

    private static final int DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS = 500;

    private int numberOfSupplementaryBids = DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS;

    @Override
    public Set<XORValue<T>> getSupplementaryBids(Bidder<T> bidder, NonGenericDemandQueryMIP<T> genericDemandQueryMIP) {
        List<? extends NonGenericDemandQueryResult<T>> resultSet = genericDemandQueryMIP.getResultPool(numberOfSupplementaryBids);
        return resultSet.stream().map(NonGenericDemandQueryResult::getResultingBundle).collect(Collectors.toSet());
    }

    public void setNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
    }

    public ProfitMaximizingNonGenericSupplementaryRound<T> withNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
        return this;
    }
}

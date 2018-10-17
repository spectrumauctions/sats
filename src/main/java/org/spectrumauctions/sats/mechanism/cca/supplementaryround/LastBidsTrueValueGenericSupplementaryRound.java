package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.bidlang.generic.GenericBid;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.cca.GenericCCAMechanism;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryResult;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class LastBidsTrueValueGenericSupplementaryRound<G extends GenericDefinition<T>, T extends Good> implements GenericSupplementaryRound<G, T> {

    private static final int DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS = 500;

    private int numberOfSupplementaryBids = DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS;

    @Override
    public List<GenericValue<G, T>> getSupplementaryBids(GenericCCAMechanism<G, T> cca, Bidder<T> bidder) {
        Preconditions.checkArgument(bidder instanceof GenericValueBidder);
        GenericValueBidder<G> genericBidder = (GenericValueBidder<G>) bidder;
        GenericBid<G, T> bid = cca.getBidAfterClockPhase(bidder);
        List<GenericValue<G, T>> result = new ArrayList<>();
        int count = 0;
        for (int i = bid.getValues().size() - 1; i >= 0 && count++ < numberOfSupplementaryBids; i--) {
            GenericValue<G, T> value = bid.getValues().get(i);
            GenericValue.Builder<G, T> builder = new GenericValue.Builder<>(genericBidder);
            for (Map.Entry<G, Integer> entry : value.getQuantities().entrySet()) {
                builder.putQuantity(entry.getKey(), entry.getValue());
            }
            result.add(builder.build());
        }
        return result;
    }

    public void setNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
    }

    public LastBidsTrueValueGenericSupplementaryRound<G, T> withNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
        return this;
    }
}

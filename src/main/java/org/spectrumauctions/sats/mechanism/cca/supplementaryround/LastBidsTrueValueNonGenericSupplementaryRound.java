package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.cca.NonGenericCCAMechanism;

import java.util.ArrayList;
import java.util.List;

public class LastBidsTrueValueNonGenericSupplementaryRound<T extends Good> implements NonGenericSupplementaryRound<T> {

    private static final int DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS = 500;

    private int numberOfSupplementaryBids = DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS;

    @Override
    public List<XORValue<T>> getSupplementaryBids(NonGenericCCAMechanism<T> cca, Bidder<T> bidder) {
        XORBid<T> bid = cca.getBidAfterClockPhase(bidder);
        List<XORValue<T>> result = new ArrayList<>();
        int count = 0;
        for (int i = bid.getValues().size() - 1; i >= 0 && count++ < numberOfSupplementaryBids; i--) {
            XORValue<T> value = bid.getValues().get(i);
            Bundle<T> licenses = new Bundle<>(value.getLicenses());
            result.add(new XORValue<>(licenses, bidder.calculateValue(licenses)));
        }
        return result;
    }

    public void setNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
    }

    public LastBidsTrueValueNonGenericSupplementaryRound<T> withNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
        return this;
    }
}

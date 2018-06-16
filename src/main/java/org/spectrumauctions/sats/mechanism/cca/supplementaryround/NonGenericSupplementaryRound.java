package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIP;

import java.util.Set;

public interface NonGenericSupplementaryRound<T extends Good> {
    Set<XORValue<T>> getSupplementaryBids(Bidder<T> bidder, NonGenericDemandQueryMIP<T> genericDemandQueryMIP);
}

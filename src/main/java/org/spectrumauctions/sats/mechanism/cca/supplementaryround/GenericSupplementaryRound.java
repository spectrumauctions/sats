package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIP;

import java.util.Set;

public interface GenericSupplementaryRound<G extends GenericDefinition<T>, T extends Good> {
    Set<GenericValue<G, T>> getSupplementaryBids(Bidder<T> bidder, GenericDemandQueryMIP<G, T> genericDemandQueryMIP);
}

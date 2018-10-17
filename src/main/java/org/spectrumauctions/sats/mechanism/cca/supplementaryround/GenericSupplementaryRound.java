package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.cca.GenericCCAMechanism;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIP;

import java.util.List;
import java.util.Set;

public interface GenericSupplementaryRound<G extends GenericDefinition<T>, T extends Good> {
    List<GenericValue<G, T>> getSupplementaryBids(GenericCCAMechanism<G, T> cca, Bidder<T> bidder);
}

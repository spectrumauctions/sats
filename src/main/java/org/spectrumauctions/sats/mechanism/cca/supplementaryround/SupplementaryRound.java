package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.opt.domain.DemandQueryMIP;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public interface SupplementaryRound<T extends Good> {
    Set<GenericValue<? extends GenericDefinition<T>, T>> getSupplementaryBids(Bidder<T> bidder, DemandQueryMIP<GenericDefinition<T>, T> demandQueryMIP);
}

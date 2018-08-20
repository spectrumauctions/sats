package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.cca.NonGenericCCAMechanism;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIP;

import java.util.List;

public interface NonGenericSupplementaryRound<T extends Good> {
    List<XORValue<T>> getSupplementaryBids(NonGenericCCAMechanism<T> cca, Bidder<T> bidder);
}

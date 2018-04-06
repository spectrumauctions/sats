package org.spectrumauctions.sats.mechanism.domain.mechanisms;

import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;

public interface AuctionMechanism<T extends Allocation<S>, S extends Good> extends PaymentDeterminator<S>, WinnerDeterminator<T, S> {
    MechanismResult<S> getMechanismResult();
}

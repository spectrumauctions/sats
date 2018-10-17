package org.spectrumauctions.sats.mechanism.domain.mechanisms;

import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;

public interface AuctionMechanism<T extends Good> extends PaymentDeterminator<T>, WinnerDeterminator<T> {
    MechanismResult<T> getMechanismResult();
}

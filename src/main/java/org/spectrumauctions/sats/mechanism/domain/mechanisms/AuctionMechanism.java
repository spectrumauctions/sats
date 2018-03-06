package org.spectrumauctions.sats.mechanism.domain.mechanisms;

import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;

public interface AuctionMechanism extends PaymentDeterminator, WinnerDeterminator {
    MechanismResult getMechanismResult();
}

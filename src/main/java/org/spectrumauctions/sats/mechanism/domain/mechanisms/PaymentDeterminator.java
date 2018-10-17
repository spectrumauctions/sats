package org.spectrumauctions.sats.mechanism.domain.mechanisms;

import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.domain.Payment;

public interface PaymentDeterminator<T extends Good> {

    Payment<T> getPayment();

}
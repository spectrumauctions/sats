package org.spectrumauctions.sats.mechanism.domain.mechanisms;

import org.spectrumauctions.sats.mechanism.domain.Payment;

public interface PaymentDeterminator {

    Payment getPayment();

}
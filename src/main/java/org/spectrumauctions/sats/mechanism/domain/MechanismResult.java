package org.spectrumauctions.sats.mechanism.domain;

import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.opt.domain.Allocation;

/**
 * This class represents the result of an Auction, consisting of
 * an Allocation and a Payment vector.
 */
public class MechanismResult<T extends Good> {
    private final Payment<T> payment;
    private final Allocation<T> allocation;

    public MechanismResult(Payment<T> payment, Allocation<T> allocation) {
        this.payment = payment;
        this.allocation = allocation;
    }

    public Payment<T> getPayment() {
        return payment;
    }

    public Allocation<T> getAllocation() {
        return allocation;
    }

}

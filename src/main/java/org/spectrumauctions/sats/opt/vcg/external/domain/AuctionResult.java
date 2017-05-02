package org.spectrumauctions.sats.opt.vcg.external.domain;

import org.spectrumauctions.sats.core.model.Good;

/**
 * This class represents the result of an Auction, consisting of
 * an Allocation and a Payment vector.
 */
public class AuctionResult<T extends Good> {
    private final Payment<T> payment;
    private final XORAllocation<T> allocation;

    public AuctionResult(Payment<T> payment, XORAllocation<T> allocation) {
        this.payment = payment;
        this.allocation = allocation;
    }

    public Payment<T> getPayment() {
        return payment;
    }

    public XORAllocation<T> getAllocation() {
        return allocation;
    }

}

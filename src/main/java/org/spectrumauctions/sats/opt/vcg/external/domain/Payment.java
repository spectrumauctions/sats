package org.spectrumauctions.sats.opt.vcg.external.domain;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the Payment vector after a WinnerDetermination.
 */
public final class Payment<T extends Good> {
    private final Map<Bidder<T>, BidderPayment> payments;


    /**
     * @param payments     Map of bidder to payments. One payment per bidder. Payment may
     *                     be 0 and allocation may of payment may be empty
     */
    public Payment(Map<Bidder<T>, BidderPayment> payments) {
        this.payments = Collections.unmodifiableMap(payments);
    }

    public double getTotalPayments() {
        double totalPayments = 0;
        for (BidderPayment payment : getPayments()) {
            totalPayments += payment.getAmount();
        }
        return totalPayments;
    }

    public Collection<BidderPayment> getPayments() {
        return payments.values();
    }

    public Map<Bidder<T>, BidderPayment> getPaymentMap() {
        return payments;
    }

    public BidderPayment paymentOf(Bidder<T> bidder) {
        BidderPayment payment = payments.get(bidder);
        if (payment == null) {
            return new BidderPayment(0);
        }
        return payment;
    }

    @Override
    public String toString() {

        return "Payment[payments=" + payments + "]";
    }

    public Set<Bidder<T>> getWinners() {
        return payments.keySet();
    }

    public boolean isWinner(Bidder<T> bidder) {
        return payments.containsKey(bidder);
    }


    public static <T extends Good> Payment<T> getZeroPayment(Set<Bidder<T>> bidders) {
        BidderPayment zeroBidderPayment = new BidderPayment(0);
        Function<Object, BidderPayment> zeroPaymentFunction = Functions.constant(zeroBidderPayment);
        Map<Bidder<T>, BidderPayment> emptyPaymentMap = Maps.asMap(bidders, zeroPaymentFunction);
        return new Payment<T>(emptyPaymentMap);
    }

}

package org.spectrumauctions.sats.mechanism.vcg;

import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.mechanism.domain.BidderPayment;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;

import java.util.HashMap;
import java.util.Map;

public class VCGMechanism<T extends Good> implements AuctionMechanism {

    private WinnerDeterminator<Allocation<T>> baseWD;
    private MechanismResult<T> result;


    public VCGMechanism(WinnerDeterminator<Allocation<T>> wdp) {
        this.baseWD = wdp;
    }

    @Override
    public MechanismResult<T> getMechanismResult() {
        if (result == null) {
            result = calculateVCGPrices();
        }
        return result;
    }

    @Override
    public Payment<T> getPayment() {
        return getMechanismResult().getPayment();
    }

    @Override
    public WinnerDeterminator<Allocation<T>> getWdWithoutBidder(Bidder bidder) {
        return null;
    }

    @Override
    public Allocation<T> calculateAllocation() {
        return getMechanismResult().getAllocation();
    }

    protected MechanismResult<T> calculateVCGPrices() {
        Allocation<T> baseAllocation = baseWD.calculateAllocation();

        Map<Bidder<T>, BidderPayment> payments = new HashMap<>();
        for (Bidder<T> bidder : baseAllocation.getWinners()) {

            double valueWithoutBidder = baseAllocation.getTotalValue().doubleValue() - baseAllocation.getTradeValue(bidder).doubleValue();

            WinnerDeterminator<Allocation<T>> wdWithoutBidder = baseWD.getWdWithoutBidder(bidder);
            Allocation<T> allocationWithoutBidder = wdWithoutBidder.calculateAllocation();
            double valueWDWithoutBidder = allocationWithoutBidder.getTotalValue().doubleValue();

            double paymentAmount = valueWDWithoutBidder - valueWithoutBidder;
            payments.put(bidder, new BidderPayment(paymentAmount));

        }
        Payment<T> payment = new Payment<>(payments);
        return new MechanismResult<>(payment, baseAllocation);
    }

}

package org.spectrumauctions.sats.opt.vcg.external.vcg;

import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.opt.vcg.external.domain.*;
import org.spectrumauctions.sats.opt.vcg.external.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.vcg.external.winnerdetermination.WinnerDetermination;

import java.util.HashMap;
import java.util.Map;

public abstract class VCGAuction<T extends Good> implements AuctionMechanism {

    private AuctionResult<T> result;
    private Auction<T> auction;

    public VCGAuction(Auction<T> auction) {
        this.auction = auction;
    }

    @Override
    public AuctionResult<T> getAuctionResult() {
        if (result == null) {
            result = calculateVCGPrices(auction);
        }
        return result;
    }

    @Override
    public Payment<T> getPayment() {
        return getAuctionResult().getPayment();
    }

    @Override
    public XORAllocation<T> calculateAllocation() {
        return getAuctionResult().getAllocation();
    }

    protected AuctionResult<T> calculateVCGPrices(Auction<T> auction) {
        WinnerDetermination baseWD = getWinnerDetermination(auction);
        XORAllocation<T> baseAllocation = baseWD.calculateAllocation();

        Map<Bidder<T>, BidderPayment> payments = new HashMap<>();
        for (Bidder<T> bidder : baseAllocation.getWinners()) {

            double valueWithoutBidder = baseAllocation.getTotalAllocationValue() - baseAllocation.getAllocation(bidder).getTradeValue();
            Auction<T> auctionWithoutBidder = auction.without(bidder);
            WinnerDetermination wdWithoutBidder = getWinnerDetermination(auctionWithoutBidder);
            XORAllocation<T> allocationWithoutBidder = wdWithoutBidder.calculateAllocation();
            double valueWDWithoutBidder = allocationWithoutBidder.getTotalAllocationValue();

            double paymentAmount = valueWDWithoutBidder - valueWithoutBidder;
            payments.put(bidder, new BidderPayment(paymentAmount));

        }
        Payment<T> payment = new Payment<>(payments);
        return new AuctionResult<>(payment, baseAllocation);
    }

    /**
     * Returns a new {@link WinnerDetermination} using the WD defined in the subclass
     *
     * @param auction
     * @return
     */
    protected abstract WinnerDetermination getWinnerDetermination(Auction<T> auction);

}

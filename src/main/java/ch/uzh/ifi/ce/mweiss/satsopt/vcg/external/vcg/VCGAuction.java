package ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.vcg;

import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.Good;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.Auction;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.AuctionResult;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.BidderPayment;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.Payment;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.XORAllocation;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.mechanisms.AuctionMechanism;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.winnerdetermination.WinnerDetermination;

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
     * @param auction
     * @return
     */
    protected abstract WinnerDetermination getWinnerDetermination(Auction<T> auction);

}

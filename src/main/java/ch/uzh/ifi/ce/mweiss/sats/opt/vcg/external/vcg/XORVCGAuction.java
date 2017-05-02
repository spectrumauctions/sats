package ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.vcg;

import ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain.Auction;
import ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.winnerdetermination.XORWinnerDetermination;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Good;
import ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.winnerdetermination.WinnerDetermination;

public class XORVCGAuction<T extends Good> extends VCGAuction<T> {

    public XORVCGAuction(Auction<T> auction) {
        super(auction);
    }

    @Override
    protected WinnerDetermination getWinnerDetermination(Auction<T> auction) {
        return new XORWinnerDetermination(auction);
    }

}

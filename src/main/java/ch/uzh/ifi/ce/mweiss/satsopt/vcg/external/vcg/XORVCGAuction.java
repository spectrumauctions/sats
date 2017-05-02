package ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.vcg;

import ch.uzh.ifi.ce.mweiss.specval.model.Good;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.Auction;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.winnerdetermination.WinnerDetermination;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.winnerdetermination.XORWinnerDetermination;

public class XORVCGAuction<T extends Good> extends VCGAuction<T> {

    public XORVCGAuction(Auction<T> auction) {
        super(auction);
    }

    @Override
    protected WinnerDetermination getWinnerDetermination(Auction<T> auction) {
        return new XORWinnerDetermination(auction);
    }

}

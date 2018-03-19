package org.spectrumauctions.sats.opt.vcg.external.vcg;

import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.opt.vcg.external.domain.Auction;
import org.spectrumauctions.sats.opt.vcg.external.winnerdetermination.WinnerDetermination;
import org.spectrumauctions.sats.opt.vcg.external.winnerdetermination.XORWinnerDetermination;

public class XORVCGAuction<T extends Good> extends VCGAuction<T> {

    public XORVCGAuction(Auction<T> auction) {
        super(auction);
    }

    @Override
    protected WinnerDetermination getWinnerDetermination(Auction<T> auction) {
        return new XORWinnerDetermination(auction);
    }

}

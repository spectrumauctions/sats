package org.spectrumauctions.sats.mechanism.vcg;

import org.marketdesignresearch.mechlib.domain.bidder.Bidder;
import org.marketdesignresearch.mechlib.mechanisms.vcg.VCGMechanism;
import org.marketdesignresearch.mechlib.winnerdetermination.WinnerDetermination;
import org.spectrumauctions.sats.opt.model.ModelMIP;

public class ModelVCGMechanism extends VCGMechanism {

    private ModelMIP winnerDetermination;

    public ModelVCGMechanism(ModelMIP winnerDetermination)  {
        this.winnerDetermination = winnerDetermination;
    }

    @Override
    protected WinnerDetermination getWinnerDetermination() {
        return winnerDetermination;
    }

    @Override
    protected WinnerDetermination getWinnerDeterminationWithout(Bidder bidder) {
        return winnerDetermination.getMIPWithout(bidder);
    }
}

package org.spectrumauctions.sats.core.model.mrvm;

import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Weiss
 */
public class RandomOrderXORQFactory implements Serializable {

    private static final long serialVersionUID = -5801444096946669459L;

    public static XORQRandomOrderSimple getXORQRandomOrderSimpleLang(MRVMBidder bidder, RNGSupplier rngSupplier) throws UnsupportedBiddingLanguageException {
        List<MRVMGenericDefinition> bands = new ArrayList<>();
        for (MRVMBand band : bidder.getWorld().getBands()) {
            for (MRVMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                bands.add(new MRVMGenericDefinition(band, region));
            }
        }
        return new SimpleRandomOrder(bands, bidder, rngSupplier);
    }

    public static XORQRandomOrderSimple getXORQRandomOrderSimpleLang(MRVMBidder bidder) throws UnsupportedBiddingLanguageException {
        return getXORQRandomOrderSimpleLang(bidder, new JavaUtilRNGSupplier());
    }


    private static final class SimpleRandomOrder extends XORQRandomOrderSimple {


        private final MRVMBidder bidder;

        /**
         * @param allPossibleGenericDefinitions Collection of generic definitions
         * @param rngSupplier                   Random number generator supplier
         */
        SimpleRandomOrder(List<MRVMGenericDefinition> allPossibleGenericDefinitions, MRVMBidder bidder, RNGSupplier rngSupplier)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefinitions, rngSupplier);
            this.bidder = bidder;
        }

        @Override
        public SATSBidder getBidder() {
            return bidder;
        }

    }

}

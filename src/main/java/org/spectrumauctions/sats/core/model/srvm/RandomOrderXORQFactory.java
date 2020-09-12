package org.spectrumauctions.sats.core.model.srvm;

import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.io.Serializable;
import java.util.List;

/**
 * @author Fabio Isler
 */
class RandomOrderXORQFactory implements Serializable {

    private static final long serialVersionUID = -337996389686369882L;

    public static XORQRandomOrderSimple getXORQRandomOrderSimpleLang(SRVMBidder bidder, RNGSupplier rngSupplier) throws UnsupportedBiddingLanguageException {
        List<SRVMBand> bands = bidder.getWorld().getBands();
        return new SimpleRandomOrder(bands, bidder, rngSupplier);
    }

    public static XORQRandomOrderSimple getXORQRandomOrderSimpleLang(SRVMBidder bidder) throws UnsupportedBiddingLanguageException {
        List<SRVMBand> bands = bidder.getWorld().getBands();
        return new SimpleRandomOrder(bands, bidder, new JavaUtilRNGSupplier());
    }


    private static final class SimpleRandomOrder extends XORQRandomOrderSimple {


        private final SRVMBidder bidder;

        /**
         * @param allPossibleGenericDefinitions A collection of all available goods
         * @throws UnsupportedBiddingLanguageException Thrown if the model doesn't support the requested bidding language
         */
        SimpleRandomOrder(List<SRVMBand> allPossibleGenericDefinitions, SRVMBidder bidder, RNGSupplier rngSupplier)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefinitions, rngSupplier);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see BiddingLanguage#getBidder()
         */
        @Override
        public SATSBidder getBidder() {
            return bidder;
        }

    }

}

package org.spectrumauctions.sats.core.model.srvm;

import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 * @author Fabio Isler
 */
class RandomOrderXORQFactory {

    private static final BandComparator comparator = new BandComparator();

    public static XORQRandomOrderSimple<SRMBand> getXORQRandomOrderSimpleLang(SRMBidder bidder, RNGSupplier rngSupplier) throws UnsupportedBiddingLanguageException {
        Set<SRMBand> bands = bidder.getWorld().getBands();
        return new SimpleRandomOrder(bands, bidder, rngSupplier);
    }

    public static XORQRandomOrderSimple<SRMBand> getXORQRandomOrderSimpleLang(SRMBidder bidder) throws UnsupportedBiddingLanguageException {
        Set<SRMBand> bands = bidder.getWorld().getBands();
        return new SimpleRandomOrder(bands, bidder, new JavaUtilRNGSupplier());
    }


    private static final class SimpleRandomOrder extends XORQRandomOrderSimple<SRMBand> {


        private final SRMBidder bidder;

        /**
         * @param allPossibleGenericDefinitions A collection of all available goods
         * @throws UnsupportedBiddingLanguageException Thrown if the model doesn't support the requested bidding language
         */
        SimpleRandomOrder(Collection<SRMBand> allPossibleGenericDefinitions, SRMBidder bidder, RNGSupplier rngSupplier)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefinitions, rngSupplier);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see BiddingLanguage#getBidder()
         */
        @Override
        public Bidder<SRMLicense> getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see org.spectrumauctions.sats.core.bidlang.generic.SizeOrdered.GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<SRMBand> getGenericBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see org.spectrumauctions.sats.core.bidlang.generic.SizeOrdered.GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<SRMBand> getDefComparator() {
            return comparator;
        }
    }

    private static class BandComparator implements Comparator<SRMBand> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(SRMBand o1, SRMBand o2) {
            return o1.toString().compareTo(o2.toString());
        }

    }


}

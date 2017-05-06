package org.spectrumauctions.sats.core.model.srvm;

import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 * @author Fabio Isler
 */
class RandomOrderXORQFactory implements Serializable {

    private static final BandComparator comparator = new BandComparator();
    private static final long serialVersionUID = -337996389686369882L;

    public static XORQRandomOrderSimple<SRVMBand> getXORQRandomOrderSimpleLang(SRVMBidder bidder, RNGSupplier rngSupplier) throws UnsupportedBiddingLanguageException {
        Set<SRVMBand> bands = bidder.getWorld().getBands();
        return new SimpleRandomOrder(bands, bidder, rngSupplier);
    }

    public static XORQRandomOrderSimple<SRVMBand> getXORQRandomOrderSimpleLang(SRVMBidder bidder) throws UnsupportedBiddingLanguageException {
        Set<SRVMBand> bands = bidder.getWorld().getBands();
        return new SimpleRandomOrder(bands, bidder, new JavaUtilRNGSupplier());
    }


    private static final class SimpleRandomOrder extends XORQRandomOrderSimple<SRVMBand> {


        private final SRVMBidder bidder;

        /**
         * @param allPossibleGenericDefinitions A collection of all available goods
         * @throws UnsupportedBiddingLanguageException Thrown if the model doesn't support the requested bidding language
         */
        SimpleRandomOrder(Collection<SRVMBand> allPossibleGenericDefinitions, SRVMBidder bidder, RNGSupplier rngSupplier)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefinitions, rngSupplier);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see BiddingLanguage#getBidder()
         */
        @Override
        public Bidder<SRVMLicense> getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see org.spectrumauctions.sats.core.bidlang.generic.SizeOrdered.GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<SRVMBand> getGenericBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see org.spectrumauctions.sats.core.bidlang.generic.SizeOrdered.GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<SRVMBand> getDefComparator() {
            return comparator;
        }
    }

    private static class BandComparator implements Comparator<SRVMBand>, Serializable {

        private static final long serialVersionUID = -4955747961470283517L;

        /* (non-Javadoc)
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
        @Override
        public int compare(SRVMBand o1, SRVMBand o2) {
            return o1.toString().compareTo(o2.toString());
        }

    }


}

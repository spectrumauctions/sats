package org.spectrumauctions.sats.core.model.mrvm;

import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeOrdered;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Michael Weiss
 */
public class RandomOrderXORQFactory implements Serializable {

    private static final long serialVersionUID = -5801444096946669459L;
    @SuppressWarnings("CanBeFinal")
    private static BandComparator comparator = new BandComparator();

    public static XORQRandomOrderSimple<MRVMGenericDefinition> getXORQRandomOrderSimpleLang(MRVMBidder bidder, RNGSupplier rngSupplier) throws UnsupportedBiddingLanguageException {
        List<MRVMGenericDefinition> bands = new ArrayList<>();
        for (MRVMBand band : bidder.getWorld().getBands()) {
            for (MRVMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                bands.add(new MRVMGenericDefinition(band, region));
            }
        }
        return new SimpleRandomOrder(bands, bidder, rngSupplier);
    }

    public static XORQRandomOrderSimple<MRVMGenericDefinition> getXORQRandomOrderSimpleLang(MRVMBidder bidder) throws UnsupportedBiddingLanguageException {
        List<MRVMGenericDefinition> bands = new ArrayList<>();
        for (MRVMBand band : bidder.getWorld().getBands()) {
            for (MRVMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                bands.add(new MRVMGenericDefinition(band, region));
            }
        }
        return new SimpleRandomOrder(bands, bidder, new JavaUtilRNGSupplier());
    }


    private static final class SimpleRandomOrder extends XORQRandomOrderSimple<MRVMGenericDefinition> {


        private final MRVMBidder bidder;

        /**
         * @param allPossibleGenericDefinitions Collection of generic definitions
         * @param rngSupplier                   Random number generator supplier
         */
        SimpleRandomOrder(Collection<MRVMGenericDefinition> allPossibleGenericDefinitions, MRVMBidder bidder, RNGSupplier rngSupplier)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefinitions, rngSupplier);
            this.bidder = bidder;
        }

        @Override
        public Bidder<MRVMLicense> getBidder() {
            return bidder;
        }

        /**
         * @see GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<MRVMGenericDefinition> getGenericBidder() {
            return bidder;
        }

        /**
         * @see GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<MRVMGenericDefinition> getDefComparator() {
            return comparator;
        }
    }


    private static class BandComparator implements Comparator<MRVMGenericDefinition>, Serializable {

        private static final long serialVersionUID = 8703079363133628481L;

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(MRVMGenericDefinition o1, MRVMGenericDefinition o2) {
            return o1.toString().compareTo(o2.toString());
        }

    }


}

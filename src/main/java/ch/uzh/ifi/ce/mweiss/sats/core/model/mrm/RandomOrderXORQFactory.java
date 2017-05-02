package ch.uzh.ifi.ce.mweiss.sats.core.model.mrm;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.GenericValueBidder;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMRegionsMap.Region;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.JavaUtilRNGSupplier;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.RNGSupplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Michael Weiss
 */
public class RandomOrderXORQFactory {

    @SuppressWarnings("CanBeFinal")
    private static BandComparator comparator = new BandComparator();

    public static XORQRandomOrderSimple<MRMGenericDefinition> getXORQRandomOrderSimpleLang(MRMBidder bidder, RNGSupplier rngSupplier) throws UnsupportedBiddingLanguageException {
        List<MRMGenericDefinition> bands = new ArrayList<>();
        for (MRMBand band : bidder.getWorld().getBands()) {
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                bands.add(new MRMGenericDefinition(band, region));
            }
        }
        return new SimpleRandomOrder(bands, bidder, rngSupplier);
    }

    public static XORQRandomOrderSimple<MRMGenericDefinition> getXORQRandomOrderSimpleLang(MRMBidder bidder) throws UnsupportedBiddingLanguageException {
        List<MRMGenericDefinition> bands = new ArrayList<>();
        for (MRMBand band : bidder.getWorld().getBands()) {
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                bands.add(new MRMGenericDefinition(band, region));
            }
        }
        return new SimpleRandomOrder(bands, bidder, new JavaUtilRNGSupplier());
    }


    private static final class SimpleRandomOrder extends XORQRandomOrderSimple<MRMGenericDefinition> {


        private final MRMBidder bidder;

        /**
         * @param allPossibleGenericDefinitions Collection of generic definitions
         * @param rngSupplier                   Random number generator supplier
         * @throws UnsupportedBiddingLanguageException
         */
        SimpleRandomOrder(Collection<MRMGenericDefinition> allPossibleGenericDefinitions, MRMBidder bidder, RNGSupplier rngSupplier)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefinitions, rngSupplier);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see BiddingLanguage#getBidder()
         */
        @Override
        public Bidder<MRMLicense> getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.satscore.bidlang.generic.SizeOrdered.GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<MRMGenericDefinition> getGenericBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.satscore.bidlang.generic.SizeOrdered.GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<MRMGenericDefinition> getDefComparator() {
            return comparator;
        }
    }


    private static class BandComparator implements Comparator<MRMGenericDefinition> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(MRMGenericDefinition o1, MRMGenericDefinition o2) {
            return o1.toString().compareTo(o2.toString());
        }

    }


}

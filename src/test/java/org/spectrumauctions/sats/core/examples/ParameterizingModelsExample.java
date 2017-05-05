package org.spectrumauctions.sats.core.examples;

import org.junit.Test;
import org.spectrumauctions.sats.core.model.srvm.SRVMBidder;
import org.spectrumauctions.sats.core.model.srvm.SRVMBidderSetup;
import org.spectrumauctions.sats.core.model.srvm.SRVMWorld;
import org.spectrumauctions.sats.core.model.srvm.SRVMWorldSetup;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Michael Weiss on 09.03.2017.
 */
public class ParameterizingModelsExample {

    //For simplicity, we use the same RNGSupplier in all examples of this class
    static RNGSupplier rngSupplier = new JavaUtilRNGSupplier();

    @Test
    public void exampleOne() {
        SRVMWorldSetup setup = new SRVMWorldSetup.Builder().build();
        SRVMWorld world = new SRVMWorld(setup, rngSupplier);
        Collection<SRVMBidderSetup> bidderSetups = new ArrayList<>();
        bidderSetups.add(new SRVMBidderSetup.SmallBidderBuilder().build());
        bidderSetups.add(new SRVMBidderSetup.SecondaryBidderBuilder().build());
        bidderSetups.add(new SRVMBidderSetup.PrimaryBidderBuilder().build());
        bidderSetups.add(new SRVMBidderSetup.HighFrequenceBidderBuilder().build());
        List<SRVMBidder> bidders = world.createPopulation(bidderSetups, rngSupplier);
        // Do something with the bidders
        SimpleModelAccessorsExample.treatBidders(bidders);
    }

    @Test
    public void exampleTwo() {
        SRVMWorldSetup.Builder setupBuilder = new SRVMWorldSetup.Builder();
        // We modify the world to contain exactly 5 licenses of band B
        setupBuilder.putBand(SRVMWorldSetup.Builder.BAND_NAME_B, new IntegerInterval(5));
        // We modify the world to contain between 3 and 6 licenses of band C (chosen uniformly at random)
        setupBuilder.putBand(SRVMWorldSetup.Builder.BAND_NAME_C, new IntegerInterval(3, 6));
        // Note: We didn't change anything for band A
        // Note2: We could have added an additional band, but would have to make sure the later generated bidderSetups
        // define all required parameters for this additional band
        SRVMWorldSetup setup = setupBuilder.build();
        SRVMWorld world = new SRVMWorld(setup, rngSupplier);
        Collection<SRVMBidderSetup> bidderSetups = new ArrayList<>();
        SRVMBidderSetup.SmallBidderBuilder smallBidderBuilder = new SRVMBidderSetup.SmallBidderBuilder();
        // We change the bidder strenght of all small bidders to be exactly 0.5
        smallBidderBuilder.setBidderStrength(new DoubleInterval(0.5));
        bidderSetups.add(smallBidderBuilder.build());
        SRVMBidderSetup.SecondaryBidderBuilder secondaryBidderBuilder = new SRVMBidderSetup.SecondaryBidderBuilder();
        // We change the number of secondary bidders
        secondaryBidderBuilder.setNumberOfBidders(1);
        bidderSetups.add(secondaryBidderBuilder.build());
        // We add the remaining bidderTypes as they are in the default configuration
        bidderSetups.add(new SRVMBidderSetup.PrimaryBidderBuilder().build());
        bidderSetups.add(new SRVMBidderSetup.HighFrequenceBidderBuilder().build());
        // We create a new set of bidders, as specified in the above defined bidder types
        List<SRVMBidder> bidders = world.createPopulation(bidderSetups, rngSupplier);
        // Do something with the bidders
        SimpleModelAccessorsExample.treatBidders(bidders);
    }


}

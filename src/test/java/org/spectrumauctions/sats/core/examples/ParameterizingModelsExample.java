package org.spectrumauctions.sats.core.examples;

import org.spectrumauctions.sats.core.model.srm.SRMWorld;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.model.srm.SRMBidder;
import org.spectrumauctions.sats.core.model.srm.SRMBidderSetup;
import org.spectrumauctions.sats.core.model.srm.SRMWorldSetup;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.junit.Test;

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
    public void exampleOne(){
        SRMWorldSetup setup = new SRMWorldSetup.Builder().build();
        SRMWorld world = new SRMWorld(setup, rngSupplier);
        Collection<SRMBidderSetup> bidderSetups = new ArrayList<>();
        bidderSetups.add(new SRMBidderSetup.SmallBidderBuilder().build());
        bidderSetups.add(new SRMBidderSetup.SecondaryBidderBuilder().build());
        bidderSetups.add(new SRMBidderSetup.PrimaryBidderBuilder().build());
        bidderSetups.add(new SRMBidderSetup.HighFrequenceBidderBuilder().build());
        List<SRMBidder> bidders = world.createPopulation(bidderSetups, rngSupplier);
        // Do something with the bidders
        SimpleModelAccessorsExample.treatBidders(bidders);
    }

    @Test
    public void exampleTwo(){
        SRMWorldSetup.Builder setupBuilder = new SRMWorldSetup.Builder();
        // We modify the world to contain exactly 5 licenses of band B
        setupBuilder.putBand(SRMWorldSetup.Builder.BAND_NAME_B, new IntegerInterval(5));
        // We modify the world to contain between 3 and 6 licenses of band C (chosen uniformly at random)
        setupBuilder.putBand(SRMWorldSetup.Builder.BAND_NAME_C, new IntegerInterval(3,6));
        // Note: We didn't change anything for band A
        // Note2: We could have added an additional band, but would have to make sure the later generated bidderSetups
        // define all required parameters for this additional band
        SRMWorldSetup setup = setupBuilder.build();
        SRMWorld world = new SRMWorld(setup, rngSupplier);
        Collection<SRMBidderSetup> bidderSetups = new ArrayList<>();
        SRMBidderSetup.SmallBidderBuilder smallBidderBuilder = new SRMBidderSetup.SmallBidderBuilder();
        // We change the bidder strenght of all small bidders to be exactly 0.5
        smallBidderBuilder.setBidderStrength(new DoubleInterval(0.5));
        bidderSetups.add(smallBidderBuilder.build());
        SRMBidderSetup.SecondaryBidderBuilder secondaryBidderBuilder = new SRMBidderSetup.SecondaryBidderBuilder();
        // We change the number of secondary bidders
        secondaryBidderBuilder.setNumberOfBidders(1);
        bidderSetups.add(secondaryBidderBuilder.build());
        // We add the remaining bidderTypes as they are in the default configuration
        bidderSetups.add(new SRMBidderSetup.PrimaryBidderBuilder().build());
        bidderSetups.add(new SRMBidderSetup.HighFrequenceBidderBuilder().build());
        // We create a new set of bidders, as specified in the above defined bidder types
        List<SRMBidder> bidders = world.createPopulation(bidderSetups, rngSupplier);
        // Do something with the bidders
        SimpleModelAccessorsExample.treatBidders(bidders);
    }



}

package org.spectrumauctions.sats.opt.examples;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.opt.model.mrvm.MRVMMipResult;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * <p>These examples show the customized usage of SATS-OPT with the MRVM.
 * For the other models it works analogously.
 * <p>They are focused on creating custom worlds and custom bidders.
 * To see the basic usage check {@link BasicExamples}.
 *
 * @author Fabio Isler
 */
public class CustomizedExamples {

    /**
     * Setting the number of bidders is possible on {@link MultiRegionModel} level.
     */
    @Test
    public void differentNumberOfBiddersMRVMExample() {
        MultiRegionModel model = new MultiRegionModel();
        model.setNumberOfLocalBidders(3);
        model.setNumberOfNationalBidders(1);
        model.setNumberOfRegionalBidders(2);

        Collection<MRVMBidder> bidders = model.createNewPopulation();   // Create bidders
        MRVM_MIP mip = new MRVM_MIP(bidders);                           // Create the MIP
        MRVMMipResult result = mip.calculateAllocation();               // Solve the MIP
        System.out.println(result);                                     // Show the allocation
    }

    /**
     * To customize the world, {@link MRVMWorldSetup} and the available builders can be used
     */
    @Test
    public void customizedWorldMRVMExample() {
        MRVMWorldSetup.MRVMWorldSetupBuilder worldSetupBuilder = new MRVMWorldSetup.MRVMWorldSetupBuilder();

        // Remove a default band and change the number of lots for another one
        worldSetupBuilder.removeBandSetup("UNPAIRED");
        worldSetupBuilder.changeNumberOfLots("LOW_PAIRED", new IntegerInterval(1, 15));

        // Create a new band
        ImmutableMap<Integer, BigDecimal> synergies = (new ImmutableMap.Builder<Integer, BigDecimal>()).put(1, new BigDecimal(1.2)).build();
        MRVMWorldSetup.BandSetup bandSetup = new MRVMWorldSetup.BandSetup(
                "NEWBAND",                          // name
                new IntegerInterval(6),             // number of lots
                new DoubleInterval(1.0),            // base capacity
                synergies                           // synergy map as defined above
        );
        worldSetupBuilder.putBandSetup(bandSetup);

        // Create a new graph
        worldSetupBuilder.createGraphRandomly(
                new IntegerInterval(5, 8),          // number of regions
                new IntegerInterval(2, 3),          // average of adjacencies per region
                100000,                             // average population per region
                0                                   // standard deviation of population per region
        );

        // Create the world configured by the world setup
        MRVMWorld world = new MRVMWorld(worldSetupBuilder.build(), new JavaUtilRNGSupplier());

        // Add standard bidders
        Collection<MRVMBidder> bidders = world.createPopulation(
                new MRVMLocalBidderSetup.Builder().build(),
                new MRVMRegionalBidderSetup.Builder().build(),
                new MRVMNationalBidderSetup.Builder().build(),
                new JavaUtilRNGSupplier()
        );
        MRVM_MIP mip = new MRVM_MIP(bidders);                           // Create the MIP
        MRVMMipResult result = mip.calculateAllocation();               // Solve the MIP
        System.out.println(result);                                     // Show the allocation
    }

    /**
     * To customize the bidders, the different {@link MRVMBidderSetup} and the available builders can be used
     */
    @Test
    public void customizedBiddersMRVMExample() {
        MRVMLocalBidderSetup.Builder localBuilder = new MRVMLocalBidderSetup.Builder();
        MRVMRegionalBidderSetup.Builder regionalBuilder = new MRVMRegionalBidderSetup.Builder();

        // Customize the setups
        localBuilder.setNumberOfRegionsInterval(new IntegerInterval(3));
        localBuilder.setNumberOfBidders(4);
        regionalBuilder.setGammaShape(1.0, 1.0);
        regionalBuilder.setNumberOfBidders(2);

        // Create the world for the bidders
        MRVMWorldSetup.MRVMWorldSetupBuilder worldSetupBuilder = new MRVMWorldSetup.MRVMWorldSetupBuilder();
        MRVMWorld world = new MRVMWorld(worldSetupBuilder.build(), new JavaUtilRNGSupplier());

        // Create the bidders based on the setups
        Collection<MRVMBidder> bidders = world.createPopulation(
                localBuilder.build(),
                regionalBuilder.build(),
                null,                                                   // Don't define any national bidders
                new JavaUtilRNGSupplier()
        );

        MRVM_MIP mip = new MRVM_MIP(bidders);                           // Create the MIP
        MRVMMipResult result = mip.calculateAllocation();               // Solve the MIP
        System.out.println(result);                                     // Show the allocation
    }

}

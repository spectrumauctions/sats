package org.spectrumauctions.sats.opt.examples;

import org.junit.Test;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.core.model.gsvm.GSVMWorld;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;
import org.spectrumauctions.sats.core.model.srvm.SRVMBidder;
import org.spectrumauctions.sats.core.model.srvm.SingleRegionModel;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;
import org.spectrumauctions.sats.opt.model.mrvm.MRVMMipResult;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;
import org.spectrumauctions.sats.opt.model.srvm.SRVMMipResult;
import org.spectrumauctions.sats.opt.model.srvm.SRVM_MIP;
import org.spectrumauctions.sats.opt.vcg.external.vcg.ItemAllocation;

import java.util.Collection;
import java.util.List;

/**
 * <p>These examples show the basic usage of SATS-OPT for each supported model.
 * <p>They are based on the standard population of the standard worlds.
 * To see how to customize it check {@link CustomizedExamples}
 *
 * @author Fabio Isler
 */
public class BasicExamples {

    @Test
    public void basicMRVMExample() {
        Collection<MRVMBidder> bidders = (new MultiRegionModel()).createNewPopulation();    // Create bidders
        MRVM_MIP mip = new MRVM_MIP(bidders);                                               // Create the MIP
        MRVMMipResult result = mip.calculateAllocation();                                   // Solve the MIP
        System.out.println(result);                                                         // Show the allocation
    }

    @Test
    public void basicSRVMExample() {
        Collection<SRVMBidder> bidders = (new SingleRegionModel()).createNewPopulation();   // Create bidders
        SRVM_MIP mip = new SRVM_MIP(bidders);                                               // Create the MIP
        SRVMMipResult result = mip.calculateAllocation();                                   // Solve the MIP
        System.out.println(result);                                                         // Show the allocation
    }

    @Test
    public void basicGSVMExample() {
        List<GSVMBidder> bidders = (new GlobalSynergyValueModel()).createNewPopulation();   // Create bidders
        // TODO: align this with other models:
        // - just pass a collection of bidders
        // - include build() in calculateAllocation()
        // - Align result handling
        GSVMWorld world = bidders.stream().findFirst().get().getWorld();                    // Get the world
        GSVMStandardMIP mip = new GSVMStandardMIP(world, bidders);                          // Create the MIP
        mip.build();                                                                        // Build the MIP
        ItemAllocation<GSVMLicense> result = mip.calculateAllocation();                     // Solve the MIP
        for (Bidder<GSVMLicense> bidder : result.getBidders()) {                            // Show the allocation
            System.out.print(bidder.getId() + ":\t[ ");
            for (GSVMLicense license : result.getAllocation(bidder)) {
                System.out.print(license.getId() + ", ");
            }
            System.out.println("]");
        }

    }

}

package org.spectrumauctions.sats.opt.examples;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.marketdesignresearch.mechlib.core.Allocation;
import org.marketdesignresearch.mechlib.core.bidder.Bidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;
import org.spectrumauctions.sats.core.model.srvm.SRVMBidder;
import org.spectrumauctions.sats.core.model.srvm.SingleRegionModel;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;
import org.spectrumauctions.sats.opt.model.srvm.SRVM_MIP;

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

    private static final Logger logger = LogManager.getLogger(BasicExamples.class);

    @Test
    public void basicMRVMExample() {
        Collection<MRVMBidder> bidders = (new MultiRegionModel()).createNewWorldAndPopulation();    // Create bidders
        MRVM_MIP mip = new MRVM_MIP(bidders);                                               // Create the MIP
        Allocation result = mip.getAllocation();                                            // Solve the MIP
        for (Bidder bidder : result.getWinners()) {                                         // Show the allocation
            String sb = bidder.getName() +
                    ":\t[ " +
                    result.allocationOf(bidder).getBundle() +
                    " ]";
            logger.info(sb);
        }
    }

    @Test
    public void basicSRVMExample() {
        Collection<SRVMBidder> bidders = (new SingleRegionModel()).createNewWorldAndPopulation();   // Create bidders
        SRVM_MIP mip = new SRVM_MIP(bidders);                                               // Create the MIP
        Allocation result = mip.getAllocation();                                            // Solve the MIP
        for (Bidder bidder : result.getWinners()) {                                         // Show the allocation
            String sb = bidder.getName() +
                    ":\t[ " +
                    result.allocationOf(bidder).getBundle() +
                    " ]";
            logger.info(sb);
        }
    }

    @Test
    public void basicGSVMExample() {
        List<GSVMBidder> bidders = (new GlobalSynergyValueModel()).createNewWorldAndPopulation();   // Create bidders
        GSVMStandardMIP mip = new GSVMStandardMIP(bidders);                                 // Create the MIP
        Allocation result = mip.getAllocation();                                            // Solve the MIP
        for (Bidder bidder : result.getWinners()) {                                         // Show the allocation
            String sb = bidder.getName() +
                    ":\t[ " +
                    result.allocationOf(bidder).getBundle() +
                    " ]";
            logger.info(sb);
        }

    }

}

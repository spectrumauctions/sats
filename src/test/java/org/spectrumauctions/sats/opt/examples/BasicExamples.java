package org.spectrumauctions.sats.opt.examples;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
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
import org.spectrumauctions.sats.opt.domain.ItemAllocation;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;
import org.spectrumauctions.sats.opt.model.mrvm.MRVMMipResult;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;
import org.spectrumauctions.sats.opt.model.srvm.SRVMMipResult;
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
    @Ignore
    public void basicMRVMExample() {
        Collection<MRVMBidder> bidders = (new MultiRegionModel()).createPopulation();    // Create bidders
        MRVM_MIP mip = new MRVM_MIP(bidders);                                               // Create the MIP
        MRVMMipResult result = mip.calculateAllocation();                                   // Solve the MIP
        logger.info(result);                                                                // Show the allocation
    }

    @Test
    @Ignore
    public void basicSRVMExample() {
        Collection<SRVMBidder> bidders = (new SingleRegionModel()).createPopulation();   // Create bidders
        SRVM_MIP mip = new SRVM_MIP(bidders);                                               // Create the MIP
        SRVMMipResult result = mip.calculateAllocation();                                   // Solve the MIP
        logger.info(result);                                                                // Show the allocation
    }

    @Test
    @Ignore
    public void basicGSVMExample() {
        List<GSVMBidder> bidders = (new GlobalSynergyValueModel()).createPopulation();   // Create bidders
        GSVMStandardMIP mip = new GSVMStandardMIP(bidders);                                 // Create the MIP
        ItemAllocation<GSVMLicense> result = mip.calculateAllocation();                     // Solve the MIP
        for (Bidder<GSVMLicense> bidder : result.getWinners()) {
            StringBuilder sb = new StringBuilder();                                         // Show the allocation
            sb.append(bidder.getId());
            sb.append(":\t[ ");
            for (GSVMLicense license : result.getAllocation(bidder)) {
                sb.append(license.getId());
                sb.append(", ");
            }
            sb.append("]");
            logger.info(sb.toString());
        }

    }

}

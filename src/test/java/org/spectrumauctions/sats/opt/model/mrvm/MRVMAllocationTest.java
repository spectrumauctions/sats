package org.spectrumauctions.sats.opt.model.mrvm;

import org.junit.Assert;
import org.junit.Test;
import org.marketdesignresearch.mechlib.domain.Allocation;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMWorld;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;

import java.util.List;

public class MRVMAllocationTest {

    @Test
    public void testEqualAllocationWhenSolvingTwice() {
        MultiRegionModel model = new MultiRegionModel();
        MRVMWorld world =  model.createWorld(1234567);
        List<MRVMBidder> biddersList = model.createPopulation(world, 1234567);
        MRVM_MIP mip = new MRVM_MIP(biddersList);
        mip.setDisplayOutput(true);
        Allocation result1 = mip.getAllocation();
        Allocation result2 = mip.getAllocation();
        for (MRVMBidder bidder : biddersList) {
            Assert.assertEquals(result1.allocationOf(bidder).getBundle().getTotalAmount(), result2.allocationOf(bidder).getBundle().getTotalAmount());
        }
        Assert.assertEquals(result1.getTotalAllocationValue(), result2.getTotalAllocationValue());
    }

    @Test
    public void testEqualAllocationWhenCreatingMIPTwice() {
        MultiRegionModel model = new MultiRegionModel();
        MRVMWorld world =  model.createWorld(1234567);
        List<MRVMBidder> biddersList = model.createPopulation(world, 1234567);
        MRVM_MIP mip1 = new MRVM_MIP(biddersList);
        mip1.setDisplayOutput(true);
        MRVM_MIP mip2 = new MRVM_MIP(biddersList);
        mip2.setDisplayOutput(true);
        Allocation result1 = mip1.getAllocation();
        Allocation result2 = mip2.getAllocation();
        for (MRVMBidder bidder : biddersList) {
            Assert.assertEquals(result1.allocationOf(bidder).getBundle().getTotalAmount(), result2.allocationOf(bidder).getBundle().getTotalAmount());
        }
    }

}

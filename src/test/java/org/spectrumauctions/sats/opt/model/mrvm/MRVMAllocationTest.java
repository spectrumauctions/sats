package org.spectrumauctions.sats.opt.model.mrvm;

import org.junit.Assert;
import org.junit.Test;
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
        MRVMMipResult result1 = mip.calculateAllocation();
        MRVMMipResult result2 = mip.calculateAllocation();
        for (MRVMBidder bidder : biddersList) {
            Assert.assertEquals(result1.getGenericAllocation(bidder).getTotalQuantity(), result2.getGenericAllocation(bidder).getTotalQuantity());
        }
        Assert.assertEquals(result1.getTotalValue(), result2.getTotalValue());
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
        MRVMMipResult result1 = mip1.calculateAllocation();
        MRVMMipResult result2 = mip2.calculateAllocation();
        for (MRVMBidder bidder : biddersList) {
            Assert.assertEquals(result1.getGenericAllocation(bidder).getTotalQuantity(), result2.getGenericAllocation(bidder).getTotalQuantity());
        }
    }

}

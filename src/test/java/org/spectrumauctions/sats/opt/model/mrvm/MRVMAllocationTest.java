package org.spectrumauctions.sats.opt.model.mrvm;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.marketdesignresearch.mechlib.domain.Allocation;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMWorld;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
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

    @Test
    public void testRuntime() {
        List<Long> runtimes = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            MultiRegionModel model = new MultiRegionModel();
            MRVMWorld world =  model.createWorld();
            List<MRVMBidder> biddersList = model.createPopulation(world);
            MRVM_MIP mip = new MRVM_MIP(biddersList);
            mip.setTimeLimit(600);
            long start = System.currentTimeMillis();
            Allocation allocation = mip.getAllocation();
            long end = System.currentTimeMillis();
            log.info("Allocation of run {}: {}", i + 1, allocation);
            runtimes.add(end - start);
        }

        long total = runtimes.stream().mapToLong(l -> l).sum();
        double average = runtimes.stream().mapToLong(l -> l).average().orElseThrow(IllegalArgumentException::new);

        log.info("Done.");
        log.info("Total runtime: {}ms", total);
        log.info("Average runtime: {}ms", BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
        log.info("Runtimes (in ms):");
        for (int i = 0; i < runtimes.size(); i++) {
            log.info("\tRun {}: {}", i + 1, runtimes.get(i));
        }

    }

}

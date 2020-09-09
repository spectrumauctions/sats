/**
 * s * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import org.junit.Before;
import org.junit.Test;
import org.marketdesignresearch.mechlib.core.allocationlimits.AllocationLimit;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Weiss
 *
 */
public class MRVMBidderTest {

    private MRVMLocalBidderSetup setup = mock(MRVMLocalBidderSetup.class);
    private MRVMRegionsMap map = mock(MRVMRegionsMap.class);
    private MRVMRegionsMap.Region region1 = mock(MRVMRegionsMap.Region.class);
    private MRVMRegionsMap.Region region2 = mock(MRVMRegionsMap.Region.class);
    private MRVMWorld world;
    private MRVMBidder bidder;

    @Before
    public void setup() {
        world = new MRVMWorld(new MRVMWorldSetup.MRVMWorldSetupBuilder().build(), new JavaUtilRNGSupplier());
        when(map.getRegions()).thenReturn(new HashSet<>(Arrays.asList(region1, region2)));
        when(map.getNumberOfRegions()).thenReturn(2);
        when(region1.getId()).thenReturn(0);
        when(region2.getId()).thenReturn(1);
        when(setup.drawAlpha(any(UniformDistributionRNG.class))).thenReturn(BigDecimal.valueOf(0.1));
        when(setup.drawBeta(any(MRVMRegionsMap.Region.class), any(UniformDistributionRNG.class))).thenReturn(BigDecimal.valueOf(0.2)); // Same Beta for all Regions
        HashMap<Integer, BigDecimal> zlowMock = mock(HashMap.class);
        when(zlowMock.get(any())).thenReturn(BigDecimal.valueOf(0.3));
        when(setup.drawZLow(any(Map.class), any(MRVMWorld.class), any(UniformDistributionRNG.class))).thenReturn(zlowMock);
        HashMap<Integer, BigDecimal> zhighMock = mock(HashMap.class);
        when(zhighMock.get(any())).thenReturn(BigDecimal.valueOf(0.4));
        when(setup.drawZHigh(any(Map.class), any(MRVMWorld.class), any(UniformDistributionRNG.class))).thenReturn(zhighMock);
    }

    @Test
    public void testCorrectlyInitialized() {
        bidder = new MRVMLocalBidder(0, 0, world, setup, null, AllocationLimit.NO);
        assertEquals(0, bidder.getAlpha().compareTo(BigDecimal.valueOf(0.1))); //alpha == 0.1
        assertEquals(0, bidder.getBeta(region1).compareTo(BigDecimal.valueOf(0.2))); //beta1 == 0.2
        assertEquals(0, bidder.getBeta(region2).compareTo(BigDecimal.valueOf(0.2))); //beta2 == 0.2
        assertEquals(0, bidder.getzLow(region1).compareTo(BigDecimal.valueOf(0.3))); //zlow == 0.3
        assertEquals(0, bidder.getzHigh(region1).compareTo(BigDecimal.valueOf(0.4))); //zHigh == 0.4
    }


    @Test
    public void testSvFunction() {
        bidder = new MRVMLocalBidder(0, 0, world, setup, null, AllocationLimit.NO);
        when(region1.getPopulation()).thenReturn(50);
        //Check corner points
        //		Corner point 1
        BigDecimal expected1 = BigDecimal.ZERO;
        BigDecimal actual1 = bidder.svFunction(region1, BigDecimal.ZERO);
        assertEqualsWithDelta(actual1, expected1);
        //		Corner point 2
        BigDecimal expected2 = BigDecimal.valueOf(0.1 * 0.27);
        BigDecimal actual2 = bidder.svFunction(region1, BigDecimal.valueOf(0.3 * 50 * 0.2));
        assertEqualsWithDelta(actual2, expected2);
        //		Corner point 3
        BigDecimal expected3 = BigDecimal.valueOf(0.1 * 0.73);
        BigDecimal actual3 = bidder.svFunction(region1, BigDecimal.valueOf(0.4 * 50 * 0.2));
        assertEqualsWithDelta(actual3, expected3);
        //		Corner point 4
        BigDecimal expected4 = bidder.getAlpha();
        BigDecimal actual4 = bidder.svFunction(region1, world.getMaximumRegionalCapacity());
        assertEqualsWithDelta(actual4, expected4);

        //In between corner points
        BigDecimal c = BigDecimal.valueOf((0.4 * 50 * 0.2 + 0.3 * 50 * 0.2) / 2);
        BigDecimal expectedMiddle = expected2.add(expected3).divide(BigDecimal.valueOf(2), RoundingMode.HALF_DOWN);
        BigDecimal actual = bidder.svFunction(region1, c);
        assertEqualsWithDelta(actual, expectedMiddle);
    }


    private void assertEqualsWithDelta(BigDecimal actual, BigDecimal expected) {
        assertEquals("actual: " + actual.toString() + " expected " + expected.toString(),
                0,
                actual.setScale(5, RoundingMode.HALF_DOWN).compareTo(expected.setScale(5, RoundingMode.HALF_DOWN)));
    }

}

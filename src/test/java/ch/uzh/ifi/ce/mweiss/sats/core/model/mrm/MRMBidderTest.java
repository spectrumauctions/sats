/**
s * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.model.mrm;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import ch.uzh.ifi.ce.mweiss.sats.core.util.random.UniformDistributionRNG;
import org.junit.Before;
import org.junit.Test;

import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMRegionsMap.Region;

/**
 * @author Michael Weiss
 *
 */
public class MRMBidderTest {

	private MRMWorld world = mock(MRMWorld.class);
	private MRMLocalBidderSetup setup = mock(MRMLocalBidderSetup.class);
	private MRMRegionsMap map = mock(MRMRegionsMap.class);
	private Region region1 = mock(Region.class);
	private Region region2 = mock(Region.class);
	private MRMBidder bidder;
	
	@Before
	public void setup(){
		when(map.getRegions()).thenReturn(new HashSet<>(Arrays.asList(region1, region2)));
		when(map.getNumberOfRegions()).thenReturn(2);
		when(region1.getId()).thenReturn(0);
		when(region2.getId()).thenReturn(1);
		when(world.getRegionsMap()).thenReturn(map);
		when(setup.drawAlpha(any(UniformDistributionRNG.class))).thenReturn(BigDecimal.valueOf(0.1));
		when(setup.drawBeta(any(Region.class), any(UniformDistributionRNG.class))).thenReturn(BigDecimal.valueOf(0.2)); // Same Beta for all Regions
		Map<Integer, BigDecimal> zlowMock = mock(Map.class);
		when(zlowMock.get(any())).thenReturn(BigDecimal.valueOf(0.3));
		when(setup.drawZLow(any(Map.class),any(MRMWorld.class), any(UniformDistributionRNG.class))).thenReturn(zlowMock);
		Map<Integer, BigDecimal> zhighMock = mock(Map.class);
		when(zhighMock.get(any())).thenReturn(BigDecimal.valueOf(0.4));
		when(setup.drawZHigh(any(Map.class),any(MRMWorld.class), any(UniformDistributionRNG.class))).thenReturn(zhighMock);
	}
	
	@Test
	public void testCorrectlyInitialized(){
		bidder = new MRMLocalBidder(0, 0, world, setup, null);
		assertTrue(bidder.getAlpha().compareTo(BigDecimal.valueOf(0.1)) == 0); //alpha == 0.1
		assertTrue(bidder.getBeta(region1).compareTo(BigDecimal.valueOf(0.2)) == 0); //beta1 == 0.2
		assertTrue(bidder.getBeta(region2).compareTo(BigDecimal.valueOf(0.2)) == 0); //beta2 == 0.2
		assertTrue(bidder.getzLow(region1).compareTo(BigDecimal.valueOf(0.3)) == 0); //zlow == 0.3
		assertTrue(bidder.getzHigh(region1).compareTo(BigDecimal.valueOf(0.4)) == 0); //zHigh == 0.4
	}


	@Test
	public void testSvFunction(){
		bidder = new MRMLocalBidder(0,0,world, setup, null);
		when(region1.getPopulation()).thenReturn(50);
		when(world.getMaximumRegionalCapacity()).thenReturn(BigDecimal.valueOf(10));
		//Check corner points
		//		Corner point 1
		BigDecimal expected1 = BigDecimal.ZERO;
		BigDecimal actual1 = bidder.svFunction(region1, BigDecimal.ZERO);
		assertEqualsWithDelta(actual1, expected1);
		//		Corner point 2
		BigDecimal expected2 = BigDecimal.valueOf(0.1*0.27);
		BigDecimal actual2 = bidder.svFunction(region1, BigDecimal.valueOf(0.3*50*0.2));
		assertEqualsWithDelta(actual2, expected2);
		//		Corner point 3
		BigDecimal expected3 = BigDecimal.valueOf(0.1*0.73);
		BigDecimal actual3 = bidder.svFunction(region1, BigDecimal.valueOf(0.4*50*0.2));
		assertEqualsWithDelta(actual3, expected3);		
		//		Corner point 4
		BigDecimal expected4 = bidder.getAlpha();
		BigDecimal actual4 = bidder.svFunction(region1, BigDecimal.valueOf(10));
		assertEqualsWithDelta(actual4, expected4);	
		
		//In between corner points
		BigDecimal c = BigDecimal.valueOf((0.4*50*0.2 + 0.3*50*0.2)/2);
		BigDecimal expectedMiddle = expected2.add(expected3).divide(BigDecimal.valueOf(2), RoundingMode.HALF_DOWN);
		BigDecimal actual = bidder.svFunction(region1, c);
		assertEqualsWithDelta(actual, expectedMiddle);
	}
	
	
	
	private void assertEqualsWithDelta(BigDecimal actual, BigDecimal expected){
		assertTrue("actual: " + actual.toString() + " expected " + expected.toString(),
				actual.setScale(5, RoundingMode.HALF_DOWN)
				.compareTo(
						expected.setScale(5, RoundingMode.HALF_DOWN)) == 0	);
	}

}

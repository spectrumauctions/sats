/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.spectrumauctions.sats.core.model.Bundle;

/**
 * @author Michael Weiss
 *
 */
public class MRVMWorldTest {

    private static MRVMWorld world;
    
    private static Bundle<MRVMLicense> completeBundle;
      

    @BeforeClass
    public static void setUpBeforeClass(){
        world = new MRVMWorld(MRMSimpleWorldGen.getSimpleWorldBuilder(),new JavaUtilRNGSupplier(983742L));
        completeBundle = new Bundle<>(world.getLicenses());
    }
    
    @Test 
    /**
     * Checks if the complete bundle is split correcly into regional bundles
     */
    public void selectAllLicensesOfRegionOnCompleteBundle(){
        Map<MRVMRegionsMap.Region, Bundle<MRVMLicense>> regionalBundles = MRVMWorld.getLicensesPerRegion(completeBundle);
        int expectedNumberOfLicenses = 0;
        for(MRVMBand band : world.getBands()){
            expectedNumberOfLicenses += band.getNumberOfLots();
        }
        for(MRVMRegionsMap.Region region : world.getRegionsMap().getRegions()){
            Assert.assertTrue(regionalBundles.containsKey(region));
            Assert.assertEquals(regionalBundles.get(region).size(), expectedNumberOfLicenses);
            for(MRVMLicense license : regionalBundles.get(region)){
                Assert.assertEquals(license.getRegion(), region);
            }
        }        
    }
    
    @Test
    public void numberOfLotsGeneratedCorrectly(){
        int numberOfRegions = world.getRegionsMap().getRegions().size();
        for(MRVMBand band : world.getBands()){
            int expectedNumberOfLicenses = band.getNumberOfLots()* numberOfRegions;
            Assert.assertEquals(expectedNumberOfLicenses, band.getNumberOfLicenses());
            Assert.assertEquals(expectedNumberOfLicenses, band.getLicenses().size());
        }
    }
    
    @Test
    public void selectAllLicensesOfBandOnCompleteBundle(){
        Map<MRVMBand, Bundle<MRVMLicense>> regionalBundles = MRVMWorld.getLicensesPerBand(completeBundle);
        for(MRVMBand band : world.getBands()){
            Assert.assertTrue(regionalBundles.containsKey(band));
            int expectedNumberOfLicenses = band.getNumberOfLots() * world.getRegionsMap().getNumberOfRegions();
            Assert.assertEquals(regionalBundles.get(band).size(), expectedNumberOfLicenses);
            for(MRVMLicense license : regionalBundles.get(band)){
                Assert.assertEquals(license.getBand(), band);
            }
        }     
    }
    
    @Test
    public void capacityIsCorrectlyCalculatedCompleteRegionalBundle(){
        Map<MRVMRegionsMap.Region, Bundle<MRVMLicense>> regionalBundles = MRVMWorld.getLicensesPerRegion(new Bundle<>(world.getLicenses()));
        BigDecimal expectedCapacity = BigDecimal.ZERO;
        for (MRVMBand band : world.getBands()){
            int quantity = band.getNumberOfLots();
            BigDecimal synergy = band.getSynergy(quantity);
            BigDecimal baseCapacity = band.getBaseCapacity();
            BigDecimal regionalCap = synergy.multiply(baseCapacity).multiply(new BigDecimal(quantity));
            expectedCapacity = expectedCapacity.add(regionalCap);            
        }
        BigDecimal manuallyCalculated = new BigDecimal(2*20*2 + 6*10*1);
        Assert.assertTrue(expectedCapacity.compareTo(manuallyCalculated)==0); //Checks if band values are instantated correctly
        for(Entry<MRVMRegionsMap.Region, Bundle<MRVMLicense>> regionalBundle : regionalBundles.entrySet()){
            BigDecimal bundleRegionalCapacity = MRVMWorld.c(regionalBundle.getKey(), regionalBundle.getValue());
            Assert.assertTrue(expectedCapacity.compareTo(bundleRegionalCapacity) == 0);
        }    
    }
    
    /**
     * Tests constraints, if the calculated distance between any two regions is possible 
     * e.g., ensures that distance between neigbors is 1 and distance between non-neigbors and non-equal is bigger than 1
     */
    @Test
    public void regionalDistanceTests(){
        MRVMRegionsMap.Region anyRegion = world.getRegionsMap().getRegions().iterator().next();
        for(MRVMRegionsMap.Region r : world.getRegionsMap().getRegions()){
            if(r.equals(anyRegion)){
                Assert.assertEquals(world.getRegionsMap().getDistance(anyRegion, r), 0);
                Assert.assertFalse(world.getRegionsMap().areAdjacent(r, anyRegion));
            }else if(world.getRegionsMap().areAdjacent(r, anyRegion)){
                Assert.assertEquals(world.getRegionsMap().getDistance(anyRegion, r), 1);
            }else{
                Assert.assertTrue(world.getRegionsMap().getDistance(anyRegion, r) > 1);
            }
        }
    }
    
    
    
   
    
    

}

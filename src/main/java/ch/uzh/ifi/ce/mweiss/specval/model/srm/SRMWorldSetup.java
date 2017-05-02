/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.srm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import ch.uzh.ifi.ce.mweiss.specval.util.random.IntegerInterval;
import ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier;
import ch.uzh.ifi.ce.mweiss.specval.util.random.UniformDistributionRNG;

/**
 * @author Michael Weiss
 *
 */
public class SRMWorldSetup {
    
    private final ImmutableMap<String, IntegerInterval> bandDefinitions;
    
    private SRMWorldSetup(Builder builder){
        this.bandDefinitions = ImmutableMap.copyOf(builder.bandDefinitions);
    }
    
    public Map<String, Integer> defineBands(RNGSupplier rngSupplier){
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        Map<String, Integer> bands = new HashMap<>();
        for(Entry<String, IntegerInterval> bandDefinition : bandDefinitions.entrySet()){
            bands.put(bandDefinition.getKey(), rng.nextInt(bandDefinition.getValue()));
        }
        return bands;
    }
    
    
    
    public static class Builder{

        /**
         * The name used to represent the paired 800MHz band
         */
        public static final String BAND_NAME_A = "A";
        
        /**
         * The name used to represent the paired 2.6GHz band
         */
        public static final String BAND_NAME_B = "B";
        
        /**
         * The name used to represent the unpaired 2.6GHz band
         */
        public static final String BAND_NAME_C = "C";
        
        public Map<String, IntegerInterval> bandDefinitions;
         
        public Builder(){
            bandDefinitions = new HashMap<>();
            bandDefinitions.put("A", new IntegerInterval(6));
            bandDefinitions.put("B", new IntegerInterval(14));
            bandDefinitions.put("C", new IntegerInterval(9));
        }
        
        /**
         * Define a new band to be generated as well as the number of licenses (drawn from interval, see {@link SRMWorldSetup#defineBands(UniformDistributionRNG)})
         * @param bandName
         * @param numberOfLicenses
         */
        public void putBand(String bandName, IntegerInterval numberOfLicenses){
            Preconditions.checkArgument(numberOfLicenses.isStrictlyPositive());
            bandDefinitions.put(bandName, numberOfLicenses);
        }
        
        public Map<String, IntegerInterval> getBandDefinitions(){
            return Collections.unmodifiableMap(bandDefinitions);
        }
        
        public void removeBand(String bandName){
            bandDefinitions.remove(bandName);
        }
        
        public SRMWorldSetup build() {
            return new SRMWorldSetup(this);
        }
    }

}

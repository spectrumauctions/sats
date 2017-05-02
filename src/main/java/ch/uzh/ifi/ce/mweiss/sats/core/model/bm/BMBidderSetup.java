/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.model.bm;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ch.uzh.ifi.ce.mweiss.sats.core.util.PreconditionUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import ch.uzh.ifi.ce.mweiss.sats.core.model.BidderSetup;
import ch.uzh.ifi.ce.mweiss.sats.core.model.IncompatibleWorldException;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.DoubleInterval;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.IntegerInterval;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.UniformDistributionRNG;



/**
 * A configuration of a Bidder, 
 * i.e., the specification of the random parameters of the bidder.
 *  
 * @author Michael Weiss
 *
 */
public abstract class BMBidderSetup extends BidderSetup {
   
    protected final Map<String, IntegerInterval> positiveValueThresholdIntervals;
    protected final Map<String, DoubleInterval> baseValueIntervals;
    protected final Map<String, ImmutableMap<Integer, BigDecimal>> synFactors;

    protected BMBidderSetup(BMBidderSetupBuilder builder){     
        super(builder);
        this.positiveValueThresholdIntervals = ImmutableMap.copyOf(builder.positiveValueThresholdIntervals);
        this.baseValueIntervals = ImmutableMap.copyOf(builder.baseValueIntervals);
        ImmutableMap.Builder<String, ImmutableMap<Integer, BigDecimal>> mapBuilder = ImmutableMap.<String, ImmutableMap<Integer, BigDecimal>>builder();
        for(Entry<String, Map<Integer, BigDecimal>> entry : builder.synFactors.entrySet()){
            mapBuilder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
        }
        this.synFactors = mapBuilder.build();
    }
    
    /**
     * A parameter defining the quantity of licences per band, for which this bidder
     * has a strictly positive value. For quantities exceeding this threshold, a bidder does
     * not have additional value (free disposal).
     * @param band
     * @param rng
     * @return
     */
    public Integer drawPositiveValueThreshold(BMBand band, UniformDistributionRNG rng){
        IntegerInterval interval = positiveValueThresholdIntervals.get(band.getName());
        if(interval == null){
            throw new IncompatibleWorldException("Band name unknown to Bidder Setup");
        }else{
            return rng.nextInt(interval);
        }
    }

    /**
     * The base value of a band is a bidder specific valuation a bidder has 
     * per license of the specified band, ignoring synergies and value thresholds
     * @param band
     * @param rng
     * @return
     */
    public BigDecimal drawBaseValue(BMBand band, UniformDistributionRNG rng){
        DoubleInterval interval = baseValueIntervals.get(band.getName());
        if(interval == null){
            throw new IncompatibleWorldException("Band name unknown to Bidder Setup");
        }else{
            return rng.nextBigDecimal(interval);
        }
    }

    /**
     * The parameter defining the intra-band synergies the bidder has from having
     * multiple licenses in the same band. 
     * @param band
     * @param rng
     * @return
     */
    public  Map<Integer, BigDecimal> drawSynergyFactors(BMBand band, UniformDistributionRNG rng){
        Map<Integer, BigDecimal> bandSynergies = synFactors.get(band.getName());
        if(bandSynergies == null){
            throw new IncompatibleWorldException("Band name unknown to Bidder Setup ");
        }else{
            return Collections.unmodifiableMap(bandSynergies);
        }
    }

 
    
    
    public abstract static class BMBidderSetupBuilder extends BidderSetup.Builder{
        protected Map<String, IntegerInterval> positiveValueThresholdIntervals;
        protected Map<String, DoubleInterval> baseValueIntervals;
        protected Map<String, Map<Integer, BigDecimal>> synFactors;
        
        /**
         * 
         * @param setupName the name of this bidder type
         * @param numberOfBidders As there is no default value specified by Bichler et al. (2013) for the Base Models, 
         *      no default value is stored here
         */
        protected BMBidderSetupBuilder(String setupName, int numberOfBidders){
            super(setupName, numberOfBidders);
            positiveValueThresholdIntervals = new HashMap<String, IntegerInterval>();
            baseValueIntervals = new HashMap<>();
            synFactors = new HashMap<String, Map<Integer,BigDecimal>>();
        }
        
        //Protected as MBVM does not have thresholds, but calculation still requests param for both BVM and MBVM, 
        //as no distinction is made value calculation code
        /**
         * See {@link BMBidderSetup#drawPositiveValueThreshold(BMBand, UniformDistributionRNG)} for the explanation of this parameter.
         */
        protected void putValueThresholdInterval(String bandName, IntegerInterval interval){
            PreconditionUtils.checkNotNull(bandName, interval);
            Preconditions.checkArgument(interval.isNonNegative());
            positiveValueThresholdIntervals.put(bandName, interval);
        }
        
        //Protected as MBVM does not have thresholds, but calculation still requests param for both BVM and MBVM, 
        //as no distinction is made value calculation code
        /**
         * See {@link BMBidderSetup#drawPositiveValueThreshold(BMBand, UniformDistributionRNG)} for the explanation of this parameter.
         */
        protected IntegerInterval removeValueThresholdInterval(String bandName){
            return positiveValueThresholdIntervals.remove(bandName);
        }
        
        /**
         * See {@link BMBidderSetup#drawBaseValue(BMBand, UniformDistributionRNG)} for the explanation of this parameter.
         */
        public void putBaseValueInterval(String bandName, DoubleInterval interval){
            PreconditionUtils.checkNotNull(bandName, interval);
            Preconditions.checkArgument(interval.isNonNegative());
            baseValueIntervals.put(bandName, interval);
        }
        
        /**
         * See {@link BMBidderSetup#drawBaseValue(BMBand, UniformDistributionRNG)} for the explanation of this parameter.
         */
        public DoubleInterval removeBaseValueInterval(String bandName){
            return baseValueIntervals.remove(bandName);
        }
        
        /**
         * See {@link BMBidderSetup#drawSynergyFactors(BMBand, UniformDistributionRNG)} for the explanation of this parameter.
         * 
         */
        public void putSynergyFactors(String bandName, Map<Integer, BigDecimal> synergies){
            PreconditionUtils.checkNotNull(bandName, synergies);
            synFactors.put(bandName, synergies);
        }
        
        /**
         * See {@link BMBidderSetup#drawSynergyFactors(BMBand, UniformDistributionRNG)} for the explanation of this parameter.
         */
        public Map<Integer, BigDecimal>  removeSynergyFactor(String bandName){
            return synFactors.remove(bandName);
        }
        
        /**
         * See {@link BMBidderSetup#drawPositiveValueThreshold(BMBand, UniformDistributionRNG)} for the explanation of this parameter.
         */
        public Map<String, IntegerInterval> getPositiveValueThresholdIntervals() {
            return Collections.unmodifiableMap(positiveValueThresholdIntervals);
        }

        /**
         * See {@link BMBidderSetup#drawBaseValue(BMBand, UniformDistributionRNG)} for the explanation of this parameter.
         */
        public Map<String, DoubleInterval> getBaseValueIntervals() {
            return Collections.unmodifiableMap(baseValueIntervals);
        }

        /**
         * See {@link BMBidderSetup#drawSynergyFactors(BMBand, UniformDistributionRNG)} for the explanation of this parameter.
         */
        public Map<String, Map<Integer, BigDecimal>> getSynFactors() {
            Map<String, Map<Integer, BigDecimal>> synFactors = new HashMap<String, Map<Integer,BigDecimal>>();
            for(Entry<String, Map<Integer, BigDecimal>> entry : synFactors.entrySet()){
                synFactors.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
            }
            return Collections.unmodifiableMap(synFactors);
        }

 
        @Override
        public abstract BMBidderSetup build();


    }
    
}

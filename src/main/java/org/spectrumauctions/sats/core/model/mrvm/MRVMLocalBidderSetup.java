/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.util.*;

/**
 * @author Michael Weiss
 *
 */
public class MRVMLocalBidderSetup extends MRVMBidderSetup {

    private final IntegerInterval numberOfRegionsInterval;
    private final List<String> regionNotes;

    /**
     * @param builder
     */
    protected MRVMLocalBidderSetup(Builder builder) {
        super(builder);
        this.numberOfRegionsInterval = builder.numberOfRegionsInterval;
        this.regionNotes = builder.regionNotes;
    }


    /**
     * Randomly selects regions of interest.
     * The quantity of selected regions is also chosen randomly from a specifiable interval.
     * @param world
     * @param rng
     * @return
     */
    public Set<MRVMRegionsMap.Region> drawRegionsOfInterest(MRVMWorld world, UniformDistributionRNG rng) {
        if (regionNotes == null) {
            return drawRegionsOfInterestRandomly(world, rng);
        } else {
            return pickPredeterminedRegions(world);
        }

    }

    private Set<MRVMRegionsMap.Region> pickPredeterminedRegions(MRVMWorld world) {
        Set<MRVMRegionsMap.Region> result = new HashSet<>();
        Preconditions.checkNotNull(regionNotes, "This method must only be called if there are predefined regions");
        Preconditions.checkArgument(!regionNotes.isEmpty(), "This message must only be called if there are predefined regions");
        for (String desiredRegionNote : regionNotes) {
            for (MRVMRegionsMap.Region region : world.getRegionsMap().getRegions()) {
                if (region.getNote().equals(desiredRegionNote)) {
                    result.add(region);
                }
            }
        }

        return result;
    }

    private Set<MRVMRegionsMap.Region> drawRegionsOfInterestRandomly(MRVMWorld world, UniformDistributionRNG rng) {
        int numberOfRegions = rng.nextInt(numberOfRegionsInterval);
        if (numberOfRegions > world.getRegionsMap().getRegions().size()) {
            numberOfRegions = world.getRegionsMap().getRegions().size();
        }
        List<MRVMRegionsMap.Region> regions = new ArrayList<>(world.getRegionsMap().getRegions());
        Collections.shuffle(regions, new Random(rng.nextLong()));
        regions = regions.subList(0, numberOfRegions);
        return new HashSet<>(regions);
    }

    public static class Builder extends MRVMBidderSetup.Builder {

        private IntegerInterval numberOfRegionsInterval;
        private List<String> regionNotes;

        /**
         * @param alphaInterval
         * @param betaInterval
         */
        public Builder() {
            super("Multi Region Model Local Bidder",
                    3,
                    new DoubleInterval(200, 400),
                    new DoubleInterval(0.05, 0.15));
            this.numberOfRegionsInterval = new IntegerInterval(3, 7);
        }

        /**
         * Deterministically specify the regions in which the bidder is interested.<br>
         * If this value is null, the regions will be drawn uniformly at random, as specified in {@link #setNumberOfRegionsInterval(IntegerInterval)}
         * @param regionNames
         */
        public void setPredefinedRegionsOfInterest(List<String> regionNotes) {
            Preconditions.checkArgument(regionNotes == null || !regionNotes.isEmpty(), "List of RegionNames must not be empty");
            this.regionNotes = regionNotes;
        }


        /**
         * An interval used to determine the number of regions the bidder is interested in, see {@link MRVMLocalBidderSetup#drawRegionsOfInterest(MRVMWorld, UniformDistributionRNG)}
         * If the parameter is set, regions of interest are randomly drawn,
         * and the predefined regions of interest are set to null (i.e., {@link #setPredefinedRegionsOfInterest(List)} is called with parameter null).
         */
        public void setNumberOfRegionsInterval(IntegerInterval numberOfRegionsInterval) {
            this.numberOfRegionsInterval = numberOfRegionsInterval;
            setPredefinedRegionsOfInterest(null);
        }


        /* (non-Javadoc)
         * @see MRVMBidderSetup.Builder#build()
         */
        @Override
        public MRVMLocalBidderSetup build() {
            return new MRVMLocalBidderSetup(this);
        }


    }


}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.mrm;

import edu.harvard.econcs.jopt.solver.IMIPResult;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.mrm.MRMBand;
import org.spectrumauctions.sats.core.model.mrm.MRMBidder;
import org.spectrumauctions.sats.core.model.mrm.MRMGenericDefinition;
import org.spectrumauctions.sats.core.model.mrm.MRMRegionsMap.Region;
import org.spectrumauctions.sats.core.model.mrm.MRMWorld;
import org.spectrumauctions.sats.opt.model.GenericAllocation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author Michael Weiss
 *
 */
public final class MipResult extends GenericAllocation<MRMGenericDefinition> {


    private final MRMWorld world;
    private final BigDecimal totalValue;
    private final IMIPResult joptResult;

    private MipResult(Builder builder) {
        super(builder);
        this.world = builder.world;
        this.totalValue = BigDecimal.valueOf(builder.objectiveValue);
        this.joptResult = builder.joptResult;
    }

    @Override
    public BigDecimal getTotalValue() {
        return totalValue;
    }


    public IMIPResult getJoptResult() {
        return joptResult;
    }


    public MRMWorld getWorld() {
        return world;
    }

    public String toString() {
        String tab = "\t";
        StringBuilder builder = new StringBuilder();

        List<Entry<Bidder<?>, GenericValue<MRMGenericDefinition>>> sortedEntries = new ArrayList<>(values.entrySet());
        Collections.sort(sortedEntries, (e1, e2) -> ((Long) e1.getKey().getId()).compareTo((Long) e2.getKey().getId()));


        builder.append("===== bidder listing =======").append(System.lineSeparator());
        for (Entry<Bidder<?>, GenericValue<MRMGenericDefinition>> entry : sortedEntries) {
            MRMBidder bidder = (MRMBidder) entry.getKey();

            builder.append(entry.getKey().getId())
                    .append(tab)
                    .append(entry.getKey().getClass().getSimpleName())
                    .append("(")
                    .append(bidder.getSetupType())
                    .append(")")
                    .append(tab)
                    .append(entry.getValue().getValue().toString())
                    .append(" #licenses:")
                    .append(entry.getValue().getTotalQuantity())
                    .append(System.lineSeparator());
        }
        builder.append("===== allocation table =======").append(System.lineSeparator());

        if (!values.isEmpty()) {
            MRMWorld world = (MRMWorld) values.keySet().iterator().next().getWorld();
            List<MRMBand> orderedBands = new ArrayList<>(world.getBands());
            //Order bands by increasing name length
            Collections.sort(orderedBands, (b1, b2) -> ((Integer) b1.getName().length()).compareTo((Integer) b2.getName().length()));
            for (MRMBand band : orderedBands) {
                builder.append(tab).append(band.getName());
            }
            builder.append(System.lineSeparator());
            for (Region region : world.getRegionsMap().getRegions()) {
                //Print region information
                builder.append("rid")
                        .append(region.getId())
                        .append(tab)
                        .append("(")
                        .append(region.getNote())
                        .append(", population: ")
                        .append(region.getPopulation())
                        .append(")")
                        .append(System.lineSeparator());
                //Print allocation in reguin
                for (Entry<Bidder<?>, GenericValue<MRMGenericDefinition>> entry : sortedEntries) {
                    builder.append(tab);
                    for (MRMBand band : orderedBands) {
                        MRMGenericDefinition def = new MRMGenericDefinition(band, region);
                        int quantity = entry.getValue().getQuantity(def);
//                        builder.append(entry.getKey().getId())
//                        .append(":")
                        builder.append(quantity)
                                .append(tab)
                                .append(tab)
                                .append(tab);
                    }
                    MRMBidder bidder = (MRMBidder) entry.getKey();
                    builder.append(entry.getKey().getClass().getSimpleName())
                            .append(entry.getKey().getId())
                            .append(" (")
                            .append(bidder.getSetupType())
                            .append(")");
                    builder.append(System.lineSeparator());
                }
                builder.append("----------------newregion----------------")
                        .append(System.lineSeparator());
            }
        }
        return builder.toString();
    }


    public static final class Builder extends GenericAllocation.Builder<MRMGenericDefinition> {

        private MRMWorld world;
        private double objectiveValue;
        private final IMIPResult joptResult;

        /**
         *
         * @param objectiveValue
         * @param world
         * @param joptResult The result object //TODO Use Result object here in construction to build MipResult
         */
        public Builder(double objectiveValue, MRMWorld world, IMIPResult joptResult) {
            super();
            this.objectiveValue = objectiveValue;
            this.world = world;
            this.joptResult = joptResult;
        }

        public MipResult build() {
            return new MipResult(this);
        }
    }

}

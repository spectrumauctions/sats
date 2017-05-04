/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.srvm;

import edu.harvard.econcs.jopt.solver.IMIPResult;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.srvm.SRVMBand;
import org.spectrumauctions.sats.core.model.srvm.SRVMBidder;
import org.spectrumauctions.sats.core.model.srvm.SRVMWorld;
import org.spectrumauctions.sats.opt.model.GenericAllocation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author Michael Weiss
 */
public final class SRVMMipResult extends GenericAllocation<SRVMBand> {


    private final SRVMWorld world;
    private final BigDecimal totalValue;
    private final IMIPResult joptResult;

    private SRVMMipResult(Builder builder) {
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


    public SRVMWorld getWorld() {
        return world;
    }

    public String toString() {
        String tab = "\t";
        StringBuilder builder = new StringBuilder();

        List<Entry<Bidder<?>, GenericValue<SRVMBand>>> sortedEntries = new ArrayList<>(values.entrySet());
        Collections.sort(sortedEntries, (e1, e2) -> ((Long) e1.getKey().getId()).compareTo((Long) e2.getKey().getId()));


        builder.append("===== bidder listing =======").append(System.lineSeparator());
        for (Entry<Bidder<?>, GenericValue<SRVMBand>> entry : sortedEntries) {
            SRVMBidder bidder = (SRVMBidder) entry.getKey();

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
            SRVMWorld world = (SRVMWorld) values.keySet().iterator().next().getWorld();
            List<SRVMBand> orderedBands = new ArrayList<>(world.getBands());
            //Order bands by increasing name length
            Collections.sort(orderedBands, (b1, b2) -> ((Integer) b1.getName().length()).compareTo((Integer) b2.getName().length()));
            for (SRVMBand band : orderedBands) {
                builder.append(tab).append(band.getName());
            }
            builder.append(System.lineSeparator());
            //Print allocation in reguin
            for (Entry<Bidder<?>, GenericValue<SRVMBand>> entry : sortedEntries) {
                builder.append(tab);
                for (SRVMBand band : orderedBands) {
                    int quantity = entry.getValue().getQuantity(band);
//                        builder.append(entry.getKey().getId())
//                        .append(":")
                    builder.append(quantity)
                            .append(tab)
                            .append(tab)
                            .append(tab);
                }
                SRVMBidder bidder = (SRVMBidder) entry.getKey();
                builder.append(entry.getKey().getClass().getSimpleName())
                        .append(entry.getKey().getId())
                        .append(" (")
                        .append(bidder.getSetupType())
                        .append(")");
                builder.append(System.lineSeparator());
            }
        }
        return builder.toString();
    }


    public static final class Builder extends GenericAllocation.Builder<SRVMBand> {

        private SRVMWorld world;
        private double objectiveValue;
        private final IMIPResult joptResult;

        /**
         * @param objectiveValue
         * @param world
         * @param joptResult     The result object //TODO Use Result object here in construction to build MipResult
         */
        public Builder(double objectiveValue, SRVMWorld world, IMIPResult joptResult) {
            super();
            this.objectiveValue = objectiveValue;
            this.world = world;
            this.joptResult = joptResult;
        }

        public SRVMMipResult build() {
            return new SRVMMipResult(this);
        }
    }

}

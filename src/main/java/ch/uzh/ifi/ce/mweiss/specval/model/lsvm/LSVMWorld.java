package ch.uzh.ifi.ce.mweiss.specval.model.lsvm;

import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.World;
import ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier;
import ch.uzh.ifi.ce.mweiss.specval.util.random.UniformDistributionRNG;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Fabio Isler
 *
 */
public final class LSVMWorld extends World {

    private static final long serialVersionUID = 1737956689715986936L;
    private static final String MODEL_NAME = "Local Synergy Value Model";
    private final LSVMGrid grid;

    public LSVMWorld(LSVMWorldSetup worldSetup, RNGSupplier rngSupplier) {
        super(MODEL_NAME);
        UniformDistributionRNG uniformDistributionRNG = rngSupplier.getUniformDistributionRNG();
        this.grid = new LSVMGrid(this, worldSetup, uniformDistributionRNG);
        store();
    }


    public LSVMGrid getGrid() {
        return grid;
    }

    @Override
    public Collection<? extends Bidder<LSVMLicense>> restorePopulation(long populationId) {
        return super.restorePopulation(LSVMBidder.class, populationId);
    }

    /**
     * {@inheritDoc}
     * @return An immutable set containing all licenses.
     */
    @Override
    public ImmutableSet<LSVMLicense> getLicenses() {
        return grid.getLicenses();
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.World#getNumberOfGoods()
     */
    @Override
    public int getNumberOfGoods() {
        return grid.getLicenses().size();
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.World#refreshFieldBackReferences()
     */
    @Override
    public void refreshFieldBackReferences() {
        grid.refreshFieldBackReferences(this);
    }

    public List<LSVMBidder> createPopulation(List<LSVMBidderSetup> setups, RNGSupplier populationRNG) {
        long population = openNewPopulation();
        long currentId = 0;
        List<LSVMBidder> bidders = new ArrayList<>();
        for (LSVMBidderSetup setup : setups) {
            for (int i = 0; i < setup.getNumberOfBidders(); i++) {
                bidders.add(new LSVMBidder(setup, this, currentId++, population, populationRNG));
            }
        }
        return bidders;
    }
}

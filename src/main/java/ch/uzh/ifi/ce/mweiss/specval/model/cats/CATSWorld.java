package ch.uzh.ifi.ce.mweiss.specval.model.cats;

import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.World;
import ch.uzh.ifi.ce.mweiss.specval.model.cats.graphalgorithms.Graph;
import ch.uzh.ifi.ce.mweiss.specval.model.cats.graphalgorithms.Vertex;
import ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier;
import com.google.common.collect.ImmutableSet;

import java.util.*;

/**
 * @author Fabio Isler
 */
public final class CATSWorld extends World {
    private static final long serialVersionUID = 1794771578755986936L;
    private static final String MODEL_NAME = "CATS Region Value Model";
    private final double additivity;
    private final boolean useQuadraticPricingOption;
    private final Graph grid;
    private final int size;
    private final Set<CATSLicense> licenses;

    private transient ImmutableSet<CATSLicense> licenseSet;


    public CATSWorld(CATSWorldSetup worldSetup, RNGSupplier rngSupplier) {
        super(MODEL_NAME);
        int numberOfRows = worldSetup.drawNumberOfRows(rngSupplier);
        int numberOfColumns = worldSetup.drawNumberOfColumns(rngSupplier);
        this.grid = worldSetup.buildProximityGraph(numberOfRows, numberOfColumns, rngSupplier);
        this.licenses = new HashSet<>();
        for (Vertex vertex : this.grid.getVertices()) {
            licenses.add(new CATSLicense(vertex, worldSetup.drawCommonValue(rngSupplier), this));
        }
        this.size = this.grid.getVertices().size();
        this.additivity = worldSetup.getAdditivity();
        this.useQuadraticPricingOption = worldSetup.useQuadraticPricingOption();
        store();
    }

    @Override
    public Collection<? extends Bidder<CATSLicense>> restorePopulation(long populationId) {
        return super.restorePopulation(CATSBidder.class, populationId);
    }

    /**
     * {@inheritDoc}
     *
     * @return An immutable set containing all licenses.
     */
    @Override
    public ImmutableSet<CATSLicense> getLicenses() {
        if (licenseSet == null) {
            licenseSet = ImmutableSet.copyOf(licenses);
        }
        return licenseSet;
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.World#getNumberOfGoods()
     */
    @Override
    public int getNumberOfGoods() {
        return getLicenses().size();
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.World#refreshFieldBackReferences()
     */
    @Override
    public void refreshFieldBackReferences() {
        for (CATSLicense license : licenses) {
            license.refreshFieldBackReferences(this);
        }
    }

    public List<CATSBidder> createPopulation(List<CATSBidderSetup> setups, RNGSupplier populationRNG) {
        long population = openNewPopulation();
        long currentId = 0;
        List<CATSBidder> bidders = new ArrayList<>();
        for (CATSBidderSetup setup : setups) {
            for (int i = 0; i < setup.getNumberOfBidders(); i++) {
                bidders.add(new CATSBidder(setup, this, currentId++, population, populationRNG));
            }
        }
        return bidders;
    }

    public double getAdditivity() {
        return additivity;
    }

    public int getSize() {
        return size;
    }

    public Graph getGrid() {
        return grid;
    }

    public boolean getUseQuadraticPricingOption() {
        return useQuadraticPricingOption;
    }
}

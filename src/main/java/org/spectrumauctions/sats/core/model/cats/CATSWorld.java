package org.spectrumauctions.sats.core.model.cats;

import com.google.common.collect.ImmutableSet;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.model.cats.graphalgorithms.Graph;
import org.spectrumauctions.sats.core.model.cats.graphalgorithms.Mesh2D;
import org.spectrumauctions.sats.core.model.cats.graphalgorithms.Vertex;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.util.*;

/**
 * @author Fabio Isler
 */
public final class CATSWorld extends World {
    private static final long serialVersionUID = 1794771578755986936L;
    private static final String MODEL_NAME = "CATS Region Value Model";
    private final double additivity;
    private final double additionalLocation;
    private double budgetFactor;
    private double resaleFactor;
    private double jumpProbability;
    private double deviation;
    private final boolean useQuadraticPricingOption;
    private final Mesh2D grid;
    private final int size;
    private final HashSet<CATSLicense> licenses;

    private transient ImmutableSet<CATSLicense> licenseSet;


    public CATSWorld(CATSWorldSetup worldSetup, RNGSupplier rngSupplier) {
        super(MODEL_NAME);
        int numberOfRows, numberOfColumns;
        if (worldSetup.hasDefinedNumberOfGoodsInterval()) {
            int numberOfGoods = worldSetup.drawNumberOfGoods(rngSupplier);
            numberOfRows = (int) Math.floor(Math.sqrt(numberOfGoods));
            numberOfColumns = (int) Math.floor(Math.sqrt(numberOfGoods));
        } else {
            numberOfRows = worldSetup.drawNumberOfRows(rngSupplier);
            numberOfColumns = worldSetup.drawNumberOfColumns(rngSupplier);
        }
        this.grid = worldSetup.buildProximityGraph(numberOfRows, numberOfColumns, rngSupplier);
        this.licenses = new HashSet<>();
        for (Vertex vertex : this.grid.getVertices()) {
            licenses.add(new CATSLicense(vertex, worldSetup.drawCommonValue(rngSupplier), this));
        }
        this.size = this.grid.getVertices().size();
        this.additivity = worldSetup.getAdditivity();
        this.additionalLocation = worldSetup.getAdditionalLocation();
        this.budgetFactor = worldSetup.getBudgetFactor();
        this.resaleFactor = worldSetup.getResaleFactor();
        this.jumpProbability = worldSetup.getJumpProbability();
        this.deviation = worldSetup.getDeviation();
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

    /**
     * @see World#getNumberOfGoods()
     */
    @Override
    public int getNumberOfGoods() {
        return getLicenses().size();
    }

    /**
     * @see World#refreshFieldBackReferences()
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

    public double getAdditionalLocation() {
        return additionalLocation;
    }

    public double getBudgetFactor() {
        return budgetFactor;
    }

    public double getResaleFactor() {
        return resaleFactor;
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

    public double getJumpProbability() {
        return jumpProbability;
    }

    public double getDeviation() {
        return deviation;
    }
}

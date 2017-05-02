package ch.uzh.ifi.ce.mweiss.specval.model.lsvm;

import ch.uzh.ifi.ce.mweiss.specval.model.Bundle;
import ch.uzh.ifi.ce.mweiss.specval.model.World;
import ch.uzh.ifi.ce.mweiss.specval.util.PreconditionUtils;
import ch.uzh.ifi.ce.mweiss.specval.util.random.UniformDistributionRNG;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabio Isler
 */
public class LSVMGrid implements Serializable {

    private static final long serialVersionUID = -3752536748200949347L;
    private final LSVMLicense[][] licenses;
    private final long worldId;
    private final Integer numberOfRows;
    private final Integer numberOfColumns;
    private transient LSVMWorld world;
    private transient ImmutableSet<LSVMLicense> licenseSet = null;

    public LSVMGrid(LSVMWorld world, LSVMWorldSetup worldSetup, UniformDistributionRNG rng) {
        this.world = world;
        this.worldId = world.getId();
        this.numberOfRows = worldSetup.drawRowNumber(rng);
        this.numberOfColumns = worldSetup.drawColumnNumber(rng);
        PreconditionUtils.checkNotNegative(this.numberOfRows);
        PreconditionUtils.checkNotNegative(this.numberOfColumns);

        // Initialize licenses
        this.licenses = new LSVMLicense[this.numberOfRows][this.numberOfColumns];
        int id = 0;
        for (int i = 0; i < this.numberOfRows; i++) {
            for (int j = 0; j < this.numberOfColumns; j++) {
                this.licenses[i][j] = new LSVMLicense(id++, i, j, world);
            }
        }
    }

    public ImmutableSet<LSVMLicense> getLicenses() {
        if (licenseSet == null) {
            ImmutableSet.Builder<LSVMLicense> builder = ImmutableSet.builder();
            for (int i = 0; i < this.numberOfRows; i++) {
                for (int j = 0; j < this.numberOfColumns; j++) {
                    builder.add(licenses[i][j]);
                }
            }
            licenseSet = builder.build();
        }
        return licenseSet;
    }

    /**
     * see {@link World#refreshFieldBackReferences()} for javadoc
     *
     */
    void refreshFieldBackReferences(LSVMWorld world) {
        Preconditions.checkArgument(world.getId() == this.worldId);
        this.world = world;
        for (int i = 0; i < numberOfRows; i++) {
            for (LSVMLicense license : licenses[i]) {
                license.refreshFieldBackReferences(this);
            }
        }
    }

    public LSVMWorld getWorld() {
        return world;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    LSVMLicense getLicense(int row, int column) {
        PreconditionUtils.checkNotNegative(row);
        PreconditionUtils.checkNotNegative(column);
        Preconditions.checkArgument(row < numberOfRows);
        Preconditions.checkArgument(column < numberOfColumns);
        return licenses[row][column];
    }

    Set<LSVMLicense> getProximity(LSVMLicense center, int proximitySize) {
        Set<LSVMLicense> proximity = new HashSet<>();
        proximity.add(center);
        Set<LSVMLicense> tmp = expand(proximity, center);
        // Expand step by step
        for (int i = 0; i < proximitySize; i++) {
            proximity.addAll(tmp);
            Set<LSVMLicense> tmp2 = new HashSet<>();
            for (LSVMLicense license : tmp) {
                tmp2.addAll(expand(proximity, license));
            }
            tmp = tmp2;
        }
        return proximity;
    }

    private Set<LSVMLicense> expand(Set<LSVMLicense> proximity, LSVMLicense license) {

        Set<LSVMLicense> newLicenses = new HashSet<>();
        // Add top neighbor if not in first row
        if (license.getRowPosition() > 0) {
            // Only add if not added yet
            if (!proximity.contains(getLicense(license.getRowPosition() - 1, license.getColumnPosition()))) {
                newLicenses.add(getLicense(license.getRowPosition() - 1, license.getColumnPosition()));
            }
        }

        // Add right neighbor if not in last column
        if (license.getColumnPosition() < getNumberOfColumns() - 1) {
            // Only add if not added yet
            if (!proximity.contains(getLicense(license.getRowPosition(), license.getColumnPosition() + 1))) {
                newLicenses.add(getLicense(license.getRowPosition(), license.getColumnPosition() + 1));
            }
        }

        // Add lower neighbor if not in last row
        if (license.getRowPosition() < getNumberOfRows() - 1) {
            // Only add if not added yet
            if (!proximity.contains(getLicense(license.getRowPosition() + 1, license.getColumnPosition()))) {
                newLicenses.add(getLicense(license.getRowPosition() + 1, license.getColumnPosition()));
            }
        }

        // Add left neighbor if not in first column
        if (license.getColumnPosition() > 0) {
            // Only add if not added yet
            if (!proximity.contains(getLicense(license.getRowPosition(), license.getColumnPosition() - 1))) {
                newLicenses.add(getLicense(license.getRowPosition(), license.getColumnPosition() - 1));
            }
        }

        return newLicenses;
    }

    private boolean isNeighbor(LSVMLicense a, LSVMLicense b) {
        return a.getRowPosition() == b.getRowPosition() + 1 && a.getColumnPosition() == b.getColumnPosition()
                || a.getRowPosition() == b.getRowPosition() - 1 && a.getColumnPosition() == b.getColumnPosition()
                || a.getRowPosition() == b.getRowPosition() && a.getColumnPosition() + 1 == b.getColumnPosition()
                || a.getRowPosition() == b.getRowPosition() && a.getColumnPosition() - 1 == b.getColumnPosition();
    }

    private boolean hasNeighbor(LSVMLicense a, Set<LSVMLicense> set) {
        for (LSVMLicense b : set) {
            if (isNeighbor(a, b)) return true;
        }
        return false;
    }

    Set<Set<LSVMLicense>> getMaximallyConnectedSubpackages(Bundle<LSVMLicense> bundle) {

        Set<Set<LSVMLicense>> subpackages = new HashSet<>();
        Bundle<LSVMLicense> copyOfBundle = new Bundle<>();
        copyOfBundle.addAll(bundle);
        while (!copyOfBundle.isEmpty()) {
            // Each iteration returns one subpackage
            LSVMLicense next = copyOfBundle.iterator().next();
            Set<LSVMLicense> currentSet = new HashSet<>();
            currentSet.add(next);
            Set<LSVMLicense> subpackage = addNeighbors(currentSet, copyOfBundle);
            copyOfBundle.removeAll(subpackage);
            subpackages.add(subpackage);
        }
        return subpackages;
    }

    private Set<LSVMLicense> addNeighbors(Set<LSVMLicense> currentSet, Bundle<LSVMLicense> possibleNeighbors) {
        if (currentSet.size() == possibleNeighbors.size()) return currentSet;
        else {
            Set<LSVMLicense> linkedLicenses = new HashSet<>();
            linkedLicenses.addAll(currentSet);
            Bundle<LSVMLicense> unassigned = new Bundle<>(possibleNeighbors);
            unassigned.removeAll(currentSet);

            for (LSVMLicense license : unassigned) {
                if (hasNeighbor(license, currentSet)) {
                    linkedLicenses.add(license);
                    linkedLicenses.addAll(addNeighbors(linkedLicenses, possibleNeighbors));
                }
            }

            return linkedLicenses;

        }

    }
}

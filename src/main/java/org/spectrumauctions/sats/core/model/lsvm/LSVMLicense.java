package org.spectrumauctions.sats.core.model.lsvm;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.World;

/**
 * @author Fabio Isler
 */
public class LSVMLicense extends Good {

    private static final long serialVersionUID = 2814831251672586420L;
    private final int rowPosition;
    private final int columnPosition;
    private transient LSVMWorld world;

    protected LSVMLicense(long id, int row, int column, LSVMWorld world) {
        super(id, world.getId());
        this.rowPosition = row;
        this.columnPosition = column;
        this.world = world;
    }

    public int getRowPosition() {
        return rowPosition;
    }

    public int getColumnPosition() {
        return columnPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LSVMLicense license = (LSVMLicense) o;

        if (rowPosition != license.rowPosition) return false;
        //noinspection SimplifiableIfStatement
        if (columnPosition != license.columnPosition) return false;
        return world.equals(license.world);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (world != null ? world.hashCode() : 0);
        return result;
    }

    /* (non-Javadoc)
     * @see Good#getWorld()
     */
    @Override
    public LSVMWorld getWorld() {
        return world;
    }

    @Override
    public long getWorldId() {
        return worldId;
    }

    /**
     * Method is called after deserialization, there is not need to call it on any other occasion.<br>
     * See {@link World#refreshFieldBackReferences()} for explanations.
     */
    public void refreshFieldBackReferences(LSVMGrid grid) {
        Preconditions.checkArgument(grid.getWorld().getId() == this.worldId);
        this.world = grid.getWorld();
    }


}

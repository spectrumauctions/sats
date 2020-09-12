package org.spectrumauctions.sats.core.model.gsvm;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.model.License;
import org.spectrumauctions.sats.core.model.World;

import java.util.Objects;

/**
 * @author Fabio Isler
 */
public class GSVMLicense extends License {

    private static final long serialVersionUID = 5732211251672586420L;
    private final int position;
    private transient GSVMWorld world;


    GSVMLicense(long id, int position, GSVMWorld world) {
        super(id, world.getId());
        this.position = position;
        this.world = world;
    }

    /* (non-Javadoc)
    * @see License#getWorld()
    */
    @Override
    public GSVMWorld getWorld() {
        return world;
    }

    /**
     * Method is called after deserialization, there is not need to call it on any other occasion.<br>
     * See {@link World#refreshFieldBackReferences()} for explanations.
     */
    public void refreshFieldBackReferences(GSVMCircle circle) {
        Preconditions.checkArgument(circle.getWorld().getId() == this.worldId);
        this.world = circle.getWorld();
        // TODO: Refresh circle?
        // Preconditions.checkArgument(circle.getLongId() == this.circleId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GSVMLicense that = (GSVMLicense) o;
        return position == that.position &&
                Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), position, world);
    }

    public int getPosition() {
        return position;
    }
}

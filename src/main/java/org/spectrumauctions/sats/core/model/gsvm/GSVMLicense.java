package org.spectrumauctions.sats.core.model.gsvm;

import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.World;
import com.google.common.base.Preconditions;

/**
 * @author Fabio Isler
 */
public class GSVMLicense extends Good {

    private static final long serialVersionUID = 5732211251672586420L;
    private final int position;
    private transient GSVMWorld world;


    GSVMLicense(long id, int position, GSVMWorld world) {
        super(id, world.getId());
        this.position = position;
        this.world = world;
    }

    /* (non-Javadoc)
    * @see Good#getWorld()
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
        // Preconditions.checkArgument(circle.getId() == this.circleId);
    }

    public int getPosition() {
        return position;
    }
}

package ch.uzh.ifi.ce.mweiss.specval.model.gsvm;

import ch.uzh.ifi.ce.mweiss.specval.model.World;
import ch.uzh.ifi.ce.mweiss.specval.util.PreconditionUtils;
import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Fabio Isler
 */
public class GSVMCircle implements Serializable {

    private static final long serialVersionUID = -3752536748200949347L;
    private final GSVMLicense[] licenses;
    private final long worldId;

    private final int size;

    private transient GSVMWorld world;

    GSVMCircle(GSVMWorld world, int size, int startId) {
        this.world = world;
        this.worldId = world.getId();
        this.size = size;
        PreconditionUtils.checkNotNegative(this.size);

        // Initialize licenses
        this.licenses = new GSVMLicense[this.size];
        int id = startId;
        for (int i = 0; i < this.size; i++) {
            this.licenses[i] = new GSVMLicense(id++, i, world);
        }
    }

    public int getSize() {
        return size;
    }

    public GSVMWorld getWorld() {
        return world;
    }


    /**
     * see {@link World#refreshFieldBackReferences()} for javadoc
     */
    void refreshFieldBackReferences(GSVMWorld world) {
        Preconditions.checkArgument(world.getId() == this.worldId);
        this.world = world;
        for (GSVMLicense license : licenses) {
            license.refreshFieldBackReferences(this);
        }
    }


    public GSVMLicense[] getLicenses() {
        return Arrays.copyOf(licenses, licenses.length);
    }
}

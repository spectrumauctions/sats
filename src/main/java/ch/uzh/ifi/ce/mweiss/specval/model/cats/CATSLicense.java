package ch.uzh.ifi.ce.mweiss.specval.model.cats;

import ch.uzh.ifi.ce.mweiss.specval.model.Good;
import ch.uzh.ifi.ce.mweiss.specval.model.World;
import ch.uzh.ifi.ce.mweiss.specval.model.cats.graphalgorithms.Vertex;
import com.google.common.base.Preconditions;

/**
 * @author Fabio Isler
 */
public class CATSLicense extends Good {

    private static final long serialVersionUID = 5732211251618769420L;
    private final Vertex vertex;
    private final double commonValue;


    private transient CATSWorld world;

    CATSLicense(Vertex vertex, double commonValue, CATSWorld world) {
        super(vertex.getID(), world.getId());
        this.commonValue = commonValue;
        this.vertex = vertex;
        this.world = world;
    }

    public Vertex getVertex() {
        return vertex;
    }

    /* (non-Javadoc)
    * @see ch.uzh.ifi.ce.mweiss.specval.model.Good#getWorld()
    */
    @Override
    public CATSWorld getWorld() {
        return world;
    }

    public double getCommonValue() {
        return commonValue;
    }

    /**
     * Method is called after deserialization, there is not need to call it on any other occasion.<br>
     * See {@link World#refreshFieldBackReferences()} for explanations.
     */
    public void refreshFieldBackReferences(CATSWorld world) {
        Preconditions.checkArgument(world.getId() == this.worldId);
        this.world = world;
    }
}

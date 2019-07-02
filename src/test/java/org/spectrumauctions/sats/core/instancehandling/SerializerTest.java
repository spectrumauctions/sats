/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.instancehandling;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.spectrumauctions.sats.core.TestSuite;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.DefaultModel;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.instancehandling.InstanceHandler;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import javax.management.RuntimeOperationsException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Weiss
 *
 */
@RunWith(Parameterized.class)
public class SerializerTest {
    UniformDistributionRNG rng = new JavaUtilRNGSupplier(35435434343L).getUniformDistributionRNG();

    private final DefaultModel<?, ?> model;


    public SerializerTest(DefaultModel<?, ?> model) {
        super();
        this.model = model;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> models() {
        List<Object[]> testInput = new ArrayList<>();
        for (Object o : TestSuite.getAllModelAccessors()) {
            testInput.add(new Object[]{
                    o
            });
        }
        return testInput;
    }


    @Test
    public void deserializedWorldShouldBeEqual() {
        try {
            testWorldSerializability(model);
        } catch (RuntimeException e) {
            throw new RuntimeOperationsException(e, "Error during Serialization-Test of " + model.getClass().getSimpleName());
        }
    }

    public <W extends World, B extends SATSBidder<?>> void testWorldSerializability(DefaultModel<W, B> model) {
        World original = model.createWorld(rng.nextLong());
        World deserialized = InstanceHandler.getDefaultHandler().readWorld(original.getClass(), original.getId());
        Assert.assertEquals("Model " + model.getClass().getSimpleName() + " changed after deserialization", original, deserialized);
    }


    @Test
    public void deserializedBiddersShouldBeEqual() {
        try {
            testBidderSerializability(model);
        } catch (RuntimeException e) {
            throw new RuntimeOperationsException(e, "Error during Serialization-Test of " + model.getClass().getSimpleName() + " population : " + e.getMessage());
        }
    }

    public <W extends World, B extends SATSBidder<?>> void testBidderSerializability(DefaultModel<W, B> model) {
        W world = model.createWorld(rng.nextLong());
        Set<SATSBidder<?>> originalPopulation = new HashSet<>(model.createPopulation(world, rng.nextLong()));
        long populationId = originalPopulation.iterator().next().getPopulation();
        Set<? extends SATSBidder<?>> deserializedPopulation = new HashSet<>(world.restorePopulation(populationId));
        Assert.assertEquals(originalPopulation, deserializedPopulation);
    }

}

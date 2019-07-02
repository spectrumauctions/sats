/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Item Structure used to represent a bundle of items.
 * The items in this set are sorted by their id's when iterating over them.
 * It is checked that all items in a bundle are of the same {@link World}
 *
 * @author Michael Weiss
 *
 * @param <T>
 */
@EqualsAndHashCode(callSuper = true)
public class LicenseBundle<T extends License> extends TreeSet<T> {

    private static final long serialVersionUID = -821067248569898586L;

    // Is set with first added item
    protected World world;


    public LicenseBundle(Collection<T> allGoods) {
        this();
        addAll(allGoods);
    }

    @SafeVarargs
    public LicenseBundle(T... goods) {
        this(Arrays.asList(goods));
    }

    public LicenseBundle() {
        super(new License.IdComparator());
    }

    public String itemIds(String deliminator) {
        StringBuilder ids = new StringBuilder();
        boolean first = true;
        for (T good : this) {
            if (!first) {
                ids.append(deliminator);
            }
            first = false;
            ids.append(String.valueOf(good.getId()));
        }
        return ids.toString();
    }

    @Override
    public boolean add(T obj) {
        if (obj == null)
            return false;
        if (this.world == null) {
            this.world = obj.getWorld();
        } else if (!this.world.equals(obj.getWorld())) {
            throw new UnequalWorldsException();
        }
        return super.add(obj);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = false;
        for (T good : c) {
            result |= add(good);
        }
        return result;
    }


    public World getWorld() {
        return world;
    }

}

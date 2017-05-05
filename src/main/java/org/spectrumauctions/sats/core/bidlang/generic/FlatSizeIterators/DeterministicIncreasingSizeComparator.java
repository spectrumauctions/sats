/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;

import java.io.Serializable;
import java.util.*;

/**
 * @author Michael Weiss
 *
 */
public abstract class DeterministicIncreasingSizeComparator<T extends GenericDefinition> implements Comparator<Set<T>>, Serializable{

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Set<T> o1, Set<T> o2) {
        Preconditions.checkNotNull(o1);
        Preconditions.checkNotNull(o2);
        int difference = o1.size() - o2.size();
        if (difference != 0) {
            return difference;
        } else {
            return compareSameSize(o1, o2);
        }
    }

    private int compareSameSize(Set<T> o1, Set<T> o2) {
        Preconditions.checkArgument(o1.size() == o2.size());
        List<T> o1List = new ArrayList<>(o1);
        o1List.sort(getDefintionComparator());
        List<T> o2List = new ArrayList<>(o2);
        o2List.sort(getDefintionComparator());
        for (int i = 0; i < o1List.size(); i++) {
            int comparison = getDefintionComparator().compare(o1List.get(i), o2List.get(i));
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    protected abstract Comparator<T> getDefintionComparator();
}
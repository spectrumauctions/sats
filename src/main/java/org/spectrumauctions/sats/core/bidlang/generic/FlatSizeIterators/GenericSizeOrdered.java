/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators;

import com.google.common.collect.ImmutableSet;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.model.GenericGood;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 *
 * @author Michael Weiss
 *
 */
public abstract class GenericSizeOrdered implements BiddingLanguage {

    protected final Set<? extends GenericGood> allGoods;

    GenericSizeOrdered(Collection<? extends GenericGood> allPossibleGenericDefintions) throws UnsupportedBiddingLanguageException {
        allGoods = ImmutableSet.copyOf(allPossibleGenericDefintions);
        if (allGoods.size() > 6) {
            throw new UnsupportedBiddingLanguageException("Too many possible Generic Items in this world. "
                    + "Iterating size-based would not be reasonable");
        }

    }

    protected abstract Comparator<GenericGood> getDefComparator();

    protected DeterministicIncreasingSizeComparator getIncreasingSizeComparator() {
        return new DeterministicIncreasingSizeComparator() {

            @Override
            protected Comparator<GenericGood> getDefintionComparator() {
                return getDefComparator();
            }
        };
    }


}

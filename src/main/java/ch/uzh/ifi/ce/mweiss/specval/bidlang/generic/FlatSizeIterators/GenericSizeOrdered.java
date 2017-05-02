/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.FlatSizeIterators;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericDefinition;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericLang;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValueBidder;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;

/**
 * 
 * @author Michael Weiss
 *
 * @param <T>
 */
public abstract class GenericSizeOrdered<T extends GenericDefinition> implements GenericLang<T> {

    protected final Set<T> allDefintions;

    GenericSizeOrdered(Collection<T> allPossibleGenericDefintions) throws UnsupportedBiddingLanguageException{
        allDefintions = ImmutableSet.copyOf(allPossibleGenericDefintions);
        if(allDefintions.size() > 6){
            throw new UnsupportedBiddingLanguageException("Too many possible Generic Items in this world. "
                    + "Iterating size-based would not be reasonable");
        }
        
    }

    protected abstract GenericValueBidder<T> getGenericBidder();
    
    protected abstract Comparator<T> getDefComparator();
    
    protected DeterministicIncreasingSizeComparator<T> getIncreasingSizeComparator(){
        return new DeterministicIncreasingSizeComparator<T>() {

            @Override
            protected Comparator<T> getDefintionComparator() {
                return getDefComparator();
            }
        };
    }
    
    
   
    
    
    
    
   

}

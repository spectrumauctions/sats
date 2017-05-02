/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This data structure is basically a sorted container for the bidder types. It
 * may be extended for a specific model if there are correlations between the
 * different bidder types.
 * 
 * @author Michael Weiss
 *
 */
@Deprecated
public class PopulationStructure<T extends BidderCreationType> {

    private List<T> bidderTypes = new ArrayList<>();

    /**
     * Adds the bidder type to the end of the list. If there exists already a
     * bidder type with the same name, it is beeing removed The bidders are
     * created in the order their types appear in this list.
     * 
     * @param type
     */
    public void addBidderType(T type) {
        // Remove biddertype with same name, if any
        for (int i = 0; i < bidderTypes.size(); i++) {
            if (bidderTypes.get(i).name().equals(type.name())) {
                bidderTypes.remove(i);
                break;
            }
        }
        // Add new BidderType at the end of queue
        bidderTypes.add(type);
    }

    /**
     * The currently stored bidder types
     * 
     * @return Unmodifiable copy of the list, if you want edit list, use {@link #addBidderType(BidderCreationType)} or
     *         {@link #removeBidderType(String)}
     */
    public List<T> getBidderTypes() {
        return Collections.unmodifiableList(bidderTypes);
    }

    /**
     * Removes a biddertype with a given name, if exists
     * 
     * @param name
     */
    public void removeBidderType(String name) {
        for (int i = 0; i < bidderTypes.size(); i++) {
            if (bidderTypes.get(i).name().equals(name)) {
                bidderTypes.remove(i);
                break;
            }
        }
    }

}

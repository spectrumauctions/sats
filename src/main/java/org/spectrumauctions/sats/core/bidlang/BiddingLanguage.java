/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang;

import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;

/**
 * Bidding languages represent a mean for the bidder to express a value for a certain set of goods.
 * Typically, bidding languages in SATS contain an iterator that defines by what logic a bidder provides bids when he
 * is asked for a new bid.
 * For example, we can ask a bidder to provide XOR bids, starting at the smallest bundle and increasing the size the
 * more bids we ask for (see {@link org.spectrumauctions.sats.core.bidlang.xor.IncreasingSizeOrderedXOR}).
 * Another example is to get (parametrized) random bids with the {@link org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR}
 *
 * A famous example of a more domain-specific iterator is described in CATS by Leyton-Brown et al. (2000).
 * The part that makes the domain most interesting is the algorithm on how the bidders bid based on some parameters.
 * If trying to imitate a bidding behavior is the focus in a certain model, such an iterator is essential to the model.
 *
 * On the other hand, if you are less interested in a specific bidding behavior, but are more interested in getting
 * the "most profitable" bids of the bidder in a certain model, you can find those by using a demand query MIP
 * (see {@link org.spectrumauctions.sats.opt.domain.DemandQueryMIP} (and setting the prices to zero if you just want
 * to find the most valuable bids).
 */
public interface BiddingLanguage {

    /**
     * References back to the Bidder instance from whom this
     * Bidding language instance was created.
     */
    Bidder<? extends Good> getBidder();
}

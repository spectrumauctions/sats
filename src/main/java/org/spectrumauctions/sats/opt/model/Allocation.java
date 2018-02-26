/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model;

import org.spectrumauctions.sats.core.model.Bidder;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * @author Michael Weiss
 *
 */
public interface Allocation<T> {

    public Collection<? extends Bidder<?>> getBidders();

    /**
     * Returns information about the goods allocated to a specific bidder
     */
    public T getAllocation(Bidder<?> bidder);

    public BigDecimal getTotalValue();
}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model;

/**
 * The class BidderType is required when creating new Bidders. This interface is usually extended
 * by an abstract, model specific class doing two things:
 * <ul>
 * <li>
 * Defining one method per required information to create the new Bidders. Note that each of these methods is called
 * exactly once per created bidder, allowing counting.</li>
 * <li>
 * The abstract, implementing class should provide the 'default' bidder types used in the original, underlying model
 * specifications.</li>
 * </ul>
 * 
 * @author Michael Weiss
 *
 */
public interface BidderCreationType {

    /**
     * Each Bidder Type must have a name. Must be unique over all BidderTypes
     * within its PopulationStructure
     * 
     * @return
     */
    String name();

    /**
     * The number of bidders of this type
     * Deprecated: The number of bidders of a certain type will not be stored in the biddertype anymore
     * 
     * @return
     */
    int quantity();
}

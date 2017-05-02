/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.util;

import com.google.common.base.Preconditions;

/**
 * @author Michael Weiss
 *
 */
public class PreconditionUtils {

    /**
     * Ensures the all passed objects are not null.
     */
    public static void checkNotNull(Object... objects){
        for(int i = 0; i< objects.length; i++){
            Preconditions.checkNotNull(objects[i], "Item " + i + "in Array");
        }
    }

    /**
     * Ensures the all passed objects are not negative.
     */
    public static void checkNotNegative(int value){
        Preconditions.checkArgument(value >= 0);
    }
}

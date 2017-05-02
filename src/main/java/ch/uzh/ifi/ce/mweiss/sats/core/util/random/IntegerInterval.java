/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.util.random;

import java.io.Serializable;

import com.google.common.base.Preconditions;

public final class IntegerInterval implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 3299198688422530105L;

    private final int minValue;
    private final int maxValue;

    public IntegerInterval(int minValue, int maxValue) {
        Preconditions.checkArgument(maxValue >= minValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public IntegerInterval(int singleValue) {
        this.minValue = singleValue;
        this.maxValue = singleValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }
    
    public boolean isStrictlyPositive(){
        return minValue > 0;
    }
    
    public boolean isNonNegative(){
        return minValue >= 0;
    }
    
    public boolean isStrictlyNegative(){
        return maxValue < 0;
    }

}

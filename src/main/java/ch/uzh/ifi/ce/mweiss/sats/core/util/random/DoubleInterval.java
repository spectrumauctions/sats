/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.util.random;

import java.io.Serializable;

public final class DoubleInterval implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -771682375945835327L;

    private final double minValue;
    private final double maxValue;

    public DoubleInterval(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public DoubleInterval(double singleValue) {
        this.minValue = singleValue;
        this.maxValue = singleValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return new StringBuilder("[").append(minValue).append(",").append(maxValue).append("]").toString();
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

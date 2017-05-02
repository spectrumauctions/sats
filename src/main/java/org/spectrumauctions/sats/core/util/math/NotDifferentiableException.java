/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util.math;

import java.math.BigDecimal;

/**
 * Thrown if a partial function from a Piecewise Linear Function is requested at a corner point, i.e., where the slope is undefined
 * @author Michael Weiss
 *
 */
public class NotDifferentiableException extends Exception {

	private static final long serialVersionUID = -3769389560505390908L;
	private final LinearFunction lowerAdjacentFunction;
    private final LinearFunction higherAdjacentFunction;
    private final BigDecimal x;
    
    /**
     * @param lowerAdjacentFunction The adjacent function for lower x values of the corner point
     * which caused this exception to be thrown 
     * @param higherAdjacentFunction The adjacent function for higher x values of the corner point
     * which caused this exception to be thrown 
     * @param x The corner point x-value which caused this exception to be thrown
     */
    NotDifferentiableException(LinearFunction lowerAdjacentFunction, LinearFunction higherAdjacentFunction, BigDecimal x) {
        super();
        if(lowerAdjacentFunction == null && higherAdjacentFunction == null){
            throw new IllegalArgumentException("At least one adjacent function must be defined");
        }
        this.lowerAdjacentFunction = lowerAdjacentFunction;
        this.higherAdjacentFunction = higherAdjacentFunction;
        this.x = x;
    }
    
    public LinearFunction getLowerAdjacentFunction() {
        return lowerAdjacentFunction;
    }
    
    public LinearFunction getHigherAdjacentFunction() {
        return higherAdjacentFunction;
    }
    
    public BigDecimal getX() {
        return x;
    }
    
    public BigDecimal getCornerPointY(){
        if(lowerAdjacentFunction != null){
            return lowerAdjacentFunction.getY(x);
        }
        return higherAdjacentFunction.getY(x);
    }
    
    
}

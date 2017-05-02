/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Michael Weiss
 *
 */
public final class LinearFunction implements Function{

    private static int SCALE = 10;
    
    private final BigDecimal slope;
    private final BigDecimal yIntercept;
    
    public LinearFunction(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2){
        BigDecimal deltaX = x1.subtract(x2);
        BigDecimal deltaY = y1.subtract(y2);
        this.slope = deltaY.divide(deltaX, SCALE, RoundingMode.CEILING);
        this.yIntercept = y1.subtract(x1.multiply(slope));
    }
    
    public LinearFunction(BigDecimal x, BigDecimal y, BigDecimal slope){
        this.slope = slope;
        this.yIntercept = y.subtract(x.multiply(slope));
    }
    
    public LinearFunction(BigDecimal slope, BigDecimal yIntercept){
        this.slope = slope;
        this.yIntercept = yIntercept;
    }
    
    @Override
    public BigDecimal getY(BigDecimal x){
        return (x.multiply(slope)).add(yIntercept);
    }    
    
    public boolean isValid(BigDecimal x, BigDecimal y){
        return getY(x).compareTo(y) == 0;
    }

    public BigDecimal getSlope() {
        return slope;
    }

    public BigDecimal getyIntercept() {
        return yIntercept;
    }

    public static int getSCALE() {
        return SCALE;
    }

    public static void setSCALE(int scale) {
        SCALE = scale;
    }

    
    
    
}

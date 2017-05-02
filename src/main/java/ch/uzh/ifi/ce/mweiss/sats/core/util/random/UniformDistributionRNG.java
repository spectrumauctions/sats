/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.util.random;

import java.io.Serializable;
import java.math.BigDecimal;

public interface UniformDistributionRNG extends Serializable {

    int nextInt();

    /**
     * Returns a random integer between 0 and upperLimit (exclusive)
     * 
     * @param upperLimit
     * @return
     */
    int nextInt(int upperLimit);

    int nextInt(int lowerLimit, int upperLimit);

    int nextInt(IntegerInterval interval);

    long nextLong();

    double nextDouble();

    double nextDouble(double lowerLimit, double upperLimit);

    double nextDouble(DoubleInterval interval);

    BigDecimal nextBigDecimal();

    BigDecimal nextBigDecimal(double lowerLimit, double upperLimit);

    BigDecimal nextBigDecimal(DoubleInterval interval);

}

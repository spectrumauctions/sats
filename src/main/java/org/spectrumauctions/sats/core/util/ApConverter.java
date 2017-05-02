/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util;

import java.math.BigDecimal;

import org.apfloat.Apfloat;

/**
 * @author Michael Weiss
 *
 */
public class ApConverter {

    public static long PRECISION = 10;

    public static Apfloat toApfloat(BigDecimal value) {
        return new Apfloat(value).precision(PRECISION);
    }

    public static BigDecimal toBigDecimal(Apfloat value) {
        // Convert via String. Haven't found a nicer way of conversion yet
        return new BigDecimal(value.precision(PRECISION).toString(true));
    }

}

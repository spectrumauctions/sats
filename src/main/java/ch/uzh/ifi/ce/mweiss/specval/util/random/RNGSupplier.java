/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.util.random;

import java.io.Serializable;

public interface RNGSupplier extends Serializable {

    /**
     * Returnes a Random Number Generator with Uniform Distribution and a given seed.
     * 
     * @return
     */
    UniformDistributionRNG getUniformDistributionRNG(long seed);

    /**
     * Returnes a Random Number Generator with Uniform Distribution.
     * 
     * @return
     */
    UniformDistributionRNG getUniformDistributionRNG();

    /**
     * Returnes a Random Number Generator with Uniform Distribution and a given seed.
     * 
     * @return
     */
    GaussianDistributionRNG getGaussianDistributionRNG(long seed);

    GaussianDistributionRNG getGaussianDistributionRNG();

}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util.random;

import java.util.Date;
import java.util.Random;

public class GaussianJavaUtilRandomWrapper implements GaussianDistributionRNG {

    private Random rng;

    public GaussianJavaUtilRandomWrapper() {
        this(new Date().getTime());
    }

    public GaussianJavaUtilRandomWrapper(long seed) {
        this.rng = new Random(seed);
    }

    @Override
    public double nextGaussian() {
        return rng.nextGaussian();
    }

    @Override
    public double nextGaussian(double mean, double standardDeviation) {
        return mean + standardDeviation * nextGaussian();
    }
}

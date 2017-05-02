/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael Weiss
 *
 */
public class GenericSetsPickNTest {

    @Test
    public void test() {
        Map<String, Integer> maxQuantities = new LinkedHashMap<>();
        maxQuantities.put("C", 1);
        maxQuantities.put("B", 2);
        maxQuantities.put("A", 2);
        
        GenericSetsPickN<String> pickN = new GenericSetsPickN<String>(maxQuantities, 2);
        int iterCount = 0;
        while(pickN.hasNext()){
            iterCount++;
            Map<String, Integer> quantities = pickN.next();
            Assert.assertNotNull(quantities);
            Assert.assertEquals(maxQuantities.size(),quantities.size());
            int sum = 0;
            for(Entry<String, Integer> val : quantities.entrySet()){
                Assert.assertTrue(val.getValue() >= 0);
                Assert.assertTrue(val.getValue() <= maxQuantities.get(val.getKey()));
                sum+= val.getValue();
            }
            Assert.assertEquals(2, sum);
        }
        Assert.assertEquals(5, iterCount);
    }

}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util;

import com.google.common.base.Objects;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Michael Weiss
 *
 */
public class BigDecimalUtils {

    public static boolean equalIgnoreScale(Collection<BigDecimal> collection1, Collection<BigDecimal> collection2) {
        if (collection1 == null || collection2 == null) {
            return false;
        } else if (collection1.size() != collection2.size()) {
            return false;
        } else {
            List<BigDecimal> sorted1 = new ArrayList<>(collection1);
            List<BigDecimal> sorted2 = new ArrayList<>(collection2);
            Collections.sort(sorted1);
            Collections.sort(sorted2);
            Iterator<BigDecimal> iter1 = sorted1.iterator();
            Iterator<BigDecimal> iter2 = sorted2.iterator();
            while (iter1.hasNext()) {
                if (iter1.next().compareTo(iter2.next()) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Compares if two maps with BigDecimal values are numerically equivalent, i.e, if the keys are {@link Object#equals(Object)} and the values numerically equivalent.
     * @param map1 the first map
     * @param map2 the second map
     * @return true if the values are numerically equivalent
     */
    public static <T> boolean equalIgnoreScaleOnValues(Map<T, BigDecimal> map1, Map<T, BigDecimal> map2) {
        if (map1 == null || map2 == null) {
            return false;
        } else if (map1.size() != map2.size()) {
            return false;
        } else {
            Iterator<Entry<T, BigDecimal>> iter1 = map1.entrySet().iterator();
            Iterator<Entry<T, BigDecimal>> iter2 = map1.entrySet().iterator();
            while (iter1.hasNext()) {
                Entry<T, BigDecimal> entry1 = iter1.next();
                Entry<T, BigDecimal> entry2 = iter2.next();
                if (!entry1.getKey().equals(entry2.getKey())) {
                    return false;
                }
                if ((entry1.getValue() == null && entry2.getValue() != null) ||
                        (entry1.getValue() != null && entry2.getValue() == null)) {
                    //one value, but not both, are null
                    return false;
                }
                if (entry1.getValue().compareTo(entry2.getValue()) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int hashCodeIgnoringScale(Map<?, BigDecimal> map) {
        int keyCode = Objects.hashCode(map.keySet().toArray());
        int valCode = hashCodeIgnoringScale(map.values());
        return keyCode + valCode;
    }

    public static int hashCodeIgnoringScale(Collection<BigDecimal> collection) {
        final int prime = 31;
        int result = prime;
        for (BigDecimal bd : collection) {
            result = prime * result + ((bd == null) ? 0 : new Double(bd.doubleValue()).hashCode());
        }

        return result;
    }

}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.bidlang.xor;

import java.math.BigDecimal;

import ch.uzh.ifi.ce.mweiss.specval.model.Bundle;
import ch.uzh.ifi.ce.mweiss.specval.model.Good;

public class XORValue<T extends Good> {

	private transient final int id;
	private static int ID_COUNT = 0;
	
	private static int getNextId(){
		return ID_COUNT++;
	}
	
    private Bundle<T> licenses;
    private BigDecimal value;

    @Deprecated
    public XORValue(Bundle<T> licenses, double value) {
        this.licenses = licenses;
        this.value = BigDecimal.valueOf(value);
        this.id = getNextId();
    }
    
    public XORValue(Bundle<T> licenses, BigDecimal value) {
        this.licenses = licenses;
        this.value = value;
        this.id = getNextId();       
    }

    public Bundle<T> getLicenses() {
        return licenses;
    }

    public void setLicenses(Bundle<T> licenses) {
        this.licenses = licenses;
    }

    @Deprecated
    public double getValue() {
        return value.doubleValue();
    }
    
    public BigDecimal value(){
        return value;
    }

    @Deprecated
    public void setValue(double value) {
        this.value = BigDecimal.valueOf(value);
    }
    
    public void setValue(BigDecimal value){
        this.value = value;
    }

    /**
     * <p> This instances id
     * <p> Note: The use of the id is only reasonable in a very limited number of cases, e.g.,
     * when transforming bids into MIP-variables.
     */
	public int getId() {
		return id;
	}

}

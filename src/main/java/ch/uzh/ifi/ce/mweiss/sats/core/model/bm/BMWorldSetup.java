/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.model.bm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * @author Michael Weiss
 *
 */
public abstract class BMWorldSetup {
    
    protected final String setupName;
    protected final Map<String, Integer> bands;
    
    protected BMWorldSetup(BMWorldSetupBuilder builder){
        this.bands = ImmutableMap.copyOf(builder.bands);
        this.setupName = builder.setupName;
    }
    
    /**
     * Defines which bands should be created in the world
     * 
     * @return Map with - <b>Key:</> The name of the band - <b>Value:</b> The
     *         quantity of licenses of this band
     */
    public Map<String, Integer> bands(){
        return bands;
    }

    /**
     * The setupName is used to identify the worldSetup once the world is created.
     * For different setups, different setupNames should be used.
     * @return
     */
    public String getSetupName() {
        return setupName;
    }
    
    public static abstract class BMWorldSetupBuilder {
        protected String setupName;
        protected Map<String, Integer> bands = new HashMap<String, Integer>();
        
        public BMWorldSetupBuilder(String setupName){
            this.setupName = setupName;
        }
        
        /**
         * Define new Band which should be generated in the model
         * @param nameOfBand
         * @param numberOfLicenses
         */
        public void addBand(String nameOfBand, int numberOfLicenses){
            Preconditions.checkArgument(numberOfLicenses > 0);
            bands.put(nameOfBand, numberOfLicenses);
        }
        
        /**
         * Remove a band, such that it will not be created. 
         * 
         * @param nameOfBand
         * @return true, if band was removed. false otherwise
         */
        public boolean removeBand(String nameOfBand){
            if(bands.remove(nameOfBand) != null){
                return true;
            }else{
                return false;
            }
        }
        
        /**
         * See {@link BMWorldSetup#bands()} for the purpose of this parameter
         */
        public Map<String, Integer> getBands(){
            return Collections.unmodifiableMap(bands);
        }

        /**
         * See {@link BMWorldSetup#getSetupName()} for the purpose of this parameter
         */
        public String getSetupName() {
            return setupName;
        }

        /**
         * See {@link BMWorldSetup#getSetupName()} for the purpose of this parameter
         */
        public void setSetupName(String setupName) {
            this.setupName = setupName;
        }
        

        /**
         * @return An immutable BMWorldSetup Instance with the in this builder instance defined parameters. 
         */
        public abstract BMWorldSetup build();

    }
    

   

}

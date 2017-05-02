import static org.junit.Assert.fail;

import org.junit.Test;

import edu.harvard.econcs.jopt.example.SimpleLPExample;

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * @author Michael Weiss
 *
 */
public class FindJoptTest {

    @Test
    public void joptLibrarySimpleExample(){
        try{
            SimpleLPExample.main(new String[0]);
        }catch(Exception e){
            System.out.println(e.getStackTrace());
            fail();
        } 
    }
    
    @Test
    public void noAssertions(){
        
    }
    
}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidfile;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

/**
 * @author Michael Weiss
 *
 */
public class JSONWriterTest extends BidFileWriter{
   
    public static String EXPORT_TEST_FOLDER_NAME = "CATSEXPORT_TESTFILES (AUTODELETED FOLDER)";



    @Test
    public void testMultiBidderXOR() {
        JsonExporter exporter = new JsonExporter(new File(EXPORT_TEST_FOLDER_NAME));
        super.testMultiBidderXOR(exporter);
    }

    @Test
    public void testSingleBidderXOR(){
        JsonExporter exporter = new JsonExporter(new File(EXPORT_TEST_FOLDER_NAME));
        super.testSingleBidderXOR(exporter);
    }
    
    @Test 
    public void testMultiBidderXORQ(){
        JsonExporter exporter = new JsonExporter(new File(EXPORT_TEST_FOLDER_NAME));
        super.testMultiBidderXORQ(exporter);
    }
    
    @Test
    public void testSingleBidderXORQ(){
        JsonExporter exporter = new JsonExporter(new File(EXPORT_TEST_FOLDER_NAME));
        super.testSingleBidderXORQ(exporter);
    }
}

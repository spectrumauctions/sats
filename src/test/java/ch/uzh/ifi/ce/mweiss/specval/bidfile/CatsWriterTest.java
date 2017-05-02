/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.bidfile;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import ch.uzh.ifi.ce.mweiss.specval.bidfile.CatsExporter;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.SizeBasedUniqueRandomXOR;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.XORLanguage;
import ch.uzh.ifi.ce.mweiss.specval.model.Good;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.BMBidder;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BaseValueModel;

public class CatsWriterTest extends BidFileWriter{

    public static String EXPORT_TEST_FOLDER_NAME = "CATSEXPORT_TESTFILES (AUTODELETED FOLDER)";

    @Test
    public void testSingleBidderCatsFile() {
        CatsExporter exporter = new CatsExporter(new File(EXPORT_TEST_FOLDER_NAME));
        testSingleBidderXOR(exporter);
    }
    
    @Test
    public void writeMultiBidderCatsFile() {
        CatsExporter exporter = new CatsExporter(new File(EXPORT_TEST_FOLDER_NAME));
        testMultiBidderXOR(exporter);
    }

}

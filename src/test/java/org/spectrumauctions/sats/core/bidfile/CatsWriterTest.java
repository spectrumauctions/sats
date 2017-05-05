/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidfile;

import org.junit.Test;

import java.io.File;

public class CatsWriterTest extends BidFileWriter {

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

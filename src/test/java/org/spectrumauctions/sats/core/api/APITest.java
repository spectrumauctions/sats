/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.api;

import org.junit.Assert;
import org.junit.Test;
import org.spectrumauctions.sats.core.api.MRVMModelCreator.Builder;
import org.spectrumauctions.sats.core.bidfile.CatsWriterTest;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.io.File;
import java.io.IOException;

/**
 * @author Michael Weiss
 *
 */
public class APITest {

    @Test
    public void testDefaultBVMCreatorNoException() throws IllegalConfigException {
        BVMModelCreator.Builder builder = new BVMModelCreator.Builder();
        builder.setFileType(FileType.CATS);
        builder.setGeneric(false);
        builder.setLang(BiddingLanguage.RANDOM);
        builder.setBidsPerBidder(1000);
        BVMModelCreator creator = builder.build();
        try {
            creator.generateResult(new File(CatsWriterTest.EXPORT_TEST_FOLDER_NAME));
        } catch (UnsupportedBiddingLanguageException | IOException e) {
            Assert.fail(e.getLocalizedMessage());
        } catch (IllegalConfigException e) {
            Assert.fail("Illegal Config: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testMRMJsonNoException() throws IllegalConfigException {
        Builder builder = new MRVMModelCreator.Builder();
        builder.setFileType(FileType.JSON);
        builder.setGeneric(true);
        ModelCreator creator = builder.build();
        try {
            creator.generateResult(new File(CatsWriterTest.EXPORT_TEST_FOLDER_NAME));
        } catch (UnsupportedBiddingLanguageException | IOException e) {
            Assert.fail(e.getLocalizedMessage());
        } catch (IllegalConfigException e) {
            Assert.fail("Illegal Config: " + e.getLocalizedMessage());
        }
    }

}

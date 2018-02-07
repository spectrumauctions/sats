/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.spectrumauctions.sats.core.api.APITest;
import org.spectrumauctions.sats.core.bidfile.CatsWriterTest;
import org.spectrumauctions.sats.core.bidfile.JSONWriterTest;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.SimpleRandomOrderTest;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetTest;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericSetsPickNTest;
import org.spectrumauctions.sats.core.bidlang.generic.XORQtoXORTest;
import org.spectrumauctions.sats.core.bidlang.xor.CatsXORTest;
import org.spectrumauctions.sats.core.examples.BiddingLanguagesExample;
import org.spectrumauctions.sats.core.examples.ParameterizingModelsExample;
import org.spectrumauctions.sats.core.examples.SimpleModelAccessorsExample;
import org.spectrumauctions.sats.core.instancehandling.SerializerTest;
import org.spectrumauctions.sats.core.model.DefaultModel;
import org.spectrumauctions.sats.core.model.bvm.BMRandomnessTest;
import org.spectrumauctions.sats.core.model.bvm.BMValueTest;
import org.spectrumauctions.sats.core.model.bvm.SizeOrderedIteratorTest;
import org.spectrumauctions.sats.core.model.bvm.SizeOrderedPowersetTest;
import org.spectrumauctions.sats.core.model.bvm.bvm.BaseValueModel;
import org.spectrumauctions.sats.core.model.bvm.mbvm.MultiBandValueModel;
import org.spectrumauctions.sats.core.model.cats.CATSBidderTest;
import org.spectrumauctions.sats.core.model.cats.CATSRegionModel;
import org.spectrumauctions.sats.core.model.cats.CATSWorldTest;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidderTest;
import org.spectrumauctions.sats.core.model.gsvm.GSVMWorldTest;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidderTest;
import org.spectrumauctions.sats.core.model.lsvm.LSVMWorldTest;
import org.spectrumauctions.sats.core.model.lsvm.LocalSynergyValueModel;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.core.model.srvm.SRVMBidderTest;
import org.spectrumauctions.sats.core.model.srvm.SRVMRandomnessTest;
import org.spectrumauctions.sats.core.model.srvm.SRVMTest;
import org.spectrumauctions.sats.core.model.srvm.SingleRegionModel;
import org.spectrumauctions.sats.core.util.file.FilePathUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // API
        APITest.class,
        // Bidfile
        CatsWriterTest.class,
        JSONWriterTest.class,
        // Instance handling
        SerializerTest.class,
        // Bidlang
        SimpleRandomOrderTest.class,
        GenericPowersetTest.class,
        GenericSetsPickNTest.class,
        XORQtoXORTest.class,
        CatsXORTest.class,
        // Models
        BMRandomnessTest.class,
        BMValueTest.class,
        SizeOrderedIteratorTest.class,
        SizeOrderedPowersetTest.class,
        LSVMWorldTest.class,
        LSVMBidderTest.class,
        GSVMWorldTest.class,
        GSVMBidderTest.class,
        MRVMBidderTest.class,
        MRVMBidderTypeSpecificTest.class,
        MRVMRandomnessTest.class,
        MRVMWorldTest.class,
        SRVMTest.class,
        SRVMBidderTest.class,
        SRVMRandomnessTest.class,
        CATSWorldTest.class,
        CATSBidderTest.class,
        // Examples
        BiddingLanguagesExample.class,
        ParameterizingModelsExample.class,
        SimpleModelAccessorsExample.class
})
//@RunWith(WildcardPatternSuite.class)
//@SuiteClasses({"**/*Test.class", "**/*Example.class"})
public class TestSuite {

    private static final Logger logger = LogManager.getLogger(TestSuite.class);

    private static final File TEST_WORLD_FOLDER = new File("SPECVAL_TESTFILES (AUTODELETED FOLDER)");

    /**
     * Creates new default quick accessors of all implemented models
     */
    public static List<DefaultModel<?, ?>> getAllModelAccessors() {
        List<DefaultModel<?, ?>> models = new ArrayList<>();
        models.add(new BaseValueModel());
        models.add(new MultiBandValueModel());
        models.add(new MultiRegionModel());
        models.add(new SingleRegionModel());
        models.add(new LocalSynergyValueModel());
        models.add(new GlobalSynergyValueModel());
        models.add(new CATSRegionModel());
        return models;
    }

    @BeforeClass
    public static void init() {
        FilePathUtils.FOLDER = TEST_WORLD_FOLDER;
    }

    @AfterClass
    public static void cleanUp() {
        try {
            org.apache.commons.io.FileUtils.forceDelete(TEST_WORLD_FOLDER);
            org.apache.commons.io.FileUtils.forceDelete(new File(CatsWriterTest.EXPORT_TEST_FOLDER_NAME));
            logger.info("Deleted folder containing the worlds and cats files generated in the Tests");
        } catch (IOException e) {
            logger.error("Folder with Worlds generated by Tests could not be deleted");
        }
    }

}

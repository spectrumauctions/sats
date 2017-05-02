/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core;

import org.spectrumauctions.sats.core.api.APITest;
import org.spectrumauctions.sats.core.bidfile.CatsWriterTest;
import org.spectrumauctions.sats.core.bidfile.JSONWriterTest;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.SimpleRandomOrderTest;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericSetsPickNTest;
import org.spectrumauctions.sats.core.examples.BiddingLanguagesExample;
import org.spectrumauctions.sats.core.examples.ParameterizingModelsExample;
import org.spectrumauctions.sats.core.examples.SimpleModelAccessorsExample;
import org.spectrumauctions.sats.core.instancehandling.SerializerTest;
import org.spectrumauctions.sats.core.model.DefaultModel;
import org.spectrumauctions.sats.core.model.bm.BMRandomnessTest;
import org.spectrumauctions.sats.core.model.bm.BMValueTest;
import org.spectrumauctions.sats.core.model.bm.SizeOrderedIteratorTest;
import org.spectrumauctions.sats.core.model.bm.SizeOrderedPowersetTest;
import org.spectrumauctions.sats.core.model.bm.bvm.BaseValueModel;
import org.spectrumauctions.sats.core.model.cats.CATSRegionModel;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidderTest;
import org.spectrumauctions.sats.core.model.gsvm.GSVMWorldTest;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidderTest;
import org.spectrumauctions.sats.core.model.lsvm.LSVMWorldTest;
import org.spectrumauctions.sats.core.model.lsvm.LocalSynergyValueModel;
import org.spectrumauctions.sats.core.model.mrm.*;
import org.spectrumauctions.sats.core.model.srm.SRMTest;
import org.spectrumauctions.sats.core.model.srm.SingleRegionModel;
import org.spectrumauctions.sats.core.util.file.FilePathUtils;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetTest;
import org.spectrumauctions.sats.core.bidlang.generic.XORQtoXORTest;
import org.spectrumauctions.sats.core.model.bm.mbvm.MultiBandValueModel;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.model.srm.SRVMRandomnessTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

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
        // Models
        BMRandomnessTest.class,
        BMValueTest.class,
        SizeOrderedIteratorTest.class,
        SizeOrderedPowersetTest.class,
        LSVMWorldTest.class,
        LSVMBidderTest.class,
        GSVMWorldTest.class,
        GSVMBidderTest.class,
        MRMBidderTest.class,
        MRMBidderTypeSpecificTest.class,
        MRMRandomnessTest.class,
        MRMWorldTest.class,
        SRMTest.class,
        SRVMRandomnessTest.class,
        // Examples
        BiddingLanguagesExample.class,
        ParameterizingModelsExample.class,
        SimpleModelAccessorsExample.class
})
//@RunWith(WildcardPatternSuite.class)
//@SuiteClasses({"**/*Test.class", "**/*Example.class"})
public class TestSuite {

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
            System.out.println("Deleted folder containing the worlds and cats files generted in the Tests");
        } catch (IOException e) {
            System.out.println("Folder with Worlds generated by Tests could not be deleted");
        }
    }

}

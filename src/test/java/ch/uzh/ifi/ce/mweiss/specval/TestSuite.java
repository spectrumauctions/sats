/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval;

import ch.uzh.ifi.ce.mweiss.specval.api.APITest;
import ch.uzh.ifi.ce.mweiss.specval.bidfile.CatsWriterTest;
import ch.uzh.ifi.ce.mweiss.specval.bidfile.JSONWriterTest;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SimpleRandomOrder.SimpleRandomOrderTest;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrderedPowerset.GenericPowersetTest;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrderedPowerset.GenericSetsPickNTest;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.XORQtoXORTest;
import ch.uzh.ifi.ce.mweiss.specval.examples.BiddingLanguagesExample;
import ch.uzh.ifi.ce.mweiss.specval.examples.ParameterizingModelsExample;
import ch.uzh.ifi.ce.mweiss.specval.examples.SimpleModelAccessorsExample;
import ch.uzh.ifi.ce.mweiss.specval.instancehandling.SerializerTest;
import ch.uzh.ifi.ce.mweiss.specval.model.DefaultModel;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.BMRandomnessTest;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.BMValueTest;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.SizeOrderedIteratorTest;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.SizeOrderedPowersetTest;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BaseValueModel;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.mbvm.MultiBandValueModel;
import ch.uzh.ifi.ce.mweiss.specval.model.cats.CATSRegionModel;
import ch.uzh.ifi.ce.mweiss.specval.model.gsvm.GSVMBidderTest;
import ch.uzh.ifi.ce.mweiss.specval.model.gsvm.GSVMWorldTest;
import ch.uzh.ifi.ce.mweiss.specval.model.gsvm.GlobalSynergyValueModel;
import ch.uzh.ifi.ce.mweiss.specval.model.lsvm.LSVMBidderTest;
import ch.uzh.ifi.ce.mweiss.specval.model.lsvm.LSVMWorldTest;
import ch.uzh.ifi.ce.mweiss.specval.model.lsvm.LocalSynergyValueModel;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.*;
import ch.uzh.ifi.ce.mweiss.specval.model.srm.SRMTest;
import ch.uzh.ifi.ce.mweiss.specval.model.srm.SRVMRandomnessTest;
import ch.uzh.ifi.ce.mweiss.specval.model.srm.SingleRegionModel;
import ch.uzh.ifi.ce.mweiss.specval.util.file.FilePathUtils;
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

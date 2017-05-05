/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.clt;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.spectrumauctions.sats.core.api.*;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Michael Weiss
 *
 */
public abstract class ModelOptionParser extends OptionParser {

    public static String KEY_NUMBEROFBIDS = "numberOfBids";
    public static String KEY_MULTIPLEFILES = "multiplefiles";
    public static String KEY_ITERATOR = "iterator";
    public static String KEY_XORQ = "xorq";
    public static String KEY_BIDSPATH = "bidspath";
    public static String KEY_FILETYPE = "filetype";
    public static String KEY_MUTE = "mute";
    public static String KEY_SEED = "seed";

    private static File DEFAULTBIDSPATH = new File("bidfiles");

    public ModelOptionParser() {
        this.accepts("model",
                "Chose which model to use to generate your value function. Possible models: BVM, MBVM, SRVM, MRVM")
                .withRequiredArg().ofType(Model.class).required();
        this.accepts(KEY_NUMBEROFBIDS, "The number of atomic XOR bids per bidder to be written to the output file")
                .withRequiredArg().ofType(Integer.class);
        this.accepts(KEY_MULTIPLEFILES, "define if a separate file should be created for every bidder");
        this.accepts(KEY_ITERATOR,
                "define an order in which the atomic bids should be returned. Options are: SIZE_INCREASING, SIZE_DECREASING, RANDOM, BIDDER_SPECIFIC")
                .withRequiredArg().ofType(BiddingLanguage.class);
        this.accepts(KEY_XORQ, "if flag is set, the returned bids are XOR-Q (And file format JSON)");
        this.accepts(CommandLineTool.KEY_HELP,
                "Gives a list of all possible Options. " + "the options for this model are also printed.");
        this.accepts(KEY_FILETYPE, "Decide for a File Type in which the bids are returned. Options are JSON and CATS")
                .withRequiredArg().ofType(FileType.class);
        this.accepts(KEY_MUTE, "Disables Notification about successful creation of files");
        this.accepts(KEY_SEED, "Specify the seeds used for the creation of the random instances. If two seeds (e.g. --seed 123 --seed 345) are passed, one is used for the creation of "
                + "a non-bidder specific parameters (aka. world) and the second one for the bidders. If only one seed is "
                + "defined, it is used to generate two seeds (for world and bidders)").withRequiredArg()
                .ofType(Long.class);
        this.accepts(KEY_BIDSPATH, "Path to the folder where the bid files should be stored. Default is a folder "
                .concat(DEFAULTBIDSPATH.getName())).withRequiredArg().ofType(String.class);
    }

    protected abstract Model getModel();

    public abstract PathResult treatResult(String[] args)
            throws IllegalConfigException, UnsupportedBiddingLanguageException, IOException;

    protected PathResult allModelsResultTreating(OptionSet options, ModelCreator.Builder builder)
            throws IllegalConfigException, UnsupportedBiddingLanguageException, IOException {
        CommandLineTool.printHelpIfRequested(options, getModel(), this);
        if (options.has(KEY_NUMBEROFBIDS)) {
            builder.setBidsPerBidder((Integer) options.valueOf(KEY_NUMBEROFBIDS));
        }
        if (options.has(KEY_MULTIPLEFILES)) {
            builder.setOneFile(false);
        } else {
            builder.setOneFile(true);
        }
        if (options.has(KEY_ITERATOR)) {
            builder.setLang((BiddingLanguage) options.valueOf(KEY_ITERATOR));
        }

        if (options.has(KEY_SEED)) {
            List<Long> seeds = (List<Long>) options.valuesOf(KEY_SEED);
            System.out.println(seeds);
            if (seeds.size() == 2) {
                builder.setSeedType(SeedType.INDIVIDUALSEED);
                builder.setWorldSeed(seeds.get(0));
                builder.setPopulationSeed(seeds.get(1));
            } else if (seeds.size() == 1) {
                builder.setSeedType(SeedType.SUPERSEED);
                builder.setSuperSeed(seeds.get(0));
            } else {
                System.out.println("The number of provided seeds is not valid. Default seeds were used");
            }
        }
        if (options.has(KEY_XORQ)) {
            builder.setGeneric(true);
        } else {
            builder.setGeneric(false);
        }

        if (options.has(KEY_FILETYPE)) {
            builder.setFileType((FileType) options.valueOf(KEY_FILETYPE));
        } else {
            builder.setFileType(FileType.JSON);
        }

        File outputFolder = DEFAULTBIDSPATH;
        if (options.has(KEY_BIDSPATH)) {
            outputFolder = new File((String) options.valueOf(KEY_BIDSPATH));
        }
        outputFolder.mkdir();
        return builder.build().generateResult(outputFolder);
    }

    /**
     * Factory Method to create a ModelOptionParser for a specific model
     *
     * @param model
     * @return
     */
    public static ModelOptionParser createOptionParser(Model model) {
        switch (model) {
            case BVM:
                return new BVMModelOptionParser();
            case MBVM:
                return new MBVMModelOptionParser();
            case SRVM:
                return new SRVMModelOptionParser();
            case MRVM:
                return new MRVMModelOptionParser();
            case LSVM:
                return new LSVMModelOptionParser();
            case GSVM:
                return new GSVMModelOptionParser();
            case CATS:
                return new CATSModelOptionParser();
            default:
                throw new IllegalArgumentException("Unknown Model, No request Parser Defined");
        }
    }

    /**
     * Prints the help not specific to a certain model<br>
     * Only call this methods if no model is defined. <br>
     * If model is defined, the model parser will take care of the help printing
     */
    public static void printGeneralHelp() {
        ModelOptionParser dummy = new ModelOptionParser() {
            @Override
            public PathResult treatResult(String[] args) {
                throw new UnsupportedOperationException("Anonymous class only to be used for help printing");
            }

            @Override
            protected Model getModel() {
                return null;
            }
        };
        CommandLineTool.printHelp("all models", dummy);
    }
}

/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.util.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * @author Michael Weiss
 *
 */
public class FilePathUtils {
   
    /**
     * Determines where the files will be stored. <br>
     * Important, if you want to change this 
     * 
     */
    public static java.io.File FOLDER = new java.io.File("sats_output");
    
    private final java.io.File folder;
    private static final String FILE_TYPE_BIDDER = ".bidder.json";
    private static final String FILE_TYPE_WORLD = ".world.json";
    private static final int BIDDER_ID_LENGTH = 5;
    private static final int POPULATION_ID_LENGTH = 5;
    private static final int WORLD_ID_LENGTH = 5;
    
    private static FilePathUtils instance = null;
    
    private FilePathUtils(){
        folder = FOLDER;
        if (!folder.exists())
            folder.mkdir();
    };
    
    
    public static FilePathUtils getInstance(){
        if (instance == null){
            instance = new FilePathUtils();
        }
        return instance;       
    }
    
    public java.io.File bidderFilePath(long worldId, long populationId, long bidderId){
        String world = prependZeros(WORLD_ID_LENGTH, String.valueOf(worldId));
        String population = prependZeros(POPULATION_ID_LENGTH, String.valueOf(populationId));
        String bidder = prependZeros(BIDDER_ID_LENGTH, String.valueOf(bidderId));
        return bidderFilePath(world, population, bidder);
    }
    
    public java.io.File populationFolderPath(long worldId, long populationId){
        String world = prependZeros(WORLD_ID_LENGTH, String.valueOf(worldId));
        String population = prependZeros(POPULATION_ID_LENGTH, String.valueOf(populationId));
        return populationFolderPath(world, population);
            
    }
    
    public java.io.File worldFolderPath(long worldId){
        String world = prependZeros(WORLD_ID_LENGTH, String.valueOf(worldId));
        return worldFolderPath(world);   
    }
    
    public java.io.File worldFilePath(long worldId){
        String world = prependZeros(WORLD_ID_LENGTH, String.valueOf(worldId));
        String fileName = world.concat(FILE_TYPE_WORLD);
        return new java.io.File(worldFolderPath(world).getAbsolutePath().concat("/").concat(fileName));   
    }

    
    private java.io.File bidderFilePath(String worldId, String population, String bidderId) {
        String worldString = prependZeros(WORLD_ID_LENGTH, worldId);
        String populationString = prependZeros(POPULATION_ID_LENGTH, population);
        String bidderString = prependZeros(BIDDER_ID_LENGTH, bidderId);
        return new java.io.File(folder.getAbsolutePath() + "/" + worldString + "/" + populationString + "/"
                + bidderString + FILE_TYPE_BIDDER);
    }

    private java.io.File populationFolderPath(String worldId, String population) {
        String worldString = prependZeros(WORLD_ID_LENGTH, worldId);
        String populationString = prependZeros(POPULATION_ID_LENGTH, population);
        return new java.io.File(folder.getAbsolutePath() + "/" + worldString + "/" + populationString);
    }

    private java.io.File worldFolderPath(String worldId) {
        String worldString = prependZeros(WORLD_ID_LENGTH, worldId);
        return new java.io.File(folder.getAbsolutePath() + "/" + worldString);
    }
    
    
    private String prependZeros(int stringLength, String number) {
        StringBuilder preZeros = new StringBuilder();
        for (int i = number.length(); i < BIDDER_ID_LENGTH; i++) {
            preZeros.append('0');
        }
        return preZeros.toString() + number;
    }
    
    public Collection<Long> getPopulationIds(long worldId){
        File worldFolder = worldFolderPath(worldId);
        File[] subFilesArray = worldFolder.listFiles();
        if(subFilesArray == null){
            throw new FileException("Files could not be read. Check if Folder exists!");
        }
        List<File> subFiles = Arrays.asList();
        List<Long> ids = new ArrayList<>();
        for(File subFile : subFiles){
            if(subFile.isDirectory()){
                try{
                    ids.add(Long.valueOf(subFile.getName()));
                }catch(NumberFormatException e){
                    System.out.println("Folder which doesn't belong here was found and ignored");
                }
            }  
        }
        return ids;
    }
    
    public Collection<Long> getBidderIds(long worldId, long populationId){
        File populationFolder = populationFolderPath(worldId, populationId);
        File[] subFilesArray = populationFolder.listFiles();
        if(subFilesArray == null){
            throw new FileException("Files could not be read. Check if Folder exists!");
        }
        List<File> subFiles = Arrays.asList(subFilesArray);
        List<Long> ids = new ArrayList<>();
        for(File subFile : subFiles){
            if(!subFile.isDirectory()){
                String[] fileName = subFile.getName().split("\\.");
                String name = fileName[0];
                if(subFile.getName().contains(FILE_TYPE_BIDDER)){
                    try{
                        ids.add(Long.valueOf(name));
                    }catch(NumberFormatException e){
                        throw new RuntimeException("Invalid Bidder File", e);
                    }
                }else{
                    System.out.println("File which is not a bidder file here was found and ignored " + subFile.getName());
                }       
            }  
        }
        return ids;
    }

    public String readFileToString(File file){
        String fileContent;
        try {
            fileContent = FileUtils.readFileToString(file);
        } catch (IOException e) {
            throw new FileException(e);
        }
        return fileContent;
    }
    
    public void writeStringToFile(File file, String content){
        try {
            FileUtils.write(file, content);
        } catch (IOException e) {
            throw new FileException(e);
        }
    }

}

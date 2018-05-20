import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: verman
 * Date: 5/4/2014
 * Time: 10:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class BayesianClassifier {

    /*
    Creating three HashMaps
    1) One HasMmap for spam words (String:Integer) => the number of total appearances of it in Spam e-mails
    2) One HashMap for ham words (String:Integer) => the number of total appearances of it in Ham e-mails
    3) One HashMap to store spamacity (String:Double) => To reduce dimensionality you could also keep only the words with highest and lowest spamicity
     */
    private Map<String, Integer> spamWords = new HashMap<String, Integer>();
    private Map<String, Integer> hamWords = new HashMap<String, Integer>();
    private Map<String, Double> spamacityWords = new HashMap<String, Double>();

    public File[] filterFiles(File[] initialFiles) {

        Vector listOfFiles = new Vector();
        for(int i = 0; i<initialFiles.length; i++){
            if (initialFiles[i].getName().endsWith(".txt")) {
                listOfFiles.addElement(initialFiles[i]);
            }
        }

        File[] fileArray = new File[listOfFiles.size()];
        listOfFiles.toArray(fileArray);
        return fileArray;

   }

   /*
   Creation of a method to read from a file
   returns a string, i.e. the content of the file
   */
   public String readFromFile(File f){
        //initialize create and initialize the content to the empty string
        String content = "";
        //helper variable, used to keep in it a line at a time of a txt file.
        String line = "";

       try {
           // FileReader reads text files in the default encoding.
           FileReader fileReader = new FileReader(f);

           // Wrap FileReader in BufferedReader. (JUST JAVA THINGS)
           BufferedReader bufferedReader = new BufferedReader(fileReader);

           while((line = bufferedReader.readLine()) != null) {
               //APPEND LINE TO CONTENT
               content += line.toString()+"\n";
           }

           // Done with reading, close the file
           bufferedReader.close();
       }
       //Maybe you give extra points for error handling too...
       //kidding, you don't, but I want to show u my insane skills
       //kidding, just programming with care
       catch(FileNotFoundException ex) {
           System.out.println("Unable to open file '" + f + "'");
       }
       catch(IOException ex) {
           System.out.println("Error reading file '"+ f + "'");
       }

        //returns the content of the file
       return content;
   }

   /*
   * This method adds and updates a hash map
   * */
   public HashMap<String, Integer> updateWordToHashMap(String word, HashMap<String, Integer> givenMap){

        if(!givenMap.containsKey(word)){
            //add the word in the map
            //set its value to 1
            givenMap.put(word, 1);
        }else{
            //increment the integer
            givenMap.put(word, givenMap.get(word)+1);
        }

        return givenMap;
   }
    public void train(String spamTrainingFolder, String hamTrainingFolder) {

        File spamTrainingDirectory = new File(spamTrainingFolder);
                if (!spamTrainingDirectory.exists()){
            System.out.println("ERR: The Spam Training Directory does not exist");
            return;
        }

        File hamTrainingDirectory = new File(hamTrainingFolder);
        if (!hamTrainingDirectory.exists()){
            System.out.println("ERR: The Ham Training Directory does not exist");
            return;
        }


        File spamFiles[] = filterFiles(spamTrainingDirectory.listFiles());

        
        int numberOfFiles = 0;

        
        for (File f : spamFiles) {
            /*
                TODO
             */

            //Reading from the file
            String fileContent = readFromFile(f);
            //TODO Sanitizing the file, i.e. removing stopwords, stemming

            //remove lines
            fileContent = fileContent.replace("\n"," ");
            //remove Subject:
            fileContent = fileContent.replace("Subject: ", " ");
            //remove special characters
            //fileContent = fileContent.replaceAll("[^a-zA-Z0-9] +","");
            //System.out.println(fileContent);

            //for each word update the hashmap
            for(String word:fileContent.split(" ")){
                //Prepare for stemming
                Stemmer stemmer = new Stemmer();

                //lowercase the string
                word = word.toLowerCase();

                //creatinc a char array from the current word
                char [] wordChars = word.toCharArray();

                //stem the cars and save them in a new string
                stemmer.add(wordChars, wordChars.length);
                stemmer.stem();

                //System.out.print("original word: "+word+"\t");
                word = stemmer.toString();
                //System.out.print("stemmed word: "+word+"\n");

                //add the new stemmed word to the hash map
                updateWordToHashMap(word, (HashMap<String, Integer>) spamWords);
            }
            //remove the " " from the hash map
            spamWords.remove(" ");


            numberOfFiles++;

        }
        //print hashmap
//        Iterator itSpam = spamWords.entrySet().iterator();
//        while (itSpam.hasNext()) {
//            Map.Entry pairSpam = (Map.Entry)itSpam.next();
//            System.out.println(pairSpam.getKey() + " = " + pairSpam.getValue());
//            itSpam.remove(); // avoids a ConcurrentModificationException
//        }

        System.out.println(numberOfFiles+" files found in spam training folder");

        numberOfFiles = 0;
        File hamFiles[] = filterFiles(hamTrainingDirectory.listFiles());
        for (File f : hamFiles) {
            /*
                TODO
             */

            //Reading from the file
            String fileContent = readFromFile(f);
            //TODO Sanitizing the file, i.e. removing stopwords, stemming

            //remove lines
            fileContent = fileContent.replace("\n"," ");
            //remove Subject:
            fileContent = fileContent.replace("Subject: ", " ");
            //remove special characters
            fileContent = fileContent.replaceAll("[^a-zA-Z0-9] +","");
            //System.out.println(fileContent);

            //for each word update the hashmap
            for(String word:fileContent.split(" ")){
                //Prepare for stemming
                Stemmer stemmer = new Stemmer();

                //lowercase the string
                word = word.toLowerCase();

                //creatinc a char array from the current word
                char [] wordChars = word.toCharArray();

                //stem the cars and save them in a new string
                stemmer.add(wordChars, wordChars.length);
                stemmer.stem();

                //System.out.print("original word: "+word+"\t");
                word = stemmer.toString();
                //System.out.print("stemmed word: "+word+"\n");

                updateWordToHashMap(word, (HashMap<String, Integer>) hamWords);
            }
            //remove the " " from the hash map
            hamWords.remove(" ");

            numberOfFiles++;
        }
        //print hashmap
//        Iterator itHam = hamWords.entrySet().iterator();
//        while (itHam.hasNext()) {
//            Map.Entry pairHam = (Map.Entry)itHam.next();
//            System.out.println(pairHam.getKey() + " = " + pairHam.getValue());
//            itHam.remove(); // avoids a ConcurrentModificationException
//        }

        System.out.println(numberOfFiles+" files found in ham training folder");

    }

    public void test(String spamTestingFolder, String hamTestingFolder) {

        File spamTestingDirectory = new File(spamTestingFolder);
        if (!spamTestingDirectory.exists()){
            System.out.println("ERR: The Spam Testing Directory does not exist");
            return;
        }

        File hamTestingDirectory = new File(hamTestingFolder);
        if (!hamTestingDirectory.exists()){
            System.out.println("ERR: The Ham Testing Directory does not exist");
            return;
        }

        System.out.println("Testing phase:");
        
        int allSpam = 0;
        int SpamClassifiedAsHam = 0; //Spams incorrectly classified as Hams

        File spamFiles[] = filterFiles(spamTestingDirectory.listFiles());
        for (File f : spamFiles) {
            allSpam++;
            if (!isSpam(f))
                SpamClassifiedAsHam++;

        }

        int allHam = 0;
        int HamClassifiedAsSpam = 0; //Hams incorrectly classified as Spams
        
        File hamFiles[] = filterFiles(hamTestingDirectory.listFiles());
        for (File f : hamFiles) {
            allHam++;
            if (isSpam(f))
                HamClassifiedAsSpam++;

        }

        System.out.println("###_DO_NOT_USE_THIS_###Spam = "+allSpam);
        System.out.println("###_DO_NOT_USE_THIS_###Ham = "+allHam);
        System.out.println("###_DO_NOT_USE_THIS_###SpamClassifAsHam = "+SpamClassifiedAsHam);
        System.out.println("###_DO_NOT_USE_THIS_###HamClassifAsSpam = "+HamClassifiedAsSpam);


    }



    public boolean isSpam(File f){
        /*
        TODO
        implement method
        erase the following "return true" statement
         */
       return true;
    }
    
}

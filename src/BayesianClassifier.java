import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    private Map<String, Double> spamWords = new HashMap<String, Double>();
    private Map<String, Double> hamWords = new HashMap<String, Double>();
    private Map<String, Double> spamacityWords = new HashMap<String, Double>();
    private HashMap<String, Integer> termOccursInEmailMap = new HashMap<>();
    //STOP WORDS TAKEN FROM NLTK PYTHON
    private String STOP_WORDS[] = {"a", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an",
            "and", "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could",
            "dear", "did", "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has",
            "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it", "its",
            "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my", "neither", "no",
            "nor", "not", "of", "off", "often", "on", "only", "or", "other", "our", "own", "rather", "said", "say",
            "says", "she", "should", "since", "so", "some", "than", "that", "the", "their", "them", "then", "there",
            "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was", "we", "were", "what", "when",
            "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your"};

    /**
     * Number of terms considered when predicting a tested file,
     * terms are sorted by their absolute value compared to 0.5 */
    private static final int NR_OF_TERMS_CONSIDER = 20;
    /**
     * The probability threshold used to decide a spam email */
    private static final double SPAM_PROBAB_THRESHOLD = 0.7;
    /**
     * The threshold of occurrence under which term will be discarded,
     * If a term occurs only in a few files, we will not consider it. */
    private static final int OCCUR_THRESHOLD_OF_DISCARD_TERM = 5;


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
   public HashMap<String, Double> updateWordToHashMap(String word, HashMap<String, Double> givenMap){

        if(!givenMap.containsKey(word)){
            //add the word in the map
            //set its value to 1
            givenMap.put(word, (double) 1);
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
            for(String word:fileContent.split(" ")) {
                if (!isStopWord(word)) {
                    //Prepare for stemming
                    Stemmer stemmer = new Stemmer();

                    //lowercase the string
                    word = word.toLowerCase();

                    //creatinc a char array from the current word
                    char[] wordChars = word.toCharArray();

                    //stem the cars and save them in a new string
                    stemmer.add(wordChars, wordChars.length);
                    stemmer.stem();

                    //System.out.print("original word: "+word+"\t");
                    word = stemmer.toString();
                    //System.out.print("stemmed word: "+word+"\n");

                    //add the new stemmed word to the hash map
                    updateWordToHashMap(word, (HashMap<String, Double>) spamWords);

                    //add all terms and their frequencies here
                    int val = termOccursInEmailMap.getOrDefault(word, 0);
                    termOccursInEmailMap.put(word, val+1);
                }
                //remove the " " from the hash map
                spamWords.remove(" ");
            }



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
                //remove stop words
                if(!isStopWord(word)) {

                    //Prepare for stemming
                    Stemmer stemmer = new Stemmer();

                    //lowercase the string
                    word = word.toLowerCase();

                    //creatinc a char array from the current word
                    char[] wordChars = word.toCharArray();

                    //stem the cars and save them in a new string
                    stemmer.add(wordChars, wordChars.length);
                    stemmer.stem();

                    //System.out.print("original word: "+word+"\t");
                    word = stemmer.toString();
                    //System.out.print("stemmed word: "+word+"\n");

                    updateWordToHashMap(word, (HashMap<String, Double>) hamWords);

                    //add all terms and their frequencies here
                    int val = termOccursInEmailMap.getOrDefault(word, 0);
                    termOccursInEmailMap.put(word, val+1);
                }
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


        //compute spamacity probability
        spamacityProbability();

        //print hashmap
//        Iterator itSpam = spamWords.entrySet().iterator();
//        while (itSpam.hasNext()) {
//            Map.Entry pairSpam = (Map.Entry)itSpam.next();
//            System.out.println(pairSpam.getKey() + " = " + pairSpam.getValue());
//            itSpam.remove(); // avoids a ConcurrentModificationException
//        }
        //print hashmap
//        Iterator itHam = hamWords.entrySet().iterator();
//        while (itHam.hasNext()) {
//            Map.Entry pairHam = (Map.Entry)itHam.next();
//            System.out.println(pairHam.getKey() + " = " + pairHam.getValue());
//            itHam.remove(); // avoids a ConcurrentModificationException
//        }

        System.out.println(numberOfFiles+" files found in ham training folder");

    }

    //CALCULATE THE SPAMACITY OF A WORD; start with unbiased P(S) = 0.5 and P(H) = 0.5.
    public void spamacityProbability(){

        HashSet<String> spamTerms = new HashSet<String>(this.spamWords.keySet());
        HashSet<String> hamTerms = new HashSet<String>(this.hamWords.keySet());

        /** merge the vocabulary of hamTermMap to spamTermMap */
        for (String term : hamTerms) {
            if (!spamTerms.contains(term)) {
                this.spamWords.put(term, 0.0);
                spamTerms.add(term);
            }
        }

        /** calculate term value counts */
        double spamSum = 0;
        for (double v : this.spamWords.values()) {
            spamSum += v;
        }

        double hamSum = 0;
        for (double v : this.hamWords.values()) {
            hamSum += v;
        }

        for (String term : spamTerms) {
            Integer termOccurs = this.termOccursInEmailMap.getOrDefault(term, 0);
            if (termOccurs <= OCCUR_THRESHOLD_OF_DISCARD_TERM) {
                this.spamWords.remove(term);
                continue;
            }

            double spamFreq = this.spamWords.get(term) / spamSum;
            double hamFreq = this.hamWords.getOrDefault(term, 0.0) / hamSum;

            double spamTermProbab = spamFreq/(spamFreq + hamFreq);
            this.spamWords.put(term, spamTermProbab);
        }


    }

    //Check if a word is a stop word
    public boolean isStopWord(String word){
       boolean toReturn = false;

       for(String stopWord:STOP_WORDS){
           if(word.toLowerCase() == stopWord)
               toReturn =  true;
       }

       return  toReturn;
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

    private ArrayList<String> cleanString(String line) {
        ArrayList<String> strings = new ArrayList<>();

        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String tobeStemmed = matcher.group();
            for(String stopWord:STOP_WORDS) {
                if (stopWord == tobeStemmed) {
                    continue;
                }
            }

            Stemmer stemmer = new Stemmer();
            stemmer.add(tobeStemmed.toCharArray(), tobeStemmed.toCharArray().length);
            stemmer.stem();
            strings.add(stemmer.toString());
        }
        return strings;
    }


    public boolean isSpam(File f){
        HashSet<String> tockens = new HashSet<>();
        ArrayList<Double> termProbabs = new ArrayList<Double>();

        String fileContent = readFromFile(f);
        //remove lines
        fileContent = fileContent.replace("\n"," ");
        //remove Subject:
        fileContent = fileContent.replace("Subject: ", " ");

        //for each word update the hashmap
        for(String word:fileContent.split(" ")) {
            if (!isStopWord(word)) {
                //Prepare for stemming
                Stemmer stemmer = new Stemmer();

                //lowercase the string
                word = word.toLowerCase();

                //creatinc a char array from the current word
                char[] wordChars = word.toCharArray();

                //stem the cars and save them in a new string
                stemmer.add(wordChars, wordChars.length);
                stemmer.stem();

                //System.out.print("original word: "+word+"\t");
                word = stemmer.toString();
                //System.out.print("stemmed word: "+word+"\n");

                //add the new stemmed word to the hash map
                tockens.add(word);
            }
        }

        for (String str : tockens) {
            if (this.spamWords.containsKey(str)) {
                termProbabs.add(this.spamWords.get(str));
            }
//                termProbabs.add(this.spamTermMap.getOrDefault(str, UNKNOWN_WORD_DEFAULT_PROBAB));
        }



        Collections.reverse(termProbabs);
        PriorityQueue<Double> descendSortedProbabs = new PriorityQueue<Double>();
        Collections.sort(termProbabs);
        PriorityQueue<Double> ascendSortedProbabs = new PriorityQueue<Double>(termProbabs);

        double logSum = 0;
        for (int i = 0; i < NR_OF_TERMS_CONSIDER; i++) {
            Double highProbab = descendSortedProbabs.poll();
            Double lowProbab = ascendSortedProbabs.poll();
            if (highProbab != null) {
                if (highProbab == 1.0) {highProbab = 0.9999;}
                logSum += Math.log(1 - highProbab) - Math.log(highProbab);
            }
            if (lowProbab != null) {
                if (lowProbab == 0.0) {lowProbab = 0.0001;}
                logSum += Math.log(1 - lowProbab) - Math.log(lowProbab);
            }
        }
        double p = 1 / (1 + Math.exp(logSum));

        if (p > SPAM_PROBAB_THRESHOLD) {
            return true;
        } else { return false;}

    }
    
}

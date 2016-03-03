/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.queries;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Crane476
 */
public class IRQueries {

    /**
     * @param args the command line arguments
     */
    /*
     * Function Name: main()
     * Arguments: String[] args
     * Returns: N/A
     * Description: This method takes two inputs from the user. These inputs
     * are the input file directory, and stop word list directory respectively.
     * The file paths for each file in the input directory are then read into an
     * array. Next, the stop words from the specified stop word file are read
     * into an array. These two arrays are then passed to the getTokens()
     * function.
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String inDirectory = JOptionPane.showInputDialog(null, "Enter an Input Directory: ");
        String stopPath = JOptionPane.showInputDialog(null, "Enter File Path for stopword list: ");
        File fileNames = new File(inDirectory);
        File fList[] = fileNames.listFiles();
        ArrayList<String> filePaths = new ArrayList<>();
        ArrayList<String> stopwords = getStopWords(stopPath);
        for (int i = 0; i < fList.length; i++) {
            filePaths.add(fList[i].toString());
        }

        HashMap<String, HashMap<Integer, Integer>> invertedIndex = getTokens(filePaths, stopwords);
        output(invertedIndex);
    }

    /*
     * Function Name: getStopWords()
     * Arguments: String string
     * Returns: ArrayList<String>
     * Description: This method reads a stopword file specified by the user into
     * an ArrayList and returns it.
     */
    public static ArrayList<String> getStopWords(String path) {
        ArrayList<String> stopWords = new ArrayList<>();
        try {
            stopWords = (ArrayList<String>) Files.readAllLines(Paths.get(path));
        } catch (IOException ex) {
            Logger.getLogger(IRQueries.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stopWords;
    }

    /*
    * Function Name: processSpecialCharacters()
    * Arguments: String string
    * Returns: String
    * Description: This method takes a string and removes any special characters if needed.
    */
    
    public static String processSpecialCharacters(String word) {
        word = word.replaceAll("[+^&:\\[\\],\"/();]", "");
        int length = word.length();
        if (word.contains("'")) {
            if (word.charAt(length - 2) == '\'') {
                if (word.charAt(length - 1) == 's') {
                    word = word.replace("'", "");
                }
            } else if (word.charAt(length - 1) == '\'') {
                word = word.replace("'", "");
            }
        } else if (word.contains(".")) {
            char[] array = word.toCharArray();
            int count = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] == '.') {
                    count++;
                }
            }
            if (count == 1 && word.charAt(length - 1) == '.') {
                word = word.replace(".", "");
                System.out.println(word);
            } else {
                System.out.println(word);
            }
        }
        else if(word.equals("-")){
            word = word.replace("-", "");
        }
        return word;
    }

    /*
     * Function Name: getTokens()
     * Arguments: ArrayList<String>, ArrayList<String>
     * Returns: N/A
     * Description: This method iterates through a list of files and reads in
     * text from each file. Each word is converted to lowercase, checked for 
     * special characters, and stemmed. If it is a stop word it is removed. Once
     * the word has been tokenized it is then added to the inverted index. After
     * the inverted index has been built it is passed to the output function.
     */
    public static HashMap<String, HashMap<Integer, Integer>> getTokens(ArrayList<String> files, ArrayList<String> stopWords) {
        String newToken; //current word being read from file
        HashMap<String, HashMap<Integer, Integer>> index = new HashMap<>(); //List of tokens which map to posting
        HashMap<Integer, Integer> posting; //list of document ID's which map to number of occurrences
        for (int i = 0; i < files.size(); i++) { //loop through list of files
            try {
                Scanner fileIn = new Scanner(new File(files.get(i))); //read in word from file
                while (fileIn.hasNext()) {
                    newToken = fileIn.next(); //assign word to newToken
                    newToken = newToken.toLowerCase(); //convert to lowercase
                    //newToken = newToken.replaceAll("[-+.^:,&\"/=();]", ""); //remove all special characters
                    newToken = processSpecialCharacters(newToken);
                    if (!stopWords.contains(newToken) && !newToken.equals("")) {
                        newToken = WordStem(newToken); //reduce word to root form
                        if (index.containsKey(newToken)) {
                            if (index.get(newToken).containsKey(i)) { //if term has already been encountered in this document
                                HashMap<Integer, Integer> newPosting = (HashMap) index.get(newToken).clone(); //shallow copy of posting
                                newPosting.put(i, newPosting.get(i) + 1); //update number of occurrences
                                index.put(newToken, newPosting); //overwrite posting with new one
                            } else { //term has been encountered, but not in current document
                                HashMap<Integer, Integer> newPosting = (HashMap) index.get(newToken).clone();
                                newPosting.put(i, 1);
                                index.put(newToken, newPosting);
                            }
                        } else { //term has not been encountered yet
                            posting = new HashMap<>();
                            posting.put(i, 1);
                            index.put(newToken, posting);
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(IRQueries.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        //output(index);
        return index;
    }

    /*
     * Function Name: output()
     * Arguments: HashMap<String, HashMap<Integer, Integer>> map, String string
     * Returns: N/A
     * Description: This function iterates through the inverted index and appends 
     * the output to a formatted string. This string is then written to a file
     * in a directory specified by the user.
     */
    public static void output(HashMap<String, HashMap<Integer, Integer>> invertedIndex) {
        String outFile = JOptionPane.showInputDialog(null, "Enter Path for Output File: ");
        TreeMap<String, HashMap<Integer, Integer>> sortedIndex = new TreeMap(invertedIndex); //sort invertedIndex by converting to treeMap
        Iterator tokens = sortedIndex.keySet().iterator();
        String formatStr = "%-15s %25s %n";
        String output = String.format(formatStr, "Token", "<Frequency, DocID>\n");
        while (tokens.hasNext()) {
            String token;
            String posting = "";
            token = tokens.next().toString();
            TreeMap<Integer, Integer> sortedID = new TreeMap(sortedIndex.get(token));
            Iterator ID = sortedID.keySet().iterator();
            int count = 0;
            while (ID.hasNext()) {
                int docID = (Integer) ID.next();
                int frequency = sortedIndex.get(token).get(docID);
                posting += "<" + frequency + ", " + docID + "> ";
                count++;
                if(count == 5){
                    count = 0;
                    posting += "\n";
                }
            }
            //System.out.printf("%-15s %25s %n", token, posting);
            output += String.format(formatStr, token, posting);
        }
        try {
            File file = new File(outFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(output);
            }

        } catch (IOException e) {
        }

    }

    /*
     * Function Name: WordStem
     * Arguments: String string
     * Returns: String
     * Description: This method takes a string and reduces it to its root form.
     */
    public static String WordStem(String word) {
        Stemmer st = new Stemmer();
        String s1 = st.step1(word);
        String s2 = st.step2(s1);
        String s3 = st.step3(s2);
        String s4 = st.step4(s3);
        String s5 = st.step5(s4);
        word = s5;
        return word;
    }
}

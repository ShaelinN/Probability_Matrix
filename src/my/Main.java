package my;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CollectionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class Main {
    //lists
    private static List<List<String>> NGrams;
    private static List<List<String>> uniqueMarkovParents;
    private static List<List<String>> uniqueTokens;

    //matrix
    private static double[][] matrix;

    //size of n grams
    private static int N = 2;



    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
         String myString = "";
        try {
            Scanner scanner = new Scanner(new File("data.txt"));
            while (scanner.hasNextLine()){
                myString+=scanner.nextLine();
            }
        }catch (FileNotFoundException e){
            System.out.println("File Not Found");
            myString = "I am sam. sam I am. I do not like green eggs and ham.";

        }

        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument document = new CoreDocument(myString);
        // annotate the document
        pipeline.annotate(document);


        NGrams = getNGrams( N, document,false);
        uniqueMarkovParents = getNGrams(N - 1, document,true);
        uniqueTokens = getNGrams( 1, document,true);

        matrix = createMatrix(NGrams, uniqueMarkovParents, uniqueTokens);


/*
        //printing to console and file
        System.out.println("printing Matrix:");

        PrintWriter writer = new PrintWriter("output.txt","utf-8");
        String formatSpacer = "%"+((N-1)*30)+"s";

        writer.format(formatSpacer,"");
        System.out.format(formatSpacer,"");
        for (int i = 0; i < uniqueTokens.size(); i++) {//column headers
            writer.format("%20s", uniqueTokens.get(i).get(0));
            System.out.format("%20s", uniqueTokens.get(i).get(0));
        }
        writer.println();
        System.out.println();
        for (int i = 0; i < matrix.length; i++) {
            writer.format(formatSpacer, uniqueMarkovParents.get(i).toString());//row headers
            System.out.format(formatSpacer, uniqueMarkovParents.get(i).toString());
            for (int j = 0; j < matrix[0].length; j++) {
                writer.format("%20.16f",matrix[i][j]);
                System.out.format("%20.16f",matrix[i][j]);
            }
            writer.println();
            System.out.println();
        }
        writer.close();
        */

    }

    private static List<List<String>> getNGrams(int N, CoreDocument document, boolean unique) {
        List<List<String>> strings = OriginalTextFromCoreLabels(CollectionUtils.getNGrams(document.tokens(), N, N));

        if(unique){
            HashSet<List<String>> hashSet = new HashSet<>(strings);
            strings = new ArrayList<>(hashSet);
        }

        return strings;
    }


    private static List<List<String>> OriginalTextFromCoreLabels(List<List<CoreLabel>> cList){
        List<List<String>> sList = new ArrayList<>();
        for (int i = 0; i < cList.size(); i++) {
            sList.add(new ArrayList<>());
            for (int j = 0; j < cList.get(i).size(); j++) {
                String s = cList.get(i).get(j).originalText();
                sList.get(i).add(s);
            }
        }
        return sList;
    }

    private static double[][] createMatrix(List<List<String>> NGrams, List<List<String>> uniqueMarkovParents, List<List<String>> uniqueTokens){
        //make a matrix with as many rows as unique parent markov chains and as many columns as there are unique tokens
        double[][] matrix = new double[uniqueMarkovParents.size()][uniqueTokens.size()];

        for (int i = 0; i < uniqueMarkovParents.size(); i++) {
            for (int j = 0; j < uniqueTokens.size(); j++) {
                for (List list: NGrams) {
                    //P(x|y....z)
                    if(list.subList(0,list.size()-1).equals(uniqueMarkovParents.get(i))){//if (y....z) are present
                        if(list.get(list.size()-1).equals(uniqueTokens.get(j).get(0))){//if x is present
                            matrix[i][j]++;
                        }
                    }
                }
            }
        }
        //matrix now contains pure counts, not yet probabilities
        //divide rows by their sums to get probabilities
        double rowSum = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                rowSum+=matrix[i][j];
            }
            if(rowSum!=0){
                for (int j = 0; j <matrix[0].length ; j++) {
                    matrix[i][j] = matrix[i][j] / rowSum;
                }
            }

            rowSum = 0;
        }

        return matrix;
    }


}
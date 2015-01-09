package tue.webanalytics.group9;

import net.didion.jwnl.data.Exc;
import org.apache.uima.resource.ResourceInitializationException;
import org.deeplearning4j.text.inputsanitation.InputHomogenization;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.UimaTokenizerFactory;
import org.omg.CORBA_2_3.portable.*;

import java.io.*;
import org.deeplearning4j.text.inputsanitation.InputHomogenization;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Hello world!
 *
 */
public class App
{

    WordCollection wordCollection = new WordCollection();
    TokenizerFactory tokenizerFactory;
    HashSet<String> stopWordsList = new HashSet<>();
    Vector<sentimentEnum> phraseSentiments = new Vector<>(240000);

    String [] adversativeConjunctions = {
            "but",
            "yet",
            "still",
            "however",
            "albeit",
            "although"};
    HashSet<String> advConj = new HashSet<>(adversativeConjunctions.length);
    String [] negatingWords = {"No",
            "Not",
            "None",
            "Nobody",
            "Nothing",
            "Neither",
            "Nowhere",
            "Never",
            "Hardly",
            "Scarcely",
            "Barely",
            "Doesn’t",
            "Isn’t",
            "Wasn’t",
            "Shouldn’t",
            "Wouldn’t",
            "Couldn’t",
            "Won’t",
            "Can’t",
            "Don’t"};
    HashSet<String> negWords = new HashSet<>(negatingWords.length);

    public static void main( String[] args )
    {

        if(args.length != 2)
        {
            System.out.println("Usage: "+args[0]+" <input file name> <evaluation file name>");
            System.out.println("in the input file:");
            System.out.println("  each line is a phrase to check sentiment, separated with | with the id of phrase");
            System.out.println("in the evaluation file");
            System.out.println("  each line begins with phrase id , separated by | with it's sentiment value");
            return;
        }

        File input = new File(args[0]);
        if(!input.exists() || !input.canRead())
        {
            System.out.println("File "+args[0]+" doesn't exist");
            return ;
        }
        File test = new File(args[1]);
        if(!test.exists() || !test.canRead())
        {
            System.out.println("File "+args[1]+" doesn't exist");
            return ;
        }
        App app = new App();
        try {
            app.start(input,test);
        } catch (Exception e) {
            e.printStackTrace();
            return ;
        }
        return ;
    }

     void start(File inputFile,File testFile) throws Exception {
         negWords.addAll(java.util.Arrays.asList(negatingWords));
         advConj.addAll(java.util.Arrays.asList(adversativeConjunctions));
         try {
             tokenizerFactory = new UimaTokenizerFactory();
         } catch (Exception e) {
             e.printStackTrace();
         }
         loadStopWords();
         loadSentiWordNetVectors();

         processInput(inputFile,testFile);
     }

    private void loadStopWords() throws IOException {
        System.out.println("Loading stop words...");
        InputStream is = getClass().getResourceAsStream("/StopWords.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while((line = br.readLine()) != null)
        {
            if(line.length()>0)
                stopWordsList.add(line);
        }
        br.close();
        System.out.println("Stop words loaded");
    }

    private void processInput(File inputFile,File testFile) throws Exception {

        readTestSentiments(testFile);

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))) ;
        String line;
        System.out.println("Reading input file...");


        LinkedList<String> lineHolder = new LinkedList<>();

        // for gathering statistics and evaluation
        int totalCount = 0;
        int totalMatching = 0;
        int totalVotingMatching = 0;
        int totalPositiveMatching = 0, totalPositive = 0;
        int totalNegativeMatching = 0, totalNegative = 0;
        int totalNeutralMatching = 0, totalNeutral = 0;
        while((line = br.readLine()) != null)
        {
            int voting[] = {0, 0, 0};
            String s[] = line.split(Pattern.quote("|"));
            Integer index = Integer.parseInt(s[1]);
            lineHolder.clear();
            lineHolder.add(s[0]);
            CollectionSentenceIterator sentenceIterator = new CollectionSentenceIterator(lineHolder);
            try {
                float phraseSumPositive = 0;
                float phraseSumNegative = 0;
                //iterating through sentences
                while(sentenceIterator.hasNext()) {

                    String phrase = (new InputHomogenization(sentenceIterator.nextSentence())).transform();
                    Tokenizer tokenizer = tokenizerFactory.create(phrase);

                    float sumPositive = 0;
                    float sumNegative = 0;
                    int negating = 0;
                    while (tokenizer.hasMoreTokens()) {
                        String word = tokenizer.nextToken();
                        if (stopWordsList.contains(word))
                            continue;//ignoring stop words

                        if(advConj.contains(word))
                        {
                            //adversative conjunction
                            //but,albeit,however ....
                            //resetting previous sentiment in that sentence
                            sumPositive = 0;
                            sumNegative = 0;
                            continue;

                        }
                        if(negWords.contains(word))
                        {// negating word, will negate next words in the sentence
                            negating = 3; //negating next three words
                            continue;
                        }
                        Word w = wordCollection.getWord(word);
                        if(negating == 0) {
                            sumPositive += w.positive;
                            sumNegative += w.negative;
                        }
                        else
                        {
                            sumPositive += w.negative;
                            sumNegative += w.positive;
                            negating--;
                        }
                        voting[1 + w.getSentiment()]++;
                    }
                    //judging sentence
                    float sum = sumPositive - sumNegative;
                    if (sum < -0.3)
                        phraseSumNegative++;
                    if (sum > 0.3)
                        phraseSumPositive++;
                }
                //judging phrase

                sentimentEnum sentiment = sentimentEnum.neutral;

                if (phraseSumNegative > phraseSumPositive && phraseSumNegative > 0)
                    sentiment = sentimentEnum.negative;
                if (phraseSumPositive > phraseSumNegative && phraseSumPositive > 0)
                    sentiment = sentimentEnum.positive;

                sentimentEnum votingSentiment = sentimentEnum.neutral;
                if (voting[0] > voting[1] && voting[0] > voting[2])
                    votingSentiment = sentimentEnum.negative;
                if (voting[2] > voting[0] && voting[2] > voting[1])
                    votingSentiment = sentimentEnum.positive;

                sentimentEnum realSentiment = phraseSentiments.get(index);
                System.out.println("sentiment: computed - " + sentiment + " (sum: " + (phraseSumPositive-phraseSumNegative) + ") voting - " + votingSentiment + ", supposed to be - " + realSentiment + ", phrase: '" + line + "'");
                totalCount++;
                if (realSentiment == sentiment)
                    totalMatching++;
                if (votingSentiment == realSentiment)
                    totalVotingMatching++;
                switch (realSentiment) {
                    case positive:
                        totalPositive++;
                        if (sentiment == realSentiment)
                            totalPositiveMatching++;
                        break;
                    case negative:
                        totalNegative++;
                        if (sentiment == realSentiment)
                            totalNegativeMatching++;
                        break;
                    case neutral:
                        totalNeutral++;
                        if (sentiment == realSentiment)
                            totalNeutralMatching++;
                        break;
                }
            }
            catch(Exception ex)
            {//ignoring parse errors

            }
        }
        br.close();
        System.out.println();
        System.out.println("Evaluation: "+(100*totalMatching/(float)totalCount)+"% matching, voting: "+(100*totalVotingMatching/(float)totalCount)+"% matching");
        System.out.println("Positive matching: "+(100*totalPositiveMatching/(float)totalPositive)+"% (total "+totalPositive+"), Negative matching: "+(100*totalNegativeMatching/(float)totalNegative)+"% (total "+totalNegative+"),  Neutral matching "+(100*totalNeutralMatching/(float)totalNeutral)+"% (total "+totalNeutral+")");
        //TODO:finish this
    }
    public enum sentimentEnum {
        negative(-1),
        neutral(0),
        positive(1);

        private int numVal;
        sentimentEnum(int numVal)
        {
            this.numVal = numVal;
        }
        public int getNumVal(){
            return numVal;
        }

    };
    private void readTestSentiments(File testFile) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(testFile))) ;
        String line;
        System.out.println("Reading test file sentiments...");

        while((line = br.readLine()) != null)
        {
            try {
                String s[] = line.split(Pattern.quote("|"));
                Integer index = Integer.parseInt(s[0]);
                float sentiment = Float.parseFloat(s[1]);
                if(sentiment <= 0.4)
                    phraseSentiments.add(index,sentimentEnum.negative);
                else if(sentiment > 0.6)
                    phraseSentiments.add(index,sentimentEnum.positive);
                else
                    phraseSentiments.add(index,sentimentEnum.neutral);

            }
            catch(Exception ex)
            {

            }
        }
        br.close();
        System.out.println("Test file sentiments loaded.");
    }


    void loadSentiWordNetVectors() throws Exception {
        System.out.println("Loading sentiwordnet database...");
        InputStream is = getClass().getResourceAsStream("/SentiWordNet_3.0.0_20130122.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while((line = br.readLine()) != null)
        {
            if(line.charAt(0) == '#')
                continue;
            String[] s = line.split("\t");

            if(s.length < 5)
                continue;
            try {
                int id = Integer.parseInt(s[1]);
                float positive = Float.parseFloat(s[2]);
                float negative = Float.parseFloat(s[3]);
                String[] words = s[4].split(" ");
                for(String word:words)
                {
                    String w = word.substring(0,word.indexOf('#')).toLowerCase();
                    wordCollection.addWord(w,id,positive,negative);
                }

            }
            catch(Exception ex)
            {

            }
        }
        br.close();
        System.out.println("Loaded "+wordCollection.idToWord.size()+" words with sentiment");
    }
}

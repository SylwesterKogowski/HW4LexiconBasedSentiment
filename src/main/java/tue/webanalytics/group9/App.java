package tue.webanalytics.group9;

import net.didion.jwnl.data.Exc;
import org.deeplearning4j.text.inputsanitation.InputHomogenization;
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.omg.CORBA_2_3.portable.*;

import java.io.*;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App
{

    WordCollection wordCollection = new WordCollection();

    public static void main( String[] args )
    {
        if(args.length != 1)
        {
            System.out.println("Usage: "+args[0]+" <input file name>");
            System.out.println("in the input file:");
            System.out.println("  each line is a phrase to check sentiment");
            return;
        }


        File input = new File(args[0]);
        /*if(!input.exists() || !input.canRead())
        {
            System.out.println("File "+args[0]+" doesn't exist");
            return ;
        }*/
        App app = new App();
        try {
            app.start(input);
        } catch (Exception e) {
            e.printStackTrace();
            return ;
        }
        return ;
    }

     void start(File inputFile) throws Exception {
         loadSentiWordNetVectors();

         processInput(inputFile);
     }

    private void processInput(File inputFile) throws Exception {
        if(!inputFile.exists())
        {
            throw new Exception("Input file "+inputFile.getName()+" doesn't exist");
        }
        if(!inputFile.canRead())
        {
            throw new Exception("Input file "+inputFile.getName()+" cannot be read");
        }

        //TODO:finish this
    }


    void loadSentiWordNetVectors() throws Exception {
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
            int id = Integer.parseInt(s[1]);
            float positive = Float.parseFloat(s[2]);
            float negative = Float.parseFloat(s[3]);
            String[] words = s[4].split(" ");
            for(String word:words)
            {
                String w = word.substring(0,word.indexOf('#'));
                wordCollection.addWord(w,id,positive,negative);
            }
        }
    }
}

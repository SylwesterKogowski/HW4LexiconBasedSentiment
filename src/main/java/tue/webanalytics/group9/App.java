package tue.webanalytics.group9;

import org.deeplearning4j.text.inputsanitation.InputHomogenization;
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;

import java.io.File;

/**
 * Hello world!
 *
 */
public class App
{


    public static int main( String[] args )
    {
        if(args.length != 2)
        {
            System.out.println("Usage: "+args[0]+" <input file name>");
            System.out.println("in the input file:");
            System.out.println("  each line is a phrase to check sentiment");
            return 1;
        }

        File input = new File(args[1]);
        if(!input.exists() || !input.canRead())
        {
            System.out.println("File "+args[1]+" doesn't exist");
            return 2;
        }
        App app = new App();
        app.start(input);
        return 0;
    }

     void start(File inputFile)
     {
         loadSentiWordNetVectors();

         processInput(inputFile);
     }

    private void processInput(File inputFile) {

    }


    void loadSentiWordNetVectors()
    {

    }
}

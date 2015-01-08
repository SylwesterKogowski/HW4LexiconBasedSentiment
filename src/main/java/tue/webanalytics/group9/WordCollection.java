package tue.webanalytics.group9;

import java.util.HashMap;

/**
 * Created by sylwek on 8-1-15.
 */
public class WordCollection {
    HashMap<Integer,Word> idToWord = new HashMap<>();
    HashMap<String,Word> stringToWord = new HashMap<>();
    public void addWord(String word,int id,float positive, float negative){
        if(stringToWord.containsKey(word))
        {

            //TODO:finish
        }
        else
        {
            Word newWord = new Word();
            newWord.id = id;
            newWord.negative = negative;
            newWord.positive = positive;
            newWord.word = word;
            idToWord.put(id,newWord);
            stringToWord.put(word,newWord);
        }
    }
}

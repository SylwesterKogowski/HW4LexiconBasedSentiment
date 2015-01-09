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
            Word oldWord = stringToWord.get(word);
            oldWord.addAnotherDef(negative,positive,word);
        }
        else
        {
            Word newWord = new Word(id,negative,positive,word);
            idToWord.put(id,newWord);
            stringToWord.put(word,newWord);
        }
    }

    public Word getWord(String word) throws Exception {
        if(!stringToWord.containsKey(word))
        {
            //find the closest possible word from our database
            int minDist = 1000;
            HashMap.Entry<String,Word> minDistWord = null;
            for(HashMap.Entry<String,Word> kv:stringToWord.entrySet())
            {
                int d = levenshteinDistance(word,kv.getKey());
                if(d < minDist)
                {
                    minDist = d;
                    minDistWord = kv;
                }
            }
            if(minDistWord == null)
                throw new Exception("Unknown word encountered '"+word+"' and no similar found");
            Word w = minDistWord.getValue();
            addWord(word,w.id,w.positive,w.negative);
            return w;

        }
        else
        {
            return stringToWord.get(word);
        }
    }
    int levenshteinDistance(String s, String t)
    {
        // degenerate cases
        if (s.equals(t)) return 0;
        if (s.length() == 0) return t.length();
        if (t.length() == 0) return s.length();

        // create two work vectors of integer distances
        int[] v0 = new int[t.length() + 1];
        int[] v1 = new int[t.length() + 1];

        // initialize v0 (the previous row of distances)
        // this row is A[0][i]: edit distance for an empty s
        // the distance is just the number of characters to delete from t
        for (int i = 0; i < v0.length; i++)
            v0[i] = i;

        for (int i = 0; i < s.length(); i++)
        {
            // calculate v1 (current row distances) from the previous row v0

            // first element of v1 is A[i+1][0]
            //   edit distance is delete (i+1) chars from s to match empty t
            v1[0] = i + 1;

            // use formula to fill in the rest of the row
            for (int j = 0; j < t.length(); j++)
            {
                int cost = (s.charAt(i) == t.charAt(j)) ? 0 : 1;
                int t1 = v1[j] + 1;
                int t2 = v0[j + 1] + 1;
                int t3 = v0[j] + cost;
                if ( t1 < t2 )
                    v1[j+1]=t1;
                else
                    v1[j+1]=t2;
                if(t3 < v1[j+1])
                    v1[j+1]=t3;

            }

            // copy v1 (current row) to v0 (previous row) for next iteration
            for (int j = 0; j < v0.length; j++)
                v0[j] = v1[j];
        }

        return v1[t.length()];
    }
}

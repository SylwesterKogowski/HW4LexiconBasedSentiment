package tue.webanalytics.group9;

/**
 * Created by sylwek on 8-1-15.
 */
public class Word {
    int id;
    String word;
    float positive;
    float negative;
    float totalNegative;
    float totalPositive;
    int amount;

    public float getObjectivity() {
        return 1-(positive+negative);
    }

    /**
     * Will give sentiment classification for that word
     * @return 0 if neutral, -1 if negative, 1 if positive
     */
    public int getSentiment(){
        if(positive  > negative)
        {
            if(getObjectivity() > positive)
                return 0;
            else
                return 1;
        }
        else
        {
            if(getObjectivity() > negative)
                return 0;
            else
                return -1;

        }
    }


    public Word(int id, float negative, float positive, String word) {

        this.id = id;
        this.negative = negative;
        this.positive = positive;
        this.word = word;
        this.totalNegative = negative;
        this.totalPositive = positive;
        this.amount = 1;
    }

    public void addAnotherDef(float negative, float positive, String word)
    {
        totalPositive += positive;
        totalNegative += negative;
        amount++;
        this.negative = totalNegative/amount;
        this.positive = totalPositive/amount;
    }
}

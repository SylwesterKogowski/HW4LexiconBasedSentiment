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

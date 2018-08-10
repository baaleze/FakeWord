package vahren.fr.fakeword;


import android.content.Context;

/**
 * Created by fdroumaguet on 10/08/18.
 */

public class Lang {
    public int file;
    public String lang;
    public int minLength;
    public int maxLength;
    public Romanizer romanizer;
    public String[] illegalStarts;

    public Lang(int f, String l, int min, int max, Romanizer r, String[] i) {
        file = f;
        lang = l;
        minLength = min;
        maxLength = max;
        romanizer = r;
        illegalStarts = i;
    }
}

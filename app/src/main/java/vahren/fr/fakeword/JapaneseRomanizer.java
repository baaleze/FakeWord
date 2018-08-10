package vahren.fr.fakeword;

import com.moji4j.MojiConverter;

/**
 * Created by fdroumaguet on 10/08/18.
 */

public class JapaneseRomanizer implements Romanizer {

    MojiConverter converter = new MojiConverter();

    @Override
    public String toRoman(String word) {
        return converter.convertKanaToRomaji(word);
    }
}

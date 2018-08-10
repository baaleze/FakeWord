package vahren.fr.fakeword;

import com.github.kimkevin.hangulparser.HangulParser;
import com.github.kimkevin.hangulparser.HangulParserException;

import java.util.List;

/**
 * Created by fdroumaguet on 10/08/18.
 */

class KoreanRomanizer implements Romanizer {


    private static final char[] CHOSUNG_LIST = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };
    private static final String[] CHOSUNG_ROM_INI = {
            "g", "kk", "n", "d", "tt", "l", "m", "b", "pp", "s",
            "ss", "", "j", "jj", "ch", "k", "t", "p", "h"
    };
    private static final String[] CHOSUNG_ROM_FIN = {
            "k", "k", "n", "d", "", "l", "m", "p", "", "t",
            "t", "ng", "t", "", "t", "k", "t", "p", "t"
    };


    // 21 vowels
    private static final char[] JUNGSUNG_LIST = {
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
            'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ',
            'ㅣ'
    };
    private static final String[] JUNGSUNG_ROM = {
            "a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o", "wa",
            "wae", "oe", "yo", "u", "wo", "we", "wi", "yu", "eu", "ui",
            "i"
    };

    // 28 consonants placed under a vowel(plus one empty character)
    private static final char[] JONGSUNG_LIST = {
            ' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
            'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };
    private static final String[] JONGSUNG_ROM = {
            " ", "g", "kk", "ks", "n", "nj", "nh", "d", "l", "lg",
            "lm", "lb", "ls", "lt", "lp", "lh", "m", "b", "bs", "s",
            "ss", "", "j", "ch", "k", "t", "p", "h"
    };

    @Override
    public String toRoman(String word) {
        try {
            List<String> jasos = HangulParser.disassemble(word);
            StringBuilder res = new StringBuilder();
            for(int i = 0; i< jasos.size();i++){
                String j = jasos.get(i);
                int f = in(j, CHOSUNG_LIST);
                if (f != -1) {
                    // END or not followed by vowel
                    if (i == jasos.size()-1 || in(jasos.get(i+1),JUNGSUNG_LIST) == -1) {
                        res.append(CHOSUNG_ROM_FIN[f]);
                    } else {
                        res.append(CHOSUNG_ROM_INI[f]);
                    }
                    continue;
                }
                f = in(j, JUNGSUNG_LIST);
                if (f != -1) { res.append(JUNGSUNG_ROM[f]);continue;}
                f = in(j, JONGSUNG_LIST);
                if (f != -1) { res.append(JONGSUNG_ROM[f]);}
            }

            return res.toString();
        } catch (HangulParserException e) {
            return "???";
        }


    }

    private int in(String s, char[] list){
        for(int i = 0; i< list.length;i++){
            if(list[i] == s.charAt(0)){
                return i;
            }
        }
        return -1;
    }
}

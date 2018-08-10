package vahren.fr.fakeword;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.content.Context;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.content.res.Resources.Theme;

import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import rita.RiMarkov;

public class MainActivity extends AppCompatActivity {

    private static Lang[] langs;

    private static ClipboardManager clip;
    private static RiMarkov currentMarkov;
    private static final String END = "$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivity.langs = new Lang[]{
                new Lang(R.raw.french, "French", 4, 12, null, new String[]{"-"}, 5),
                new Lang(R.raw.english, "English", 4, 12, null, new String[]{"-"}, 5),
                new Lang(R.raw.japanese, "Japanese", 2, 8, new JapaneseRomanizer(),
                        new String[]{"・", "ょ", "ッ", "っ", "ャ", "ゥ", "ゃ", "ゅ", "ィ", "ァ","ェ", "ォ","ョ", "ー", "ュ" ,"ン", "ん"}, 3),
                new Lang(R.raw.norsk, "Norwegian", 6, 15, null, new String[]{}, 5),
                new Lang(R.raw.korean, "Korean", 1, 4, new KoreanRomanizer(), new String[]{}, 3)
        };

        clip = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup spinner
        String[] strLang = new String[MainActivity.langs.length];
        for(int i =0; i<strLang.length;i++){
            strLang[i] = MainActivity.langs[i].lang;
        }
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setAdapter(new MyAdapter(
                toolbar.getContext(), strLang));

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }



    private static class MyAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        public MyAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private TextView wordView;
        private TextView wordSubView;
        private Lang lang;
        private View rootView;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            lang = MainActivity.langs[getArguments().getInt(ARG_SECTION_NUMBER)];
            TextView textView = rootView.findViewById(R.id.section_label);
            textView.setText("Generating "  + lang.lang);
            wordView = rootView.findViewById(R.id.gen_word);
            wordSubView = rootView.findViewById(R.id.word_sub);
            // do this in an async task as it's LOOONG
            new Markov(rootView).execute(lang);
            // touch
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);

            return rootView;
        }

        @Override
        public void onClick(View v) {
            this.gen();
        }

        @Override
        public boolean onLongClick(View view) {
            StringBuilder words =  new StringBuilder();
            words.append(this.wordView.getText());
            if(this.wordSubView.getText() != null && this.wordSubView.getText().length() > 0){
                words.append(this.wordSubView.getText());
            }
            clip.setPrimaryClip(ClipData.newPlainText("Fake text", words.toString()));
            Toast.makeText(view.getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
            return true;
        }

        private void gen(){
            if (currentMarkov != null) {
                StringBuffer word = new StringBuffer();
                int i = 0;
                while(word.length() < lang.minLength) {
                    String[] chars = currentMarkov.generateTokens(MainActivity.random(lang.minLength, lang.maxLength));
                    for (String s : chars) {
                        if (END.equals(s)) {
                            break;
                        }
                        if (i > 0 || !isIllegal(lang.illegalStarts, s)) {
                            word.append(s);
                        }
                        i++;

                    }
                }
                wordView.setText(word);
                if (lang.romanizer != null){
                    wordSubView.setText(lang.romanizer.toRoman(word.toString()));
                } else {
                    wordSubView.setText("");
                }
            }
        }

        private boolean isIllegal(String[] illegalStarts, String s) {
            for(String i : illegalStarts){
                if (i.equals(s)){
                    return true;
                }
            }
            return false;
        }

    }

    private static class Markov extends AsyncTask<Lang, Void, RiMarkov> {

        private final View view;
        private final Context context;
        private Lang lang;

        public Markov(View rootView) {
            this.view = rootView;
            this.context = rootView.getContext();
        }

        @Override
        protected void onPreExecute() {
            ((TextView)view.findViewById(R.id.gen_word)).setText("LOADING");
            ((TextView)view.findViewById(R.id.word_sub)).setText("...");
            currentMarkov = null;
        }

        @Override
        protected RiMarkov doInBackground(Lang... langs) {
            lang = langs[0];
            try {
                // instantiate markov generator
                return this.loadUniqueNames(
                        this.readResource(lang.file,this.context),
                        lang.markovFactor);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(RiMarkov riMarkov) {
            ((TextView)view.findViewById(R.id.gen_word)).setText("READY");
            ((TextView)view.findViewById(R.id.word_sub)).setText("");
            currentMarkov = riMarkov;
        }

        private RiMarkov loadUniqueNames(List<String> names, int factor) {
            RiMarkov uniqueGen = new RiMarkov(factor, false, true);
            List<Character> tokens = new LinkedList<>();

            for (String name : names) {
                char[] chars;
                int l = (chars = name.toLowerCase().toCharArray()).length;

                for (int i = 0; i < l; ++i) {
                    char c = chars[i];
                    tokens.add(c);
                }
            }

            char[] tokensArray = new char[tokens.size()];
            int i = 0;

            Character c;
            for(Iterator var12 = tokens.iterator(); var12.hasNext(); tokensArray[i++] = c) {
                c = (Character)var12.next();
            }

            uniqueGen.loadTokens(tokensArray);
            return uniqueGen;
        }


        private List<String> readResource(int resId, Context context) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(resId)));
            List<String> s = new LinkedList<>();
            String readLine;
            while ((readLine = reader.readLine()) != null) {
                s.add(readLine);
            }
            reader.close();
            return s;
        }

    }


    private static int random(int minLength, int maxLength) {
        return (int) Math.floor(Math.random() *  (maxLength-minLength) + minLength);
    }


}

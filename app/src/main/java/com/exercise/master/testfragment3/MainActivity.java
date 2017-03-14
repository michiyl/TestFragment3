package com.exercise.master.testfragment3;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    protected int selectedItem = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // mit dem folgenden "verschwindet" die Android-Leiste ganz oben,
        // lässt sich aber wieder durch Runterwischen (von ganz oben etwas nach unten) erreichen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        // MainActivity macht eigentlich nichts spektakuläres
    }

    /**
     * Das Fragment für die (scrollbare) Auswahlliste
     */
    public static class AuswahlFragment extends ListFragment {
        // Key zum Merken der Auswahl
        private static final String STR_ZULETZT_SELEKTIERT = "zuletztSelektiert";
        int zuletztSelektiert = 0;
        boolean zweiSpaltenModus; /* = false */

        ArrayList<String> myMapArrayList = new ArrayList<>();

        // wenn die Activity erstellt wurde, dann wird diese Methode weitermachen
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // als erstes:
            // die View über einen Adapter aus einem Array befüllen
            // = das, was im Array steht, wird zu einem Element in der View
            // Adapter: container, layout, und etwas mit dem man es verbindet

            /*
            setListAdapter(new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1,
                    new String[]{"eins", "zwei", "drei"}));
            */

            // für simple_list_item_2 muss der Adapter anders gesetzt werden:
            // ACHTUNG: hier noch mit android.R-Einstellungen, also vorgegebene Elemente!
            // wenn man eigene Layoutelemente benutzen will, dann:
            // 1. Strg + Klick auf
            //      android.R.layout.simple_list_item_2
            //      -> dann darin alles in ein eigenes XML-Layout kopieren & Einträge umbenennen
            // 2. "android.R.id.text1" in die ID der eigenen TextView ändern!
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setListAdapter(new ArrayAdapter(getContext(),
                        R.layout.myfragment,
                        R.id.twolineTextViewText1,
                        new String[]{"XYz", "qwerty", "3", "4"})   // Anzahl der Einträge = Anzahl der Listenelemente!
                               {
                                   @NonNull
                                   @Override
                                   public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                       View view = super.getView(position, convertView, parent);
                                       TextView text1 = (TextView) view.findViewById(R.id.twolineTextViewText1);
                                       TextView text2 = (TextView) view.findViewById(R.id.twolineTextViewText2);
                                       text1.setText("Test Map");
                                       text2.setText("mp_testmap");

                                       return view;
                                   }
                               }


                );
            }



            // prüfen, ob das Details-Fragment eingebunden weden kann
            View detailFragment = getActivity().findViewById(R.id.details_fragment_layoutParent);
            // wenn detailFragment nicht null ist UND die Sichtbarkeit auf "sichtbar ist" => true
            zweiSpaltenModus = detailFragment != null && detailFragment.getVisibility() == View.VISIBLE;

            if(savedInstanceState != null) {
                // hol den Int mit dem Key, wenn nichts da ist dann 0
                zuletztSelektiert = savedInstanceState.getInt(STR_ZULETZT_SELEKTIERT, 0);
            }

            if(zweiSpaltenModus) /* = true */ {
                // man will Details anzeigen, also soll nur EIN Item auswählbar sein!
                getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                detailsAnzeigen(zuletztSelektiert);
            }
        } // <-- end of onActivityCreated


        // hiermit merken wir uns vor dem Drehen, was ausgewählt war
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            // merk dir den zuletzt selektierten Eintrag:
            outState.putInt(STR_ZULETZT_SELEKTIERT, zuletztSelektiert);
        }

        public void onListItemClick(ListView l, View view, int position, long id) {
            detailsAnzeigen(position);
            TextView text1 = (TextView) view.findViewById(R.id.twolineTextViewText1);
            TextView text2 = (TextView) view.findViewById(R.id.twolineTextViewText2);

            text1.setTextColor(Color.GREEN);
        }

        /**
         * Hier werden die Details angezeigt. Anhand des Wertes für zweiSpaltenModus entwede als <br>
         * Fragment (wenn TRUE) oder in einer Activity (wenn FALSE)
         * @param index - aktueller Wert des List-Eintrags
         */
        private void detailsAnzeigen(int index) {
            zuletztSelektiert = index;
            if(zweiSpaltenModus /* = true */) {
                // Eintrag soll wieder selektiert sein
                getListView().setItemChecked(index, true);
                //DetailsFragment details = (DetailsFragment)getFragmentManager().findFragmentById(R.id.details);
                DetailsFragment details = (DetailsFragment)getFragmentManager().findFragmentById(R.id.details_fragment_layoutParent);

                if(details == null || details.getIndex() != index) {
                    // neues Fragment zum selektierten Eintrag anzeigen
                    details = DetailsFragment.newInstance(index);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.details, details);  // ersetze R.id.details mit details
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);    // übergangseffekt
                    ft.commit();
                }
            }
            else {
                Intent intent = new Intent();
                intent.setClass(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsFragment.INDEX, index);  // Wertüberlieferung
                startActivity(intent);
            }
        }
    } // <-- end of inner class


    // == Details als Fragment ==
    public static class DetailsFragment extends Fragment {
        public static final String INDEX = "index";


        // neue Instanz erzeugen
        public static DetailsFragment newInstance(int index) {
            DetailsFragment f = new DetailsFragment();
            // Bundle hat was mit Key-Value-Paaren zu tun
            Bundle args = new Bundle();
            args.putInt(INDEX, index);
            // beim Erzeugen wird der Schlüssel gleich vergeben
            f.setArguments(args);

            return f;
        }


        public int getIndex() {
            return getArguments().getInt(INDEX, 0);    // der Zählwert, der beim Schlüsselwert hinterlegt ist, 0 = default
        }


        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Altes Verfahren mit nur einer ScrollView, die hier erzeugt wurde
            /*
            ScrollView scroller = null;
            // Aufbau des scroller:
            // wenn der container zur Verfügung steht, dann erzeuge was
            if (container != null) {
                scroller = new ScrollView(getActivity());
                TextView text = new TextView(getActivity());
                scroller.addView(text); // TextView wird in die ScrollView geklinkt
                text.setText("Element " + (getIndex()+1) + " ist sichtbar" );
                text.setTextColor(Color.WHITE);
            }
            // Aufbau fertig, nun Ausgabe scroller:
            // return scroller;
            */

            // === neu und angepasst ===
            LinearLayout mylayout = (LinearLayout)inflater.inflate(R.layout.fragment_details, container, false);
            TextView text = null;
            if (container != null) {
                text = (TextView)container.findViewById(R.id.details_fragment_textView);
                text.setText("Element " + (getIndex()+1) + " ist sichtbar" );
            }
            else {
                Log.d("DetailsFragment", "Else! " + container.toString());
            }


            return mylayout;
        }
    }


    // == Details als Activity ==
    // Activity erstellt sich aus dem Fragment
    // -> also Fragment bearbeiten!
    public static class DetailsActivity extends AppCompatActivity {

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // soll es gezeigt werden oder nicht?
            // wird anhand der Ausrichtung des Geräts ermittelt
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                finish();   // beendet
                return;
            }

            if(savedInstanceState == null) {
                DetailsFragment details = new DetailsFragment();
                // wenn eine neue Activity aufgerufen wird, dann wird ein Intent genutzt
                // um Daten zu transferieren
                details.setArguments(getIntent().getExtras());
                getFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
                // ohne "android." gibt's nen Fehler!
            }
        }
    } // <-- end of inner class DetailsActivity

}









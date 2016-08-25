package sk.jmurin.android.testy.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sk.jmurin.android.testy.entities.Question;
import sk.jmurin.android.testy.entities.Test;

/**
 * Created by Janco1 on 26. 5. 2015.
 */
public class InstanciaTestu implements Serializable {

    public final int POCET_ODPOVEDI;
    public final Test test;
    public boolean ucenieSelected;
    public final int[][] idckaUloh;
    public int aktUlohaIdx = 0;
    public int uspesnych;
    public int pocetMinusBodov;
    public final int[][] odpovedeOrder;
    public final List<Integer> cisla = new ArrayList<>();
    public int[][] zaskrtnute;
    public boolean[] ohodnotene;

    public InstanciaTestu(Test test, List<Question> otazky) {// stats je komplet zoznam statistiky pre kazdu ulohu, z databazy
        this.test=test;
        POCET_ODPOVEDI = test.getQuestions().get(0).getAnswers().size();
        // nainicializujeme si poradove cisla
        for (int i = 0; i < POCET_ODPOVEDI; i++) {
            cisla.add(i);
        }

        Collections.shuffle(otazky);
        // zapiseme si idcka otazok ktore chceme
        idckaUloh = new int[otazky.size()][4];//0- idcko otazky v teste, 1- povodna statistika, 2- aktualna statistika, 3- idcko otazky v db
        odpovedeOrder = new int[otazky.size()][POCET_ODPOVEDI]; // nahodne rozmiesame kazdu z odpovedi
        zaskrtnute = new int[otazky.size()][POCET_ODPOVEDI]; // aby sme vedeli ktore sme ako zaskrtli
        ohodnotene = new boolean[otazky.size()];  // aby sme vedeli ktore ulohy su uz ohodnotene aby sa hned vykreslili
        for (int i = 0; i < otazky.size(); i++) {
            idckaUloh[i][0] = otazky.get(i).getTestQuestionIndex();
            idckaUloh[i][1] = test.getQuestions().get(otazky.get(i).getTestQuestionIndex()).getStat();
            idckaUloh[i][3] = test.getQuestions().get(otazky.get(i).getTestQuestionIndex()).getDbID();
            Collections.shuffle(cisla); // pomiesame poradie odpovedi
            for (int j = 0; j < POCET_ODPOVEDI; j++) {
                odpovedeOrder[i][j] = cisla.get(j);
            }
        }
    }

    public boolean isUcenieSelected() {
        return ucenieSelected;
    }

    public void setUcenieSelected(boolean ucenieSelected) {
        this.ucenieSelected = ucenieSelected;
        if (ucenieSelected) {
            Arrays.fill(ohodnotene, true);
            System.out.println("ucenie selected: " + Arrays.toString(ohodnotene));
        }
    }

    public int getOhodnotenych() {
        int pocet = 0;
        for (int i = 0; i < ohodnotene.length; i++) {
            if (ohodnotene[i]) {
                pocet++;
            }
        }
        return pocet;

    }

    public int[] getZleZodpovedane() {
        // iba z tych co su ohodnotene chceme vybrat tie idcka, kde sme zle odpovedali
        System.out.println("getZleZodpovedane: ");
        System.out.println("ohodnotenych: " + getOhodnotenych() + "  uspesnych: " + uspesnych);
        int[] zle = new int[getOhodnotenych() - uspesnych];
        int pocet = 0;
        for (int i = 0; i < idckaUloh.length; i++) {
            if (ohodnotene[i]) {
                if (idckaUloh[i][1] >= 0) {
                    if (idckaUloh[i][1] > idckaUloh[i][2]) {
                        // zle sme odpovedali, znizil sa rating
                        //System.out.println("pocet=" + pocet);
                        zle[pocet] = idckaUloh[i][0];
                        pocet++;
                        //System.out.println("zle zodpovedane: ["+i+"] "+idckaUloh[i][1]+">"+idckaUloh[i][2]);
                    }
                } else {
                    if (idckaUloh[i][1] == idckaUloh[i][2]) {
                        // obidve idcka su -1, takze znova zla odpoved
                        zle[pocet] = idckaUloh[i][0];
                        pocet++;
                        //System.out.println("zle zodpovedane: ["+i+"] "+idckaUloh[i][1]+"=="+idckaUloh[i][2]);
                    }
                }
            }
        }
        if (pocet != zle.length) {
            throw new RuntimeException("pocet zlych sa nerovna poctu zlych");
        }
        return zle;
    }

    public int[] getPribudlo() {
        int[] pribudlo = new int[5];
        int zelenych = 0;
        int oranzovych = 0;
        int zltych = 0;
        int bielych = 0;
        int cervenych = 0;
        int povodnaStatistika;
        int aktualnaStatistika;
        for (int i = 1; i < idckaUloh.length; i++) {
            povodnaStatistika = idckaUloh[i][1];
            aktualnaStatistika = idckaUloh[i][2];
            // ci je uspesna
            if (povodnaStatistika < aktualnaStatistika) {
                // uloha bola urcite uspesna
                switch (idckaUloh[i][1]) {
                    case 0:
                        zltych++;
                        bielych--;
                        break;
                    case 1:
                        oranzovych++;
                        zltych--;
                        break;
                    case 2:
                        zelenych++;
                        oranzovych--;
                        break;
                    case -1:
                        zltych++;
                        cervenych--;
                        break;
                    default:
                        ;
                }
            } else {
                // povodna >= aktualna statistika
                // ak bola povodna -1 alebo 3, tak sa mozu rovnat povodna a aktualna
                // urcite sa nemozu rovnat ak bola povodna 0, 1, 2
                switch (povodnaStatistika) {
                    case 0:
                        cervenych++;
                        bielych--;
                        break;
                    case 1: // biela pribudla
                        bielych++;
                        zltych--;
                        break;
                    case 2:
                        oranzovych--;
                        zltych++;
                        break;
                    case 3:
                        if (povodnaStatistika == aktualnaStatistika) {
                            // uloha bola urcite uspesna, iba sa 3 nezmenila na 4
                        } else {
                            //uloha bola urcite neuspesna lebo povodna > aktualna
                            zelenych--;
                            oranzovych++;
                        }
                        break;
                    case -1:
                        // predosla bola -1 a aktualna == povodna, lebo mensia nemoze byt a vyssia je v prvom ife
                        // teda znova je uloha neuspesna a pocet cervenych sa nezvysil
                        // z cervenej sa preslo znova do cervenej
                        break;
                    default:
                        ;
                }
            }
        }
        pribudlo[0] = cervenych;
        pribudlo[1] = bielych;
        pribudlo[2] = zltych;
        pribudlo[3] = oranzovych;
        pribudlo[4] = zelenych;

        return pribudlo;
    }
}

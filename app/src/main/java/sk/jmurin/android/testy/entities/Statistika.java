package sk.jmurin.android.testy.entities;

import java.io.Serializable;

/**
 * Created by Janco1 on 29. 5. 2015.
 */
public class Statistika implements Serializable {

    public int vyriesenych;
    public int uspesnych;
    public int uspesnost;
    public int minusBodov;
    public int[] pribudlo=new int[5];
    public int[] zleZodpovedane;
    public boolean test;
    public boolean trening;
    public boolean ucenie;
    public String serverStatistika;

}

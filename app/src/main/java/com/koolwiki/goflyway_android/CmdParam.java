package com.koolwiki.goflyway_android;

/**
 * Created by shutup on 16/7/19.
 */
public class CmdParam {

    public static String LOCALADDR = "-l";
    public static String REMOTEADDR = "-up";
    public static String KEY = "-k";
    public static String CRYPT = "-crypt";
    public static String UNDERLAY = "-U";
    public static String ACL = "-acl";


    public String localaddr;
    public String remoteaddr;
    public String key;
    public String crypt;
    public String U;
    public String A;
    public String mtu;
    public String sndwnd;
    public String revwnd;
    public boolean nocmp;

    public Boolean isBasicOk(){
        if (remoteaddr.equalsIgnoreCase("")){
            return false;
        }
        if (key.equalsIgnoreCase("")) {
            return false;
        }

        return true;
    }

}

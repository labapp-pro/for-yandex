package pro.labapp.yandex;



import pro.labapp.yandex.drawer.SimpleMenu;

/**
 * Created by LabApp on 11.04.2018.
 */

public class Config {

    public static String CONFIG_URL = "";

    public static final boolean HIDE_DRAWER = false;

    public static boolean USE_HARDCODED_CONFIG = false;

    public static void configureMenu(SimpleMenu menu, ConfigParser.CallBack callback){


        callback.configLoaded(false);
    }

}

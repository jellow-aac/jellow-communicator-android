package com.dsource.idc.jellowintl.factories;

import android.content.Context;

import com.dsource.idc.jellowintl.utility.PackageRemoverAsync;
import com.dsource.idc.jellowintl.utility.SessionManager;

import java.io.File;
import java.util.Arrays;

import static com.dsource.idc.jellowintl.utility.SessionManager.LangMap;

public class LanguageFactory {

    public static String getCurrentLanguageCode(Context context){

        String localeCode = getCurrentLocaleCode(context);
        return getLanguageCode(localeCode);
    }

    public static String getCurrentLocaleCode(Context context){
        SessionManager sessionManager = new SessionManager(context);
        return sessionManager.getLanguage();
    }

    /**
     * This function returns the Language Code of the given language.
     * @param langCode
     * @return Language codes (01- English (India), 02- English (US), 03- English (UK), 04- Hindi,
     * 05- Marathi, 06- Bengali (India), 07- English (AU), 08- Spanish, 09- Tamil, 10- German,
     * 11- English (South Africa), 12- French, 13- Telugu (India), 14- Gujarati (India),
     * 15- none, 16- Bengali (Bangladesh), 17- English (Nigeria), 18- Punjabi (India)),
     * 20- Kannada (India), 21- Malayalam (India), 22- Marathi with TTS, 23- Oriya (India),
     * 24- Urdu (India), 25- Maithili (India), 28- Serbian (Serbia)
     ***/
    public static String getLanguageCode(String langCode){
        switch (langCode){
            case SessionManager.ENG_IN:
                return "01";
            case SessionManager.ENG_US:
                return "02";
            case SessionManager.ENG_UK:
                return "03";
            case SessionManager.HI_IN:
                return "04";
            case SessionManager.MR_IN:
                return "05";
            case SessionManager.BN_IN:
            case SessionManager.BE_IN:
                return "06";
            case SessionManager.ENG_AU:
                return  "07";
            case SessionManager.ES_ES:
                return "08";
            case SessionManager.TA_IN:
                return  "09";
            case SessionManager.DE_DE:
                return  "10";
            case SessionManager.FR_FR:
                return  "12";
            case SessionManager.TE_IN:
                return  "13";
            case SessionManager.GU_IN:
                return  "14";
            case SessionManager.BN_BD:
                return  "16";
            case SessionManager.ENG_NG:
                return  "17";
            case SessionManager.PA_IN:
                return  "18";
            case SessionManager.KHA_IN:
                return  "19";
            case SessionManager.KN_IN:
                return  "20";
            case SessionManager.ML_IN:
                return  "21";
            case SessionManager.MR_TTS:
                return "22";
            case SessionManager.OR_IN:
                return "23";
            case SessionManager.UR_IN:
                return "24";
            case SessionManager.MAI_IN:
                return "25";
            case SessionManager.SR_RS:
                return "28";
            default:
                return null;
        }
    }

    public static void deleteOldLanguagePackagesInBackground(Context context){
        new PackageRemoverAsync().execute(context);
    }

    public static String[] getAvailableLanguages(){
        String[] languages = new String[LangMap.size()];
        LangMap.keySet().toArray(languages);
        Arrays.sort(languages);
        return languages;
    }

    public static boolean isMarathiPackageAvailable(Context context){
        try{
            File file = context.getDir(SessionManager.UNIVERSAL_PACKAGE, Context.MODE_PRIVATE);
            File packageZip = new File(file.getPath(),SessionManager.MR_IN+".zip");
            File packageZipTemp = new File(file.getPath(),SessionManager.MR_IN+".zip.temp");
            File audioFolder = new File(context.getDir(PathFactory.UNIVERSAL_FOLDER,
                    Context.MODE_PRIVATE).getAbsolutePath()+"/"+PathFactory.AUDIO_FOLDER);
            return !packageZip.exists() && !packageZipTemp.exists() && audioFolder.exists();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkIfVerbiageAvailableForTheLanguage(Context context, String language){
        try{
            File file = context.getDir(SessionManager.UNIVERSAL_PACKAGE, Context.MODE_PRIVATE);
            File verbiageFile = new File(file.getPath(),language+".json");
            return verbiageFile.exists();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return false;
    }

    /** If zip is exist then data is not completely extracted from zip.
     *  If zip does is exist then data is extracted from zip successfully, the zip file is deleted.
     *  So when zip is exist we can consider the data is unavailable and when zip does not exist then
     *  data is available.
     *  **/
    public static boolean isLanguageDataAvailable(Context context){
        try{
            File file = context.getDir(SessionManager.UNIVERSAL_PACKAGE, Context.MODE_PRIVATE);
            File packageZip = new File(file.getPath(),SessionManager.UNIVERSAL_PACKAGE+".zip");
            File packageZipTemp = new File(file.getPath(),SessionManager.UNIVERSAL_PACKAGE+".zip.temp");
            File drawableFolder = new File(context.getDir(PathFactory.UNIVERSAL_FOLDER,
                    Context.MODE_PRIVATE).getAbsolutePath()+"/"+PathFactory.DRAWABLE_FOLDER);
            return !packageZip.exists() && !packageZipTemp.exists() && drawableFolder.exists();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return false;
    }
}

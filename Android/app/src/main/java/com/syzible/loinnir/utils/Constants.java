package com.syzible.loinnir.utils;

import java.nio.charset.Charset;

/**
 * Created by ed on 19/05/2017.
 */

public class Constants {
    public static final boolean DEV_MODE = true;

    public static String getCountyFileName(String county) {
        return county.toLowerCase().replace(" ", "_")
                .replace("á", "a").replace("é", "e")
                .replace("í", "i").replace("ó", "o").replace("ú", "u");
    }
}

package com.syzible.loinnir.utils;

/**
 * Created by ed on 17/05/2017.
 */

public class EmojiUtils {
    public static final int HAPPY_EMOJI = 0x1F603;

    public static String getEmoji(int unicode){
        return new String(Character.toChars(unicode));
    }
}

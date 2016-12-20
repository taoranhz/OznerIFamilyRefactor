/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ozner.cup.Chat.EaseUI.utils;

import android.content.Context;
import android.net.Uri;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.ImageSpan;

import com.ozner.cup.Chat.EaseUI.controller.EaseUI;
import com.ozner.cup.Chat.EaseUI.domain.EaseEmojicon;
import com.ozner.cup.Chat.EaseUI.model.EaseDefaultEmojiconDatas;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EaseSmileUtils {
    public static final String DELETE_KEY = "em_delete_delete_expression";

    public static final String f_static_000 = "[f_000]";

    public static final String f_static_001 = "[f_001]";
    public static final String f_static_002 = "[f_002]";
    public static final String f_static_003 = "[f_003]";
    public static final String f_static_004 = "[f_004]";
    public static final String f_static_005 = "[f_005]";
    public static final String f_static_006 = "[f_006]";
    public static final String f_static_007 = "[f_007]";
    public static final String f_static_008 = "[f_008]";
    public static final String f_static_009 = "[f_009]";

    public static final String f_static_010 = "[f_010]";
    public static final String f_static_011 = "[f_011]";
    public static final String f_static_012 = "[f_012]";
    public static final String f_static_013 = "[f_013]";
    public static final String f_static_014 = "[f_014]";
    public static final String f_static_015 = "[f_015]";
    public static final String f_static_016 = "[f_016]";
    public static final String f_static_017 = "[f_017]";
    public static final String f_static_018 = "[f_018]";
    public static final String f_static_019 = "[f_019]";

    public static final String f_static_020 = "[f_020]";
    public static final String f_static_021 = "[f_021]";
    public static final String f_static_022 = "[f_022]";
    public static final String f_static_023 = "[f_023]";
    public static final String f_static_024 = "[f_024]";
    public static final String f_static_025 = "[f_025]";
    public static final String f_static_026 = "[f_026]";
    public static final String f_static_027 = "[f_027]";
    public static final String f_static_028 = "[f_028]";
    public static final String f_static_029 = "[f_029]";

    public static final String f_static_030 = "[f_030]";
    public static final String f_static_031 = "[f_031]";
    public static final String f_static_032 = "[f_032]";
    public static final String f_static_033 = "[f_033]";
    public static final String f_static_034 = "[f_034]";
    public static final String f_static_035 = "[f_035]";
    public static final String f_static_036 = "[f_036]";
    public static final String f_static_037 = "[f_037]";
    public static final String f_static_038 = "[f_038]";
    public static final String f_static_039 = "[f_039]";

    public static final String f_static_040 = "[f_040]";
    public static final String f_static_041 = "[f_041]";
    public static final String f_static_042 = "[f_042]";
    public static final String f_static_043 = "[f_043]";
    public static final String f_static_044 = "[f_044]";
    public static final String f_static_045 = "[f_045]";
    public static final String f_static_046 = "[f_046]";
    public static final String f_static_047 = "[f_047]";
    public static final String f_static_048 = "[f_048]";
    public static final String f_static_049 = "[f_049]";

    public static final String f_static_050 = "[f_050]";
    public static final String f_static_051 = "[f_051]";
    public static final String f_static_052 = "[f_052]";
    public static final String f_static_053 = "[f_053]";
    public static final String f_static_054 = "[f_054]";
    public static final String f_static_055 = "[f_055]";
    public static final String f_static_056 = "[f_056]";
    public static final String f_static_057 = "[f_057]";
    public static final String f_static_058 = "[f_058]";
    public static final String f_static_059 = "[f_059]";

    public static final String f_static_060 = "[f_060]";
    public static final String f_static_061 = "[f_061]";
    public static final String f_static_062 = "[f_062]";
    public static final String f_static_063 = "[f_063]";
    public static final String f_static_064 = "[f_064]";
    public static final String f_static_065 = "[f_065]";
    public static final String f_static_066 = "[f_066]";
    public static final String f_static_067 = "[f_067]";
    public static final String f_static_068 = "[f_068]";
    public static final String f_static_069 = "[f_069]";

    public static final String f_static_070 = "[f_070]";
    public static final String f_static_071 = "[f_071]";
    public static final String f_static_072 = "[f_072]";
    public static final String f_static_073 = "[f_073]";
    public static final String f_static_074 = "[f_074]";
    public static final String f_static_075 = "[f_075]";
    public static final String f_static_076 = "[f_076]";
    public static final String f_static_077 = "[f_077]";
    public static final String f_static_078 = "[f_078]";
    public static final String f_static_079 = "[f_079]";

    public static final String f_static_080 = "[f_080]";
    public static final String f_static_081 = "[f_081]";
    public static final String f_static_082 = "[f_082]";
    public static final String f_static_083 = "[f_083]";
    public static final String f_static_084 = "[f_084]";
    public static final String f_static_085 = "[f_085]";
    public static final String f_static_086 = "[f_086]";
    public static final String f_static_087 = "[f_087]";
    public static final String f_static_088 = "[f_088]";
    public static final String f_static_089 = "[f_089]";

    public static final String f_static_090 = "[f_090]";
    public static final String f_static_091 = "[f_091]";
    public static final String f_static_092 = "[f_092]";
    public static final String f_static_093 = "[f_093]";
    public static final String f_static_094 = "[f_094]";
    public static final String f_static_095 = "[f_095]";
    public static final String f_static_096 = "[f_096]";
    public static final String f_static_097 = "[f_097]";
    public static final String f_static_098 = "[f_098]";
    public static final String f_static_099 = "[f_099]";

    public static final String f_static_100 = "[f_100]";
    public static final String f_static_101 = "[f_101]";
    public static final String f_static_102 = "[f_102]";
    public static final String f_static_103 = "[f_103]";
    public static final String f_static_104 = "[f_104]";

    private static final Factory spannableFactory = Factory
            .getInstance();

    private static final Map<Pattern, Object> emoticons = new HashMap<Pattern, Object>();


    static {
        EaseEmojicon[] emojicons = EaseDefaultEmojiconDatas.getData();
        for (EaseEmojicon emojicon : emojicons) {
            addPattern(emojicon.getEmojiText(), emojicon.getIcon());
        }
        EaseUI.EaseEmojiconInfoProvider emojiconInfoProvider = EaseUI.getInstance().getEmojiconInfoProvider();
        if (emojiconInfoProvider != null && emojiconInfoProvider.getTextEmojiconMapping() != null) {
            for (Entry<String, Object> entry : emojiconInfoProvider.getTextEmojiconMapping().entrySet()) {
                addPattern(entry.getKey(), entry.getValue());
            }
        }

    }

    public static Set<Entry<Pattern, Object>> getEmoticonEntrySet() {
        return emoticons.entrySet();
    }

    /**
     * add text and icon to the map
     * @param emojiText-- text of emoji
     * @param icon -- resource id or local path
     */
    public static void addPattern(String emojiText, Object icon) {
        emoticons.put(Pattern.compile(Pattern.quote(emojiText)), icon);
    }


    /**
     * replace existing spannable with smiles
     * @param context
     * @param spannable
     * @return
     */
    public static boolean addSmiles(Context context, Spannable spannable) {
        boolean hasChanges = false;
        for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                boolean set = true;
                for (ImageSpan span : spannable.getSpans(matcher.start(),
                        matcher.end(), ImageSpan.class))
                    if (spannable.getSpanStart(span) >= matcher.start()
                            && spannable.getSpanEnd(span) <= matcher.end())
                        spannable.removeSpan(span);
                    else {
                        set = false;
                        break;
                    }
                if (set) {
                    hasChanges = true;
                    Object value = entry.getValue();
                    if (value instanceof String && !((String) value).startsWith("http")) {
                        File file = new File((String) value);
                        if (!file.exists() || file.isDirectory()) {
                            return false;
                        }
                        spannable.setSpan(new ImageSpan(context, Uri.fromFile(file)),
                                matcher.start(), matcher.end(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        spannable.setSpan(new ImageSpan(context, (Integer) value),
                                matcher.start(), matcher.end(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

        return hasChanges;
    }

    public static Spannable getSmiledText(Context context, CharSequence text) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addSmiles(context, spannable);
        return spannable;
    }

    public static boolean containsKey(String key) {
        boolean b = false;
        for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(key);
            if (matcher.find()) {
                b = true;
                break;
            }
        }

        return b;
    }

    public static int getSmilesSize() {
        return emoticons.size();
    }


}

/*
 * VYSOKÉ UČENÍ TECHNICKÉ V BRNĚ BRNO UNIVERSITY OF TECHNOLOGY
 *
 * FAKULTA INFORMAČNÍCH TECHNOLOGIÍ
 *
 * Baklářská práce
 *
 * Generátor konečných automatů z grafického popisu pro jazyk VHDL
 *
 * Author: Martin Janyš
 *
 * Brno 2013
 */
package cz.fsmgen.gui.utils;

/**
 *
 * Utils for work with Strings.
 * 
 * @author Martin Janyš
 */
public class StringUtils {

    public static String capitalize(String s) {
        Character c = s.charAt(0);

        return Character.toUpperCase(c) + s.substring(1);
    }

    public static String dropHtmlTags(String value) {
        String regex = "\\<.*?\\>";

        value = value.replaceAll("\\<hr/?\\>", "/");
        value = value.toString().replaceAll(regex, "");
        value = value.trim();
        return value;
    }

    public static Object htmlEncode(String string) {
        // & → &amp;
        string = string.replaceAll("&", "&amp;");
        // < → &lt;
        string = string.replaceAll("<", "&lt;");
        // > → &gt;
        string = string.replaceAll(">", "&gt;");
        // ' → &#39;
        string = string.replaceAll("'", "&#39;");
        // " → &quot;
        string = string.replaceAll("\"", "&#34;");
        
        return string;
    }
}

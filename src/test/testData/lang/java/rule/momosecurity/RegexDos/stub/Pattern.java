package java.util.regex;

public class Pattern
{

    public static Pattern compile(String regex) {
        return new Pattern(regex, 0);
    }

    public static Pattern compile(String regex, int flags) {
        return new Pattern(regex, flags);
    }

    public static boolean matches(String regex, CharSequence input) {
        return true;
    }

    private Pattern(String p, int f) {
        // fake
    }
}

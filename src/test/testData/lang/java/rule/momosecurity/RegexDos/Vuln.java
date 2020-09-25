import java.util.regex.Pattern;

public class Vuln {

    private final String exponentialRegex = "([a-z]+)*"; // warning

    void bar(String id) {
        String phoneRegex = "13\\d{9}"; // no warning

        Pattern.compile(phoneRegex);

        // Pattern.compile method test
        Pattern.compile(<error descr="MomoSec: 发现regexDos风险">exponentialRegex</error>);

        // Pattern.matches method test
        Pattern.matches(<error descr="MomoSec: 发现regexDos风险">exponentialRegex</error>, id);
    }
}
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class TestIsSqliCareExpression {
    public void foo() {
        ArrayList<String> list = new ArrayList<>();
        list.add("foo");

        StringUtils.join(list, "-");
    }
}
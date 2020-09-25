import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;

public class Vuln {
    // BAD: Disables the internal response splitting verification
    private final DefaultHttpHeaders badHeaders = <error descr="MomoSec: Netty响应拆分攻击">new DefaultHttpHeaders(false)</error>;

    // GOOD: Verifies headers passed don't contain CRLF characters
    private final DefaultHttpHeaders goodHeaders = new DefaultHttpHeaders();

    // BAD: Disables the internal response splitting verification
    private final DefaultHttpResponse badResponse = <error descr="MomoSec: Netty响应拆分攻击">new DefaultHttpResponse(null, null, false)</error>;

    // GOOD: Verifies headers passed don't contain CRLF characters
    private final DefaultHttpResponse goodResponse = new DefaultHttpResponse(null, null);
}
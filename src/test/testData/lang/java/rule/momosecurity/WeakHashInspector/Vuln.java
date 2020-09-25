import org.apache.commons.codec.digest.DigestUtils;

import java.security.MessageDigest;

public class Vuln {
    public void foo() {
        <warning descr="MomoSec: 发现脆弱的消息摘要算法">MessageDigest.getInstance("MD5")</warning>;
        MessageDigest.getInstance("SHA-256");

        <warning descr="MomoSec: 发现脆弱的消息摘要算法">DigestUtils.getDigest("MD2")</warning>;
        <warning descr="MomoSec: 发现脆弱的消息摘要算法">DigestUtils.getDigest("MD5")</warning>;

        DigestUtils md5 = <warning descr="MomoSec: 发现脆弱的消息摘要算法">new DigestUtils("MD5")</warning>;

        <warning descr="MomoSec: 发现脆弱的消息摘要算法">DigestUtils.md5("foo".getBytes())</warning>;
        <warning descr="MomoSec: 发现脆弱的消息摘要算法">DigestUtils.md5Hex("foo".getBytes())</warning>;

        DigestUtils.sha1("foo".getBytes());
    }
}
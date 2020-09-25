import javax.crypto.Cipher;

public class Vuln {
    public void foo() {
        <error descr="MomoSec: 发现过时的加密标准">Cipher.getInstance("DES/ECB/NoPadding")</error>;
        <error descr="MomoSec: 发现过时的加密标准">Cipher.getInstance("DESede/CBC/PKCS5Padding")</error>;
        Cipher.getInstance("AES/ECB/NoPadding");
    }
}
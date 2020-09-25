import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

public class DefaultTypingVuln {
    public void vuln1() {
        ObjectMapper mapper = new ObjectMapper();
        <error descr="MomoSec: 发现Jackson反序列化风险">mapper.enableDefaultTyping()</error>;
    }

    public void vuln2() {
        ObjectMapper mapper = new ObjectMapper();
        <error descr="MomoSec: 发现Jackson反序列化风险">mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT)</error>;
    }

    public void novuln() {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
    }
}
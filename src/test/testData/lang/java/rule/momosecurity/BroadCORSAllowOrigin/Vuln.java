import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class Vuln {

    protected void doGet(HttpServletResponse resp)  {
        <error descr="MomoSec: 宽泛的 Allowed Origin 设置">resp.setHeader("Access-Control-Allow-Origin", "*")</error>;
        <error descr="MomoSec: 宽泛的 Allowed Origin 设置">resp.addHeader("Access-Control-Allow-Origin", "*")</error>;
    }

    <error descr="MomoSec: 宽泛的 Allowed Origin 设置">@CrossOrigin</error>
    public void annotation_1() {

    }

    <error descr="MomoSec: 宽泛的 Allowed Origin 设置">@CrossOrigin(origins = "*")</error>
    public void annotation_2() {

    }

    <error descr="MomoSec: 宽泛的 Allowed Origin 设置">@CrossOrigin(value = "*")</error>
    public void annotation_3() {

    }

    public void cors_config() {
        CorsConfiguration config = new CorsConfiguration();
        <error descr="MomoSec: 宽泛的 Allowed Origin 设置">config.addAllowedOrigin("*")</error>;
        <error descr="MomoSec: 宽泛的 Allowed Origin 设置">config.addAllowedOrigin(CorsConfiguration.ALL)</error>;
        <error descr="MomoSec: 宽泛的 Allowed Origin 设置">config.applyPermitDefaultValues()</error>;
    }

    public static class Insecure implements WebMvcConfigurer {

        public void addCorsMappings(CorsRegistry registry) {
            <error descr="MomoSec: 宽泛的 Allowed Origin 设置">registry.addMapping("/**")
                    .allowedOrigins("*")</error>;
        }
    }

    public static class Insecure2 implements WebMvcConfigurer {
        public void addCorsMappings(CorsRegistry registry) {
            <error descr="MomoSec: 宽泛的 Allowed Origin 设置">registry.addMapping("/**")</error>;
        }
    }
}
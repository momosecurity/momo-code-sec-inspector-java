
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

public class Vuln extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        <error descr="MomoSec: 发现Spring会话攻击风险">http
                .sessionManagement()
                .sessionFixation()
                .none()</error>;
    }
}

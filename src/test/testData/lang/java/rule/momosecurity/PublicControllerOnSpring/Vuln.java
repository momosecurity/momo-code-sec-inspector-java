import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Vuln {

    @RequestMapping("/novuln")
    public void novuln() {
        return ;
    }

    <warning descr="Momo 1021: \"@RequestMapping\" 方法应当为 \"public\"">@RequestMapping("/a")
    private</warning> void a() {
        return ;
    }

    <warning descr="Momo 1021: \"@RequestMapping\" 方法应当为 \"public\"">@GetMapping("/b")
    private</warning> void b() {
        return ;
    }

}
package sample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Api {

    @GetMapping
    public String authed() {
        return "authed";
    }
}

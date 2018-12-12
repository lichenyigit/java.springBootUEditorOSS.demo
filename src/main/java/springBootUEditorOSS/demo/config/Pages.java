package springBootUEditorOSS.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;

@Controller
public class Pages {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${base.url}")
    private String baseUrl = "";

    public void addBaseModel(Model model) {
        model.addAttribute("baseUrl", baseUrl);
    }

    @RequestMapping(value = {"/index.html", "/"})
    public String login(Model model) {
        addBaseModel(model);
        return "index";
    }

}

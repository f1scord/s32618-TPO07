package pl.edu.pja.s32618.tpo07;

import com.google.googlejavaformat.java.FormatterException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class CodeController {

    private final CodeFormatter codeFormatter;
    private final CodeStorageService codeStorageService;

    public CodeController(CodeFormatter codeFormatter, CodeStorageService codeStorageService) {
        this.codeFormatter = codeFormatter;
        this.codeStorageService = codeStorageService;
    }

    @GetMapping("/code")
    public String showCodeForm(Model model) {
        CodeForm form = new CodeForm();
        String original = (String) model.getAttribute("originalCode");
        if (original != null) {
            form.setCode(original);
        }
        model.addAttribute("codeForm", form);
        return "code";
    }

    @PostMapping("/saveCode")
    public RedirectView saveCode(@ModelAttribute CodeForm codeForm, RedirectAttributes redirectAttributes) {
        String code = codeForm.getCode();
        try {
            String formatted = codeFormatter.format(code);
            redirectAttributes.addFlashAttribute("originalCode", code);
            redirectAttributes.addFlashAttribute("formattedCode", formatted);
        } catch (FormatterException e) {
            redirectAttributes.addFlashAttribute("originalCode", code);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return new RedirectView("/code", true, false);
    }

    @PostMapping("/api/format")
    @ResponseBody
    public Map<String, String> formatApi(@RequestParam String code) {
        try {
            String formatted = codeFormatter.format(code);
            return Map.of("formatted", formatted);
        } catch (FormatterException e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/findCode")
    public String findCode(@RequestParam String ID, Model model) {
        Optional<SavedCode> result = codeStorageService.findById(ID);
        if (result.isPresent()) {
            model.addAttribute("savedCode", result.get());
        } else {
            model.addAttribute("error", "Code not found");
        }
        return "findCode";
    }

    @PostMapping("/saveForLater")
    public RedirectView saveForLater(
            @RequestParam String id,
            @RequestParam(defaultValue = "0") int days,
            @RequestParam(defaultValue = "0") int hours,
            @RequestParam(defaultValue = "0") int minutes,
            @RequestParam(defaultValue = "0") int seconds,
            @RequestParam String formattedCode,
            @RequestParam(required = false, defaultValue = "") String originalCode,
            RedirectAttributes redirectAttributes) {

        long totalSeconds = (long) days * 86400 + (long) hours * 3600 + (long) minutes * 60 + seconds;

        if (totalSeconds < 10) {
            redirectAttributes.addFlashAttribute("error", "Duration must be at least 10 seconds");
            redirectAttributes.addFlashAttribute("originalCode", originalCode);
            redirectAttributes.addFlashAttribute("formattedCode", formattedCode);
            return new RedirectView("/code", true, false);
        }
        if (totalSeconds > 90L * 86400) {
            redirectAttributes.addFlashAttribute("error", "Duration must not exceed 90 days");
            redirectAttributes.addFlashAttribute("originalCode", originalCode);
            redirectAttributes.addFlashAttribute("formattedCode", formattedCode);
            return new RedirectView("/code", true, false);
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(totalSeconds);
        SavedCode savedCode = new SavedCode(id, originalCode, formattedCode, expiresAt);
        codeStorageService.save(savedCode);

        String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
        return new RedirectView("/findCode?ID=" + encodedId, true, false);
    }
}

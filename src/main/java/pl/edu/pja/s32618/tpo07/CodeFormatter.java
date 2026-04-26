package pl.edu.pja.s32618.tpo07;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import org.springframework.stereotype.Component;

@Component
public class CodeFormatter {

    public String format(String code) throws FormatterException {
        return new Formatter().formatSource(code);
    }
}

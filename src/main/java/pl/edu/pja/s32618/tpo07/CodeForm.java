package pl.edu.pja.s32618.tpo07;

public class CodeForm {

    private String code;

    public CodeForm() {
        this.code = "";
    }

    public CodeForm(String code) {
        this.code = code != null ? code : "";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

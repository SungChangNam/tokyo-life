package dev.sung.tokyo_life.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TagForm {

    @NotBlank(message = "태그 이름을 입력해주세요.")
    @Size(max = 50, message = "태그 이름은 50자 이내로 입력해주세요.")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
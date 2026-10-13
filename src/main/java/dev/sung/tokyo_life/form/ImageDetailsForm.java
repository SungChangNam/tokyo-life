package dev.sung.tokyo_life.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ImageDetailsForm {

    @Size(max = 255, message = "이미지 설명은 255자 이내로 입력해주세요.")
    private String altText;

    @NotNull(message = "표시 순서를 입력해주세요.")
    @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다.")
    private Integer displayOrder;

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
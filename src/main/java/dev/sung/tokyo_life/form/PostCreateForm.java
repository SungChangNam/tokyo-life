package dev.sung.tokyo_life.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class PostCreateForm {

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 150, message = "제목은 150자 이내로 입력해주세요.")
    private String title;

    @Size(max = 300, message = "요약은 300자 이내로 입력해주세요.")
    private String summary;

    @Positive(message = "올바른 카테고리를 선택해주세요.")
    private Long categoryId;

    private String content;

    @NotBlank(message = "공개 상태를 선택해주세요.")
    @Pattern(
            regexp = "DRAFT|PUBLISHED",
            message = "올바른 공개 상태를 선택해주세요."
    )
    private String status = "DRAFT";

    @AssertTrue(message = "공개하려면 카테고리와 본문을 입력해주세요.")
    public boolean isPublishRequirementsMet() {

        if (!"PUBLISHED".equals(status)) {
            return true;
        }

        return categoryId != null
                && content != null
                && !content.isBlank();
    }

    @Size(max = 20, message = "태그는 최대 20개까지 선택해주세요.")
    private List<
            @NotNull(message = "올바른 태그를 선택해주세요.")
            @Positive(message = "올바른 태그를 선택해주세요.")
                    Long
            > tagIds = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }

}
package dev.sung.tokyo_life.controller;

import dev.sung.tokyo_life.service.TagService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import dev.sung.tokyo_life.form.TagForm;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/admin/tags")
public class AdminTagController {

    private final TagService tagService;

    public AdminTagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tags", tagService.findAllForAdmin());

        return "admin/tags/list";
    }

    @GetMapping("/new")
    public String newTag(Model model) {
        model.addAttribute("tagForm", new TagForm());

        return "admin/tags/new";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("tagForm") TagForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "admin/tags/new";
        }

        try {
            tagService.create(form);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("tag.create.invalid", e.getMessage());
            return "admin/tags/new";
        } catch (DuplicateKeyException e) {
            bindingResult.rejectValue(
                    "name",
                    "tag.name.duplicate",
                    "이미 등록된 태그 이름입니다."
            );
            return "admin/tags/new";
        }

        redirectAttributes.addFlashAttribute(
                "message",
                "태그를 등록했습니다."
        );

        return "redirect:/admin/tags";
    }


    @GetMapping("/{id}/edit")
    public String edit(
            @PathVariable("id") Long id,
            Model model
    ) {
        model.addAttribute("tagForm", tagService.getEditForm(id));
        model.addAttribute("tagId", id);

        return "admin/tags/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("tagForm") TagForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        model.addAttribute("tagId", id);

        if (bindingResult.hasErrors()) {
            return "admin/tags/edit";
        }

        try {
            tagService.update(id, form);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("tag.update.invalid", e.getMessage());
            return "admin/tags/edit";
        } catch (DuplicateKeyException e) {
            bindingResult.rejectValue(
                    "name",
                    "tag.name.duplicate",
                    "이미 등록된 태그 이름입니다."
            );
            return "admin/tags/edit";
        }

        redirectAttributes.addFlashAttribute(
                "message",
                "태그를 수정했습니다."
        );

        return "redirect:/admin/tags";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        tagService.delete(id);

        redirectAttributes.addFlashAttribute(
                "message",
                "태그를 삭제했습니다."
        );

        return "redirect:/admin/tags";
    }
}
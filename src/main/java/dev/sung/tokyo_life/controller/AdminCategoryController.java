package dev.sung.tokyo_life.controller;

import dev.sung.tokyo_life.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import dev.sung.tokyo_life.form.CategoryForm;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.dao.DataIntegrityViolationException;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute(
                "categories",
                categoryService.findAllForAdmin()
        );

        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String newCategory(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());

        return "admin/categories/new";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("categoryForm") CategoryForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/new";
        }

        try {
            categoryService.create(form);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("category.create.invalid", e.getMessage());
            return "admin/categories/new";
        } catch (DuplicateKeyException e) {
            bindingResult.rejectValue(
                    "name",
                    "category.name.duplicate",
                    "이미 등록된 카테고리 이름입니다."
            );
            return "admin/categories/new";
        }

        redirectAttributes.addFlashAttribute(
                "message",
                "카테고리를 등록했습니다."
        );

        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String edit(
            @PathVariable("id") Long id,
            Model model
    ) {
        model.addAttribute("categoryForm", categoryService.getEditForm(id));
        model.addAttribute("categoryId", id);

        return "admin/categories/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("categoryForm") CategoryForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        model.addAttribute("categoryId", id);

        if (bindingResult.hasErrors()) {
            return "admin/categories/edit";
        }

        try {
            categoryService.update(id, form);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("category.update.invalid", e.getMessage());
            return "admin/categories/edit";
        } catch (DuplicateKeyException e) {
            bindingResult.rejectValue(
                    "name",
                    "category.name.duplicate",
                    "이미 등록된 카테고리 이름입니다."
            );
            return "admin/categories/edit";
        }

        redirectAttributes.addFlashAttribute(
                "message",
                "카테고리를 수정했습니다."
        );

        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            categoryService.delete(id);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "카테고리를 삭제했습니다."
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "categoryError",
                    e.getMessage()
            );
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute(
                    "categoryError",
                    "연결된 데이터가 있어 카테고리를 삭제할 수 없습니다."
            );
        }

        return "redirect:/admin/categories";
    }


}
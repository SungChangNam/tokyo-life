package dev.sung.tokyo_life.controller;

import dev.sung.tokyo_life.form.PostCreateForm;
import dev.sung.tokyo_life.service.CategoryService;
import dev.sung.tokyo_life.service.PostService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;

@Controller
@RequestMapping("/admin/posts")
public class AdminPostController {

    private final CategoryService categoryService;
    private final PostService postService;

    public AdminPostController(
            CategoryService categoryService,
            PostService postService
    ) {
        this.categoryService = categoryService;
        this.postService = postService;
    }

    @GetMapping("/new")
    public String newPost(Model model) {
        model.addAttribute("postForm", new PostCreateForm());
        model.addAttribute("categories", categoryService.findAll());

        return "admin/posts/new";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("postForm") PostCreateForm form,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // 입력값 검증에 실패하면 작성 화면을 다시 표시
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/posts/new";
        }

        try {
            postService.create(form, principal.getName());
        } catch (IllegalArgumentException e) {
            // Service에서 확인한 업무 규칙 오류를 화면에 표시
            bindingResult.reject("post.create.invalid", e.getMessage());

            model.addAttribute("categories", categoryService.findAll());
            return "admin/posts/new";
        }

        redirectAttributes.addFlashAttribute(
                "message",
                "게시글을 저장했습니다."
        );

        return "redirect:/admin";
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("posts", postService.findAllForAdmin());

        return "admin/posts/list";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, Model model) {

        model.addAttribute("postForm", postService.getEditForm(id));
        model.addAttribute("postId", id);
        model.addAttribute("categories", categoryService.findAll());

        return "admin/posts/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("postForm") PostCreateForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        // 오류가 나서 수정 화면으로 돌아갈 때도 필요한 데이터
        model.addAttribute("postId", id);
        model.addAttribute("categories", categoryService.findAll());

        if (bindingResult.hasErrors()) {
            return "admin/posts/edit";
        }

        try {
            postService.update(id, form);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("post.update.invalid", e.getMessage());
            return "admin/posts/edit";
        }

        redirectAttributes.addFlashAttribute(
                "message",
                "게시글을 수정했습니다."
        );

        return "redirect:/admin/posts";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        postService.delete(id);

        redirectAttributes.addFlashAttribute(
                "message",
                "게시글을 삭제했습니다."
        );

        return "redirect:/admin/posts";
    }
}
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
import dev.sung.tokyo_life.service.PostImageService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import dev.sung.tokyo_life.model.PostImage;
import dev.sung.tokyo_life.service.ImageStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;


import java.security.Principal;

@Controller
@RequestMapping("/admin/posts")
public class AdminPostController {



    private final CategoryService categoryService;
    private final PostService postService;
    private final PostImageService postImageService;
    private final ImageStorageService imageStorageService;

    public AdminPostController(
            CategoryService categoryService,
            PostService postService,
            PostImageService postImageService,
            ImageStorageService imageStorageService
    ) {
        this.categoryService = categoryService;
        this.postService = postService;
        this.postImageService = postImageService;
        this.imageStorageService = imageStorageService;
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
        model.addAttribute("images", postImageService.findByPostId(id));

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
        model.addAttribute("images", postImageService.findByPostId(id));

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

    @PostMapping("/{id}/images")
    public String uploadImage(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            RedirectAttributes redirectAttributes
    ) {

        try {
            postImageService.upload(id, file, altText);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "이미지를 업로드했습니다."
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "imageError",
                    e.getMessage()
            );
        }

        return "redirect:/admin/posts/" + id + "/edit";
    }

    @GetMapping("/{id}/images/{imageId}")
    public ResponseEntity<Resource> image(
            @PathVariable("id") Long id,
            @PathVariable("imageId") Long imageId
    ) {
        PostImage image = postImageService.findByPostId(id).stream()
                .filter(item -> item.id().equals(imageId))
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND)
                );

        Resource resource = imageStorageService.load(image.storageKey());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }

    @PostMapping("/{id}/images/{imageId}/delete")
    public String deleteImage(
            @PathVariable("id") Long id,
            @PathVariable("imageId") Long imageId,
            RedirectAttributes redirectAttributes
    ) {
        postImageService.delete(id, imageId);

        redirectAttributes.addFlashAttribute(
                "message",
                "이미지 등록을 삭제했습니다."
        );

        return "redirect:/admin/posts/" + id + "/edit";
    }

    @PostMapping("/{id}/images/{imageId}/cover")
    public String changeCover(
            @PathVariable("id") Long id,
            @PathVariable("imageId") Long imageId,
            RedirectAttributes redirectAttributes
    ) {
        postImageService.changeCover(id, imageId);

        redirectAttributes.addFlashAttribute(
                "message",
                "대표 이미지를 변경했습니다."
        );

        return "redirect:/admin/posts/" + id + "/edit";
    }
}
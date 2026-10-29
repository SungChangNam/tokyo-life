package dev.sung.tokyo_life.controller;

import dev.sung.tokyo_life.service.CategoryService;
import dev.sung.tokyo_life.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final PostService postService;
    private final CategoryService categoryService;

    public HomeController(
            PostService postService,
            CategoryService categoryService
    ) {
        this.postService = postService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(value = "categoryId", required = false)
            Long categoryId,
            @RequestParam(value = "keyword", required = false)
            String keyword,
            Model model
    ) {
        String normalizedKeyword = keyword == null
                ? ""
                : keyword.strip();

        model.addAttribute(
                "posts",
                postService.searchPublishedList(
                        categoryId,
                        normalizedKeyword
                )
        );
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("keyword", normalizedKeyword);

        return "home";
    }
}
package dev.sung.tokyo_life.controller;

import dev.sung.tokyo_life.model.Post;
import dev.sung.tokyo_life.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Post post = postService.findById(id);

        model.addAttribute("post", post);

        return "post";
    }
}
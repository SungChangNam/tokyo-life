package dev.sung.tokyo_life.controller;

import dev.sung.tokyo_life.model.Post;
import dev.sung.tokyo_life.model.PostImage;
import dev.sung.tokyo_life.service.ImageStorageService;
import dev.sung.tokyo_life.service.PostImageService;
import dev.sung.tokyo_life.service.PostService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PostController {

    private final PostService postService;
    private final PostImageService postImageService;
    private final ImageStorageService imageStorageService;

    public PostController(
            PostService postService,
            PostImageService postImageService,
            ImageStorageService imageStorageService
    ) {
        this.postService = postService;
        this.postImageService = postImageService;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping("/posts/{id}")
    public String detail(
            @PathVariable("id") Long id,
            Model model
    ) {
        Post post = postService.findById(id);

        model.addAttribute("post", post);
        model.addAttribute(
                "images",
                postImageService.findByPublishedPostId(id)
        );

        return "post";
    }

    @GetMapping("/posts/{id}/images/{imageId}")
    public ResponseEntity<Resource> image(
            @PathVariable("id") Long id,
            @PathVariable("imageId") Long imageId
    ) {
        PostImage image = postImageService
                .findByPublishedPostId(id)
                .stream()
                .filter(item -> item.id().equals(imageId))
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND)
                );

        Resource resource =
                imageStorageService.load(image.storageKey());

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(image.contentType())
                )
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }
}
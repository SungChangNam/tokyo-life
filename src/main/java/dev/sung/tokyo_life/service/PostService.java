package dev.sung.tokyo_life.service;

import dev.sung.tokyo_life.model.Post;
import dev.sung.tokyo_life.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Post findById(Long id) {
        Post post = postRepository.findById(id);

        if (post == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return post;
    }
}
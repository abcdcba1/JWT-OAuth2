package com.example.aaa.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.aaa.domain.Article;
import com.example.aaa.dto.AddArticleRequest;
import com.example.aaa.dto.UpdateArticleRequest;
import com.example.aaa.repository.BlogRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor // finalまたは＠NotNullが付いたクリエーター追加
@Service
public class BlogService {

    private final BlogRepository blogRepository;

    // ブログ投稿メソッド
    public Article save(AddArticleRequest request) {
        return blogRepository.save(request.toEntity());
    }

    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    public Article findById(long id) {
        return blogRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("not found" + id));
    }

    public void delete(long id) {
        blogRepository.deleteById(id);
    }

    @Transactional
    public Article update(long id, UpdateArticleRequest request) {
        Article article =  blogRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("not found" + id));

        article.update(request.getTitle(), request.getContent());

        return article;
    }
}

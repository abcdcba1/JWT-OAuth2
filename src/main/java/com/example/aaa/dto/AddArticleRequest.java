package com.example.aaa.dto;

import com.example.aaa.domain.Article;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor //全てのfieldを入力されるクリエーター追加
@Getter
public class AddArticleRequest {
    private String title;
    private String content;

    public Article toEntity() {
        return Article.builder()
            .title(title)
            .content(content)
            .build();
    }
}

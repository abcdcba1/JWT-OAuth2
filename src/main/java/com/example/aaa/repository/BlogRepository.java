package com.example.aaa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aaa.domain.Article;

public interface BlogRepository extends JpaRepository<Article, Long> {

}

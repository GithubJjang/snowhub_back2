package com.snowhub.server.dummy.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
public class Board {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id; // 게시글 번호

    // 한명의 user가 여러개의 Board 작성 가능하다.
    @ManyToOne(fetch = FetchType.LAZY)
    private User user; // 사용자

    private String title; // 제목
    @Column(columnDefinition = "LONGTEXT" )
    private String content; // 내용

    private String category; // 카테고리

    @CreationTimestamp
    private Timestamp createDate; // 작성일
    
    private int count; // 게시글 조회

    // DAO

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class DAO {
        public DAO(){

        }
        // 명시적으로 선언을 안하니 @Builder가 안먹힘.
        private int id; // 게시글 번호
        private User user; // 사용자
        private String title; // 제목
        private String content; // 내용
        private String category; // 카테고리
        private Timestamp createDate; // 작성일
        private int count; // 게시글 조회

        private String writer;


    }

}

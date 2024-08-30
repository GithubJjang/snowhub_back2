package com.snowhub.server.dummy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne // Comment:reply = N:1
    private Reply reply;

    @Column(columnDefinition = "LONGTEXT") // DB에 글자 충분히 저장할 수 있게.
    private String comment;

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class DAO{

        public DAO(){}
        // 변수를 명시적으로 선언을 하지 않으면 @Builder를 사용할 수 없다.
        private int id;
        private Reply reply;
        private String comment;

    }
}

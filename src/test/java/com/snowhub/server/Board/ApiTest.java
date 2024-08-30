package com.snowhub.server.Board;




import com.querydsl.jpa.impl.JPAQueryFactory;

import com.snowhub.server.dummy.service.BoardService;
import com.snowhub.server.dummy.service.ReplyService;
import com.snowhub.server.dummy.service.TmpBoardService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@Transactional
public class ApiTest {

    @Autowired
    BoardService boardService;
    @Autowired
    ReplyService replyService;
    @Autowired
    TmpBoardService tmpBoardService;

    @Autowired
    private EntityManager em;

    JPAQueryFactory queryFactory;

    /*
        // 1. from절의 서브쿼리 불가!(인라인 뷰 불가!)
        SELECT * FROM board b,(
        SELECT id FROM board
        ORDER BY id DESC
        LIMIT 199990,16
                ) i_board
        WHERE b.id = i_board.id
        ;

        // 2. join절로 수정
        SELECT * FROM board b
        INNER JOIN (
        SELECT id FROM board
        ORDER BY id DESC
        LIMIT 199990,16
        ) i_board
        ON b.id = i_board.id
        ;
     */





}



package com.snowhub.server.dummy.controller;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snowhub.server.dummy.dto.board.BoardParam;
import com.snowhub.server.dummy.dto.board.TmpBoardParam;

import com.snowhub.server.dummy.model.TmpBoard;
import com.snowhub.server.dummy.repository.BoardRepo;
import com.snowhub.server.dummy.service.BoardService;
import com.snowhub.server.dummy.service.ReplyService;
import com.snowhub.server.dummy.service.TmpBoardService;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import static com.snowhub.server.dummy.model.QBoard.board;


@Slf4j
@AllArgsConstructor // <- JPAQueryFactory를 빈등록이 된 줄 알고 가져오려고 함. 그래서 어노테이션이 아닌 생성자 주입방식을 적용.
@RestController
@Tag(name = "게시글 관련 API", description = "게시글 작성/현재 작성중인 게시글 임시저장 및 불러오기/전체게시글 불러오기/게시글 상세보기/")
public class BoardController {

    // 생성자 주입 방식.
    private final BoardService boardService;
    private final TmpBoardService tmpBoardService;

    @Operation(summary = "게시글 작성", description = "현재 게시글을 작성합니다.")
    @PostMapping("/board/write")
    public ResponseEntity<?> boardWrite(@Valid @RequestBody BoardParam boardDTO,
                                        HttpServletRequest request) {
        log.debug("{}{}",boardDTO.getTitle(),boardDTO.getContent());

        return boardService.saveBoard(boardDTO,request);
    }

    // - 게시글 임시 저장하기
    @Operation(summary = "게시글 임시저장",description = "현재 작성중인 게시글을 임시저장합니다.")
    @PostMapping("/board/tmp")
    public ResponseEntity<?> boardTmpSave(@RequestBody TmpBoardParam tmpBoardParam, HttpServletRequest request){
        // 임시저장을 하는데, 굳이 검사를 할 필요가 없다.
        System.out.println("content:"+tmpBoardParam.getContent());
        return tmpBoardService.saveTmpBoard(tmpBoardParam,request);

    }

    // - 임시 저장된 게시글 불러오기
    @Operation(summary = "임시저장 게시글 불러오기",description = "임시저장한 게시글을 불러옵니다.")
    @GetMapping("/board/tmp")
    public TmpBoard getTmpBoard(HttpServletRequest request){
        return tmpBoardService.getTmpBoard(request);
    }

    // - 게시글 불러오기
    @Operation(summary = "전체 게시글 불러오기",description = "전체 게시글을 불러옵니다.") //<- 수정요구 DISK I/O 발생 많이 한다.
    @GetMapping("/board/list")
    public ResponseEntity<?> boardList(@RequestParam int page,@RequestParam String category){

        String json = new Gson().toJson(boardService.getMultiBoard(category,page));

        return ResponseEntity.ok(json);

    }

    // /board/detail/{id} <- @PathVariable String id로 받기
    @Operation(summary = "게시글 상세보기",description = "하나의 게시글을 선택하여, 자세한 내용을 불러옵니다.( 게시글+댓글 )")
    @GetMapping("/board/detail")
    public ResponseEntity<?> boardDetail(@RequestParam(name = "id") int boardId,
                                         @RequestParam(name = "number")int number
                                         ){
        log.info("1./board/detail");
        return ResponseEntity.ok(boardService.getSingleBoard(boardId,number));// body T에는 board Entity가 들어가야.
    }

    private BooleanExpression categoryuEq(String categoryCond) {

        return categoryCond.equals("all") ? null : board.category.eq(categoryCond);
    }
}

/*
1. RequestBody는 multipart/form-data를 json으로 전송해도 excpetion 발생 -> @RequestParam("image") MultipartFile file
   Resolved [org.springframework.web.HttpMediaTypeNotSupportedException: Content type 'multipart/form-data;boundary=----WebKitFormBoundary7bFMK5q5ziAvA0tq;charset=UTF-8' not supported ]

*/


// API: /board/list
   /*


        log.info("Here1");
        System.out.println("category: "+category);
        Page<Board> pages;
        if(category.equals("all")){
            // 카테고리 구분없이 id 오름차순으로 가져오기 <- 가장 최신순으로
            pages = boardRepo.findAll(PageRequest.of(page,16, Sort.by("createDate").descending()));
        }
        else{
            // 카테고리 별로 + id 오름차순으로 가져오기 <- 가장 최신순으로
            pages = boardRepo.findByCategory(category,PageRequest.of(page,16, Sort.by("createDate").descending()));
        }
        log.info("Here2");
        List<BoardFetcher> returnBoards = new ArrayList<>(pages.stream()
                .map(   // param = Board, return BoadListDTO
                        (e) -> BoardFetcher.builder()
                                .id(e.getId())
                                .category(e.getCategory())
                                .title(e.getTitle())
                                .writer(e.getUser().getDisplayName())
                                .count(e.getCount())
                                .createDate(e.getCreateDate())
                                .build()
                )
                .toList())
        ;
        log.info("Here3");
        // 전체 페이지 개수
        BoardFetcher pageSizeEntity = new BoardFetcher();
        pageSizeEntity.setId(pages.getTotalPages());
        pageSizeEntity.setTitle(pages.getTotalPages()+"");

        returnBoards.add(pageSizeEntity);

        String json = new Gson().toJson(returnBoards);

        return ResponseEntity.ok(json);

     */

// API: /board/detail
/*
        //BoardFetcher fetchBoard = boardService.getBoard(boardId);// 이거 안하면 Json으로 인해서 StackOverFlow 발생



        /*
        queryFactory = new JPAQueryFactory(em);

        // board를 1건으로 줄인 후, user와 join
        Board findBoard = queryFactory.selectFrom(board)
                .join(board.user,user).fetchJoin()
                .where(board.id.eq(boardId))
                .fetchOne();

        BoardFetcher boardFetcher =  BoardFetcher.builder()
                .id(findBoard.getId())
                //.count(board.getCount()) 추후에 추가할 예정
                .title(findBoard.getTitle())
                .content(findBoard.getContent())
                .writer(findBoard.getUser().getDisplayName())
                .category(findBoard.getCategory())
                .createDate(findBoard.getCreateDate())
                .build();

        System.out.println("==========(1)");
        List<ReplyFetcher> fetchReplies = replyService.getReply(boardId);// 마찬가지이유.
        System.out.println("==========(2)");

        // Board와 Reply를 동시에 반환을 한다.
        BoardWithReplies boardWithRepliesDTO = new BoardWithReplies();
        boardWithRepliesDTO.setBoardDTO(boardFetcher);
        boardWithRepliesDTO.setReplyDTO(fetchReplies);

        if(number==1){
            boardService.updateCount(boardId);
        }

*/

/*
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails userDetails = (UserDetails)principal;
        String username = userDetails.getUsername();
        String password = userDetails.getPassword();

        System.out.println("user:"+username);
        System.out.println("password:"+password);

        /////
        System.out.println("logout");
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails userDetails = (UserDetails)principal;
        String username = userDetails.getUsername();
        String password = userDetails.getPassword();

        System.out.println("user:"+username);
        System.out.println("password:"+password);

        SecurityContextLogoutHandler s = new SecurityContextLogoutHandler();
        s.logout(request,response, SecurityContextHolder.getContext().getAuthentication());

        System.out.println(1);
        SecurityContextLogoutHandler s2 = new SecurityContextLogoutHandler();
        s2.logout(request,response, SecurityContextHolder.getContext().getAuthentication());
        System.out.println(2);
        Object principal2 = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(SecurityContextHolder.getContext().getAuthentication()==null){
            System.out.println("Principal Null");
        }
        UserDetails userDetails2 = (UserDetails)principal2;
        String username2 = userDetails2.getUsername();
        String password2 = userDetails2.getPassword();
        System.out.println("Print");
        System.out.println("user2:"+username2);
        System.out.println("password2:"+password2);
 */
package com.snowhub.server.dummy.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snowhub.server.dummy.dto.board.BoardParam;
import com.snowhub.server.dummy.dto.reply.BoardWithReplies;
import com.snowhub.server.dummy.model.Board;
import com.snowhub.server.dummy.model.Reply;
import com.snowhub.server.dummy.model.User;
import com.snowhub.server.dummy.repository.BoardRepo;
import com.snowhub.server.dummy.repository.UserRepo;
import com.snowhub.server.dummy.module.Firebase;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.snowhub.server.dummy.model.QBoard.board;
import static com.snowhub.server.dummy.model.QUser.user;

@Slf4j
//@AllArgsConstructor JPAQueryFactory는 빈 등록 되지 않음.
@Service
public class BoardService {

    private BoardRepo boardRepo;
    private UserRepo userRepo; // userRepo는 외부 빈 -> 순환참조 x
    private JPAQueryFactory queryFactory;

    private ReplyService replyService;

    public BoardService(BoardRepo boardRepo,
                        UserRepo userRepo,
                        ReplyService replyService,
                        EntityManager em
    ){
        this.boardRepo=boardRepo;
        this.userRepo=userRepo;
        this.queryFactory=new JPAQueryFactory(em);
        this.replyService=replyService;
    }



    @Transactional
    public ResponseEntity<?> saveBoard(BoardParam boardDTO, HttpServletRequest request){

        log.info("BoardService/saveBoard Start");
        // 1. 이미 앞서 검증이 완료된 Board (by boardDTO), 만약 에러 발생시 GlobalExceptionHandler에서 캐치 후 에러 리턴.

        Board board = new Board();

        board.setTitle(boardDTO.getTitle());
        board.setContent(boardDTO.getContent());
        board.setCategory(boardDTO.getCategory());


        Firebase firebase = Firebase.getInstance();
        String userEmail = firebase.getEmail(request);


        User findUser = Optional.ofNullable(userRepo.findByEmail(userEmail)).orElseThrow(
                ()-> new NullPointerException("The user is not registered!")
        );// 혹시나 없는 사용자면 error 발생. 검증을 한번더?

        // 3. user 찾은 후, 저장하기
        findUser.addBoards(board); // user dirty checking???
        board.setUser(findUser);

        boardRepo.save(board);

        log.info("BoardService/saveBoard Completed");
        return ResponseEntity.ok(HttpStatus.OK);
    }



    @Transactional
    public List<Board.DAO> getMultiBoard(String category,int page){

        List<Board> result = boardRepo.pagination(category,page);

        List<Board.DAO> returnBoards = new ArrayList<>(result.stream()
                .map(   // param = Board, return BoadListDTO
                        (e) -> Board.DAO.builder()
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

        // Querydsl을 이용한 DTO로 값을 받는 경우, user엔티티의 username 추출하기가 매우 번거롭다. <- 추후 고려
        long getBoardSize = queryFactory
                .select(board.id.count())
                .from(board)
                .where(categoryEq(category))
                .fetchOne()
                ;
        int getPageSize = Math.toIntExact(getBoardSize/16)+1;


        Board.DAO pageSizeEntity = new Board.DAO();
        pageSizeEntity.setId(Math.toIntExact(getPageSize));
        pageSizeEntity.setTitle(getPageSize+"");

        returnBoards.add(pageSizeEntity);

        return  returnBoards;
    }
    private BooleanExpression categoryEq(String categoryCond) {

        return categoryCond.equals("all") ? null : board.category.eq(categoryCond);
    }

    @Transactional
    public BoardWithReplies getSingleBoard(int boardId,int number)  {
        //BoardFetcher fetchBoard = boardService.getBoard(boardId);// 이거 안하면 Json으로 인해서 StackOverFlow 발생
        log.info("2.getSingleBoard");

        // board를 1건으로 줄인 후, user와 join
        Board findBoard = queryFactory.selectFrom(board)
                .join(board.user,user).fetchJoin()
                .where(board.id.eq(boardId))
                .fetchOne();

        Board.DAO getBoard =  Board.DAO.builder()
                .id(findBoard.getId())
                //.count(board.getCount()) 추후에 추가할 예정
                .title(findBoard.getTitle())
                .content(findBoard.getContent())
                .writer(findBoard.getUser().getDisplayName())
                .category(findBoard.getCategory())
                .createDate(findBoard.getCreateDate())
                .build();

        //System.out.println("==========(1)");
        log.info("3.get Reply at SingleBoard");
        List<Reply.DAO> getReplies = replyService.getReply(boardId);// 마찬가지이유.
        //System.out.println("==========(2)");


        // Board와 Reply를 동시에 반환을 한다.
        BoardWithReplies boardWithRepliesDTO = new BoardWithReplies();
        boardWithRepliesDTO.setBoardDTO(getBoard);
        boardWithRepliesDTO.setReplyDTO(getReplies);

        if(number==1){
            this.updateCount(boardId);
        }


        System.out.println("title: "+boardWithRepliesDTO.getBoardDTO().getTitle());


        return boardWithRepliesDTO;
    }


    @Transactional
    public void updateCount(int boardId){
        // board 가져오기 -> 원본을 변경 -> orig와 변경된 board 서로 compare -> Dirty Checking에 의해서 자동 update
        Board board = boardRepo.findById(boardId).orElseThrow(
                ()-> new NullPointerException("Can't find board:"+boardId)
        );
        board.setCount(board.getCount()+1);

    }


}



// 2. request 헤더에서 토큰 추출 -> email 추출 ->

        /*
        String getToken = request.getHeader("Authorization");
        String bearerToken=null;
        if (getToken != null && getToken.startsWith("Bearer ")) {
            bearerToken = getToken.substring(7); // Extracting the token after "Bearer "
        }

        else{
            throw new RuntimeException("It is not Bearer Token");
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        FirebaseToken decode = null;
        // Throw를 할 경우, boardService를 쓰는 모든 컴포넌트가 throw FirebaseException을 throw 해야함.
        try {
            decode =  firebaseAuth.verifyIdToken(bearerToken);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        String userEmail = decode.getEmail();
        */

        /*
        Optional<Board> optionalBoard = boardRepo.findById(id);
        Board board = optionalBoard.orElseThrow(
                ()-> new NullPointerException("There is no board")
        );

        return BoardFetcher.builder()
                .id(board.getId())
                //.count(board.getCount()) 추후에 추가할 예정
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getUser().getDisplayName()) // User를 조회하는 Query발생
                .category(board.getCategory())
                .createDate(board.getCreateDate())
                .build();

         */




// getBoard 로직
        /*
        Optional<Board> optionalBoard = boardRepo.findById(id);
        Board board = optionalBoard.orElseThrow(
                ()-> new NullPointerException("There is no board")
        );

        return BoardFetcher.builder()
                .id(board.getId())
                //.count(board.getCount()) 추후에 추가할 예정
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getUser().getDisplayName()) // User를 조회하는 Query발생
                .category(board.getCategory())
                .createDate(board.getCreateDate())
                .build();

         */
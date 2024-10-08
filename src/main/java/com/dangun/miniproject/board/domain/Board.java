package com.dangun.miniproject.board.domain;


import com.dangun.miniproject.comment.domain.Comment;
import com.dangun.miniproject.common.domain.BaseEntity;
import com.dangun.miniproject.member.domain.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;


@Entity
@Getter
@NoArgsConstructor
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_status")
    private BoardStatus boardStatus;

    @ManyToOne(fetch = LAZY)
    private Member member;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder
    private Board(String content, Member member, Integer price, BoardStatus boardStatus, String title) {
        this.content = content;
        this.member = member;
        this.price = price;
        this.boardStatus = boardStatus;
        this.title = title;
    }

    // 수정 메서드 추가
    public void updateDetails(String title, String content, Integer price, BoardStatus boardStatus) {
        this.title = title;
        this.content = content;
        this.price = price;
        this.boardStatus = boardStatus;
    }
}

package org.com.stocknote.domain.stockVote.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.com.stocknote.domain.stock.entity.Stock;
import org.com.stocknote.domain.stockVote.service.StockVoteService;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_code", "user_id", "vote_date"})
})
public class StockVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_code")
    private Stock stock;

    private String userId;    // 투표한 사용자 ID
    private LocalDate voteDate;  // 투표 날짜
    private StockVoteService.VoteType voteType;   // 매수/매도


    @Builder
    public StockVote(Stock stock, String userId, StockVoteService.VoteType voteType) {
        this.stock = stock;
        this.userId = userId;
        this.voteType = voteType;
    }

    @PrePersist
    public void prePersist() {
        this.voteDate = LocalDate.now();
    }
}

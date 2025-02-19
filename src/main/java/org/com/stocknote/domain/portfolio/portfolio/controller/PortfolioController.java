package org.com.stocknote.domain.portfolio.portfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.com.stocknote.domain.member.entity.Member;
import org.com.stocknote.domain.member.service.MemberService;
import org.com.stocknote.domain.portfolio.portfolio.dto.request.PortfolioPatchRequest;
import org.com.stocknote.domain.portfolio.portfolio.dto.request.PortfolioRequest;
import org.com.stocknote.domain.portfolio.portfolio.dto.response.PortfolioResponse;
import org.com.stocknote.domain.portfolio.portfolio.entity.Portfolio;
import org.com.stocknote.domain.portfolio.portfolio.service.PortfolioService;
import org.com.stocknote.domain.portfolio.portfolioStock.dto.response.PfStockResponse;
import org.com.stocknote.domain.portfolio.portfolioStock.entity.PfStock;
import org.com.stocknote.domain.portfolio.portfolioStock.service.PfStockService;
import org.com.stocknote.domain.stock.entity.Stock;
import org.com.stocknote.global.globalDto.GlobalResponse;
import org.com.stocknote.oauth.entity.PrincipalDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/portfolios")
@Tag(name = "포트폴리오 API", description = "포트폴리오 API")
public class PortfolioController {
  private final PortfolioService portfolioService;

  @GetMapping
  @Operation(summary = "포트폴리오 조회")
  public GlobalResponse<List<PortfolioResponse>> getPortfolioList(
      @AuthenticationPrincipal PrincipalDetails principalDetails
  ) {
    String email = principalDetails.getUsername();
    List<Portfolio> portfolio = portfolioService.getPortfolioList(email);
    List<PortfolioResponse> response =
        portfolio.stream().map(PortfolioResponse::from).collect(Collectors.toList());
    return GlobalResponse.success(response);
  }

// 엘라스틱으로 대체 (Test용 코드)
  @GetMapping("/my")
  public GlobalResponse<PortfolioResponse> getMyPortfolioList(
      @AuthenticationPrincipal PrincipalDetails principalDetails
  ) {
    String email = principalDetails.getUsername();
    Portfolio portfolio = portfolioService.getMyPortfolioList(email);
    PortfolioResponse response = PortfolioResponse.from(portfolio);
    return GlobalResponse.success(response);
  }

  @GetMapping("/{portfolio_no}")
  @Operation(summary = "포트폴리오 조회")
  public GlobalResponse<PortfolioResponse> getPortfolioStock(
      @PathVariable("portfolio_no") Long portfolioNo) {
    Portfolio portfolio = portfolioService.getPortfolio(portfolioNo);
    PortfolioResponse response = PortfolioResponse.from(portfolio);
    return GlobalResponse.success(response);
  }

  @PostMapping
  @Operation(summary = "포트폴리오 추가")
  public GlobalResponse<String> addPortfolio(
      @RequestBody PortfolioRequest portfolioRequest,
      @AuthenticationPrincipal PrincipalDetails principalDetails)
  {
    String email = principalDetails.getUsername();
    portfolioService.save(portfolioRequest, email);
    return GlobalResponse.success("PortfolioList post");
  }

  @PatchMapping("/{portfolio_no}")
  @Operation(summary = "포트폴리오 수정")
  public GlobalResponse<String> updatePortfolio(@PathVariable("portfolio_no") Long portfolioNo,
      @Valid @RequestBody PortfolioPatchRequest request) {
    portfolioService.update(portfolioNo, request);
    return GlobalResponse.success("Portfolio updated successfully");
  }

  @DeleteMapping("/{portfolio_no}")
  @Operation(summary = "포트폴리오 삭제")
  public GlobalResponse<String> deletePortfolio(@PathVariable("portfolio_no") Long portfolioNo) {
    portfolioService.delete(portfolioNo);
    return GlobalResponse.success("Portfolio deleted successfully");
  }

//  @PostMapping("/search-stocks")
//  public GlobalResponse<List<StockTempResponse>> searchStocks(
//      @RequestBody Map<String, String> body) {
//    String keyword = body.get("keyword");
//    List<Stock> stockList = pfStockService.searchStocks(keyword);
//    List<StockTempResponse> response =
//        stockList.stream().map(StockTempResponse::new).collect(Collectors.toList());
//    return GlobalResponse.success(response);
//  }
}

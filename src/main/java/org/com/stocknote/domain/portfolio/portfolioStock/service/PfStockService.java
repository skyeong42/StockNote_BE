package org.com.stocknote.domain.portfolio.portfolioStock.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.stocknote.domain.portfolio.portfolio.entity.Portfolio;
import org.com.stocknote.domain.portfolio.portfolio.repository.PortfolioRepository;
import org.com.stocknote.domain.portfolio.portfolio.service.PortfolioService;
import org.com.stocknote.domain.portfolio.portfolioStock.dto.request.PfStockPatchRequest;
import org.com.stocknote.domain.portfolio.portfolioStock.dto.request.PfStockRequest;
import org.com.stocknote.domain.portfolio.portfolioStock.entity.PfStock;
import org.com.stocknote.domain.portfolio.portfolioStock.repository.PfStockRepository;
import org.com.stocknote.domain.stock.dto.response.StockPriceResponse;
import org.com.stocknote.domain.stock.entity.Stock;
import org.com.stocknote.domain.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class PfStockService {
  private final PfStockRepository pfStockRepository;
  private final PortfolioService portfolioService;
  private final TempStockService stockService;

  // 임시
  private final StockRepository stockRepository;
  private final PortfolioRepository portfolioRepository;

  public List<PfStock> getStockList(Long portfolioNo) {
    List<PfStock> pfStockList = pfStockRepository.findByPortfolioId(portfolioNo);
    pfStockList.forEach(pfStock -> {
      Stock stock = pfStock.getStock();
      StockPriceResponse currentPrice = stockService.getStockPrice(stock.getCode());
      int currentPriceInt = Integer.parseInt(currentPrice.getOutput().getStck_prpr());
      pfStock.setCurrentPrice(currentPriceInt);
    });
    return pfStockList;
  }

  @Transactional
  public PfStock savePfStock(Long portfolioNo, PfStockRequest pfStockRequest) {
    Portfolio portfolio = portfolioService.getPortfolio(portfolioNo);

    StockPriceResponse currentPrice = stockService.getStockPrice(pfStockRequest.getStockCode());
    int currentPriceInt = Integer.parseInt(currentPrice.getOutput().getStck_prpr());

    Stock stock = stockRepository.findByCode(pfStockRequest.getStockCode()).orElse(null);

    PfStock pfStock = PfStock.builder().portfolio(portfolio).stock(stock)
        .pfstockCount(pfStockRequest.getPfstockCount())
        .pfstockPrice(pfStockRequest.getPfstockPrice())
        .pfstockTotalPrice(pfStockRequest.getPfstockPrice() * pfStockRequest.getPfstockCount())
        .currentPrice(currentPriceInt).build();
    return pfStockRepository.save(pfStock);
  }

  public PfStock savePfStock(PfStock pfStock) {
    return pfStockRepository.save(pfStock);
  }

  public void deletePfStock(Long pfStockNo) {
    pfStockRepository.deleteById(pfStockNo);
  }

  @Transactional
  public void buyPfStock(Long portfolioNo, Long pfStockNo,
      PfStockPatchRequest pfStockPatchRequest) {
    Portfolio portfolio = portfolioService.getPortfolio(portfolioNo);
    PfStock pfStock = pfStockRepository.findById(pfStockNo).orElse(null);

    int quantity = pfStock.getPfstockCount();
    int totalPrice = pfStock.getPfstockPrice() * quantity;

    int buyQuantity = pfStockPatchRequest.getPfstockCount();
    int buyTotalPrice = pfStockPatchRequest.getPfstockPrice() * buyQuantity;

    quantity += buyQuantity;
    totalPrice += buyTotalPrice;

    int avgPrice = totalPrice / quantity;

    pfStock.setPfstockCount(quantity);
    pfStock.setPfstockTotalPrice(totalPrice);
    pfStock.setPfstockPrice(avgPrice);

    portfolio.setCash(portfolio.getCash() - buyTotalPrice);

    portfolioRepository.save(portfolio);
    pfStockRepository.save(pfStock);
  }

  @Transactional
  public void sellPfStock(Long portfolioNo, Long pfStockNo,
      PfStockPatchRequest pfStockPatchRequest) {
    try {
      Portfolio portfolio = portfolioService.getPortfolio(portfolioNo);
      PfStock pfStock = pfStockRepository.findById(pfStockNo)
          .orElseThrow(() -> new RuntimeException("Stock not found"));

      int quantity = pfStock.getPfstockCount();
      int sellQuantity = pfStockPatchRequest.getPfstockCount();
      int sellTotalPrice = pfStockPatchRequest.getPfstockPrice() * sellQuantity;

      // 매도 후 남은 수량 계산
      quantity -= sellQuantity;

      // 현금 업데이트
      portfolio.setCash(portfolio.getCash() + sellTotalPrice);

      if (quantity == 0) {
        // 수량이 0이면 Portfolio에서 PfStock 제거
        portfolio.getPfStockList().remove(pfStock);
        // DB에서 PfStock 삭제
        pfStockRepository.delete(pfStock);
        log.info("Stock with ID {} has been deleted", pfStockNo);
      } else {
        // 수량이 남아있으면 업데이트
        pfStock.setPfstockCount(quantity);
        int remainingTotalPrice = pfStock.getPfstockPrice() * quantity;
        pfStock.setPfstockTotalPrice(remainingTotalPrice);
        pfStockRepository.save(pfStock);
      }

      // Portfolio 저장
      portfolioRepository.save(portfolio);

    } catch (Exception e) {
      log.error("Error in sellPfStock: ", e);
      throw new RuntimeException("Failed to process sell stock operation", e);
    }
  }

  public void update(Long pfStockNo, PfStockPatchRequest request) {
    PfStock pfStock = pfStockRepository.findById(pfStockNo).orElse(null);
    pfStock.setPfstockCount(request.getPfstockCount());
    pfStock.setPfstockPrice(request.getPfstockPrice());
    pfStock.setPfstockTotalPrice(request.getPfstockCount() * request.getPfstockPrice());
    pfStockRepository.save(pfStock);
  }

  // stock service로 이동 예정
  public List<Stock> searchStocks(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return Collections.emptyList();
    }

    String searchKeyword = keyword.toLowerCase();
    return stockRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(searchKeyword,
        searchKeyword);
  }

  // 임시 데이터
  public Stock saveTempStock(Stock stock) {
    return stockRepository.save(stock);
  }

  public Stock getTempStock(String n) {
    return stockRepository.findById(n).orElse(null);
  }

}

package org.com.stocknote.domain.portfolio.portfolioStock.dto.request;

import lombok.Data;

@Data
public class PfStockRequest {
  private int pfstockCount;
  private int pfstockPrice;
  private String stockName;
  private String stockCode;
}


package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import org.springframework.web.client.RestClientException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

public interface PortfolioManager {

  List<AnnualizedReturn> calculateAnnualizedReturnParallel(
      List<PortfolioTrade> portfolioTrades,
      LocalDate endDate, int numThreads) throws InterruptedException,
      StockQuoteServiceException,URISyntaxException;

  //CHECKSTYLE:OFF


  List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate)
      throws StockQuoteServiceException, RestClientException, URISyntaxException
  ;
}


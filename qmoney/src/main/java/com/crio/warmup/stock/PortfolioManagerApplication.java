
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
//import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
//import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
//import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {









  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    ObjectMapper om = getObjectMapper();
    //String contents = new String(Files.readAllBytes(resolveFileFromResources(args[0]).toPath()));
    //PortfolioTrade[] trades = om.readValue(contents, PortfolioTrade[].class);
    //List<String> symbols =
    //Stream.of(trades).map(PortfolioTrade::getSymbol).collect(Collectors.toList());
    List<PortfolioTrade> trades =
        Arrays.asList(om.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class));
    List<TotalReturnsDto> totalReturns = mainReadQuotesHelper(args, trades);
    Collections.sort(totalReturns, TotalReturnsDto.closingComparator);
    List<String> stocks = new ArrayList<>();
    for (TotalReturnsDto trd : totalReturns) {
      stocks.add(trd.getSymbol());
    }

    return stocks;
  }
  
  //public static List<String> sort
  
  public static List<TotalReturnsDto> mainReadQuotesHelper(String[] args, List<PortfolioTrade> trades)
      throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();
    List<TotalReturnsDto> tests = new ArrayList<>();
    for (PortfolioTrade t : trades) {
      String uri =
          prepareUrl(t, LocalDate.parse(args[1]), getToken());
      TiingoCandle[] results = restTemplate.getForObject(uri, TiingoCandle[].class);
      if (results != null) {
        tests.add(new TotalReturnsDto(t.getSymbol(), results[results.length - 1].getClose()));
      }
    }
    return tests;
  }
  
  public static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI())
        .toFile();
  }

  public static ObjectMapper getObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename)
      throws IOException, URISyntaxException {
    List<PortfolioTrade> trades = new ArrayList<>();
    PortfolioTrade[] obj = readTradesFromJsonHelper(filename);
    for (PortfolioTrade i : obj) {
      trades.add(i);
    }
    return trades;
  }

  public static PortfolioTrade[] readTradesFromJsonHelper(String filename)
      throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper om = getObjectMapper();
    PortfolioTrade[] tradesArray = om.readValue(file, PortfolioTrade[].class);
    return tradesArray;
  }
  

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    String file = args[0];
    File contents = resolveFileFromResources(file);    
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);
    // multiple symbols
    return Stream.of(portfolioTrades).map(PortfolioTrade::getSymbol).collect(Collectors.toList());
  }


  public static List<String> debugOutputs() {
    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/qmoney/resources/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@7350471";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "29";


    return Arrays.asList(
        new String[] {valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
            functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace});
  }

  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    return "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="
        + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;
  }

  public static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
 }
  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = prepareUrl(trade, endDate, token);
    RestTemplate restTemplate = new RestTemplate();
    List<Candle> candles = Arrays.asList(restTemplate.getForObject(url, TiingoCandle[].class));
     return candles;
  }


  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {

    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    LocalDate endDate = LocalDate.parse(args[1]);
    PortfolioTrade[] trades = readTradesFromJsonHelper(args[0]);

    for (int i = 0; i < trades.length; i++) {
      annualizedReturns.add(getAnnualizedReturn(trades[i], endDate));
    }

    Collections.sort(annualizedReturns, AnnualizedReturn.returnComparator);

    return annualizedReturns;
  }
  
  
  public static AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate) {

    String tickr = trade.getSymbol();
    LocalDate startDate = trade.getPurchaseDate();

    if (startDate.compareTo(endDate) >= 0) {
      throw new RuntimeException();
    }

    String url = prepareUrl(trade, endDate, getToken());

    RestTemplate restTemplate = new RestTemplate();

    TiingoCandle[] stocks = restTemplate.getForObject(url, TiingoCandle[].class);

    if (stocks != null) {
      TiingoCandle stocksStart = stocks[0];
      TiingoCandle stocksLatest = stocks[stocks.length - 1];

      Double buyPrice = stocksStart.getOpen();
      Double sellPrice = stocksLatest.getClose();

      AnnualizedReturn annualizedReturn =
          calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);

      return annualizedReturn;
    }
    else {
      return new AnnualizedReturn(tickr, Double.NaN, Double.NaN);
    }

    
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {

    // Period period = Period.between(startDate, endDate);
    // double yearsBetween = period.getYears();
    LocalDate startDate = trade.getPurchaseDate();
    double yearsBetween = (double) ChronoUnit.DAYS.between(startDate, endDate) / 365.24;
    
    double totalReturns = (sellPrice - buyPrice) / buyPrice;
    
    double annualizedReturn = Math.pow((1 + totalReturns), (1 / yearsBetween)) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturns);
  }






















  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static RestTemplate restTemplate = new RestTemplate();
  public static PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);

       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  private static String readFileAsString(String file) throws IOException, URISyntaxException {
    //ObjectMapper om = getObjectMapper();
    return new String(Files.readAllBytes(resolveFileFromResources(file).toPath()));
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    //printJsonObject(mainReadQuotes(args));


    //printJsonObject(mainCalculateSingleReturn(args));

    printJsonObject(mainCalculateReturnsAfterRefactor(args));

  }

  public static String getToken() {
    return "65275bf240aea4c348da5b20ef7a38f309c31289";
  }

  
}


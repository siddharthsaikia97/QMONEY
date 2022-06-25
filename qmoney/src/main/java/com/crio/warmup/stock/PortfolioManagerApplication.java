
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
//import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
//import java.time.temporal.ChronoUnit;
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
  
  public static List<TotalReturnsDto> mainReadQuotesHelper(String[] args, List<PortfolioTrade> trades)
      throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();
    List<TotalReturnsDto> tests = new ArrayList<>();
    for (PortfolioTrade t : trades) {
      String uri =
          prepareUrl(t, LocalDate.parse(args[1]), "65275bf240aea4c348da5b20ef7a38f309c31289");
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
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
     List<PortfolioTrade> sym = new ArrayList<>();
    File file = resolveFileFromResources(filename);
    ObjectMapper om = getObjectMapper();
    PortfolioTrade[] obj = om.readValue(file, PortfolioTrade[].class);
    for (PortfolioTrade i : obj) {
      sym.add(i);
    }
    return sym;
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








  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    printJsonObject(mainReadQuotes(args));


  }
}


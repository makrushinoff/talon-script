package com.example.talon.client;

import com.example.talon.dto.BookTicketValue;
import com.example.talon.dto.ResponseDates;
import com.example.talon.dto.Row;
import com.example.talon.dto.SuccessFindTicketDto;
import com.example.talon.dto.TicketReservedDto;
import com.example.talon.player.MusicPlayerUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckTicketClient {

  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;

  private String getToken(String webchsid) {
    return "_ga=GA1.3.1119304809.1697983579; _gid=GA1.3.490916208.1697983579; _ga_3GVV2WPF7F=GS1.3.1697983579.1.0.1697983579.0.0.0; WEBCHSID2=" + webchsid + "; _csrf=8c61e580ada9a4dd699fe773e1a23c431fd83903bf3a0965e0f06021839a3649a%3A2%3A%7Bi%3A0%3Bs%3A5%3A%22_csrf%22%3Bi%3A1%3Bs%3A32%3A%22_GzdcdPuVh2xuP3z-uNfTYFnZ88u9MQ7%22%3B%7D; _identity=f07b034fe8fac09fac28c6e58c7cb63c6f5c43b62f1bc1b4acc3fc58a248a6f1a%3A2%3A%7Bi%3A0%3Bs%3A9%3A%22_identity%22%3Bi%3A1%3Bs%3A17%3A%22%5Bnull%2Cnull%2C28800%5D%22%3B%7D";
  }

  private static final String CSRF = "bODBb8RYnub0IKTxjR59ydmxBR2Q7kEkk4Hkh1Dy1oEzp7sLpzzOk6JIlon4Tk6z9MRLe8S3B0rJudzyab-Htg==";

  public SuccessFindTicketDto checkTicket() {
    URI uri = URI.create("https://eq.hsc.gov.ua/site/freetimes");
    LocalDate startDate = LocalDate.of(2023, 10, 31);
    LocalDate endDate = LocalDate.of(2023, 11, 4);
    MultiValueMap<String, String> map = createBody(startDate);
    HttpEntity<Object> entity = new HttpEntity<>(map, createHttpHeaders("fr4pdfm5qsol19vr55ac3ekbme", startDate, null));
    ResponseEntity<String> response = null;
    ResponseDates responseDates = null;
    LocalDate requestDate = LocalDate.from(startDate);
    Random random = new Random();
    do {
      try {
        response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        List<String> strings = response.getHeaders().get("Set-Cookie");
        String webchsid = this.getCookie(strings);

        log.info("Date: " + requestDate + "; body: " + response.getBody() + "; Returned cookie: " + webchsid);
        responseDates = objectMapper.readValue(response.getBody(), ResponseDates.class);

        if(responseDates.getRows().isEmpty()) {
          if(requestDate.isEqual(endDate)) {
            requestDate = LocalDate.from(startDate);
          } else {
            requestDate = requestDate.plusDays(1);
          }
          entity = new HttpEntity<>(createBody(requestDate), createHttpHeaders(webchsid, requestDate, null));
        } else {
          Row row = responseDates.getRows().stream()
              .sorted(Comparator.comparing(Row::getChtime))
              .findFirst().get();
          return SuccessFindTicketDto.builder()
              .ticketId(row.getId())
              .date(requestDate)
              .webchsid(webchsid)
              .build();
        }
        Thread.sleep(random.nextLong(1000, 4000));
      } catch (HttpServerErrorException e){
        e.printStackTrace();
        HttpHeaders responseHeaders = e.getResponseHeaders();
        List<String> strings = responseHeaders.get("Set-Cookie");
        String webchsid = this.getCookie(strings);
        log.info("Date: " + requestDate + "; body: " + e.getMessage() + "; Returned cookie: " + webchsid);
        entity = new HttpEntity<>(createBody(requestDate), createHttpHeaders(webchsid, requestDate, null));
      } catch (Exception e) {
        e.printStackTrace();
      }

    } while (true);
  }

  private String getCookie(List<String> strings) {
    String[] split = strings.stream()
        .filter(header -> header.contains("WEBCHSID2"))
        .flatMap(header -> Arrays.stream(header.split(";")))
        .filter(header -> header.contains("WEBCHSID2"))
        .findFirst()
        .orElse("").split("=");
    return split[1];
  }

  public MultiValueMap<String, String> createBody(LocalDate date) {
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("office_id", "137");
    map.add("date_of_admission", date.toString());
    map.add("question_id", "55");
    map.add("es_date", "");
    map.add("es_time", "");
    return map;
  }

  public HttpHeaders createHttpHeaders(String webchsid, LocalDate date, String refererUrl) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Cookie", getToken(webchsid));
    headers.set("X-Requested-With", "XMLHttpRequest");
    headers.set("Accept", "*/*");
    headers.set("Accept-Encoding", "gzip, deflate, br");
    headers.set("Sec-Fetch-Dest", "empty");
    headers.set("Referer", Objects.requireNonNullElseGet(refererUrl, () -> "https://eq.hsc.gov.ua/site/step2?chdate=" + date.toString() + "&question_id=55&id_es="));
    headers.set("Connection", "keep-alive");
    headers.set("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    headers.set("X-CSRF-Token", CSRF);
    return headers;
  }

  public HttpHeaders createMultipartHttpHeaders(String webchsid, LocalDate date, String refererUrl) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Cookie", getToken(webchsid));
    headers.set("X-Requested-With", "XMLHttpRequest");
    headers.set("Accept", "*/*");
    headers.set("Accept-Encoding", "gzip, deflate, br");
    headers.set("Sec-Fetch-Dest", "empty");
    headers.set("Referer", Objects.requireNonNullElseGet(refererUrl, () -> "https://eq.hsc.gov.ua/site/step2?chdate=" + date.toString() + "&question_id=55&id_es="));
    headers.set("Connection", "keep-alive");
    headers.set("Content-Type", "multipart/form-data");
    headers.set("X-CSRF-Token", CSRF);
    return headers;
  }

  public TicketReservedDto reserveTicket(SuccessFindTicketDto dto) throws JsonProcessingException, InterruptedException {
    URI uri = URI.create("https://eq.hsc.gov.ua/site/reservecherga");
    MultiValueMap<String, Object> body = this.createBody(dto.getTicketId());
    HttpHeaders headers = createMultipartHttpHeaders(dto.getWebchsid(), dto.getDate(), null);
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
    ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
    System.out.println(dto.getWebchsid());
    if(!response.getStatusCode().is3xxRedirection()) {
      return null;
    }
    String urlToRedirect = response.getHeaders().get("X-Redirect").get(0);
    List<String> strings = response.getHeaders().get("Set-Cookie");
    String webchsid = this.getCookie(strings);
    headers = createHttpHeaders(webchsid, dto.getDate(), null);
    requestEntity = new HttpEntity<>(null, headers);
    ResponseEntity<String> reserveResponse = restTemplate.exchange(urlToRedirect, HttpMethod.GET, requestEntity, String.class);

    String html = reserveResponse.getBody();
    System.out.println(reserveResponse);
    strings = reserveResponse.getHeaders().get("Set-Cookie");
    webchsid = this.getCookie(strings);
    System.out.println(webchsid);
    Document document = Jsoup.parse(html);
    Elements ref = document.select("a");
    System.out.println(ref);
    String toParse = ref.attr("data-params");
    System.out.println(toParse);
    BookTicketValue bookTicketValue = objectMapper.readValue(toParse, BookTicketValue.class);
    System.out.println();
    System.out.println(bookTicketValue);
    Thread.sleep(5000);
    return TicketReservedDto.builder()
        .value(bookTicketValue.getValue())
        .webchsid(webchsid)
        .token(CSRF)
        .referer(urlToRedirect)
        .build();
  }

  public void bookTicket(TicketReservedDto dto) {
    URI uri = URI.create("https://eq.hsc.gov.ua/site/finish");
    MultiValueMap<String, Object> body = this.createBody(dto);
    HttpHeaders headers = createHttpHeaders(dto.getWebchsid(), null, dto.getReferer());
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
    ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
    System.out.println(response);
    MusicPlayerUtil.runMusic();
  }

  private MultiValueMap<String, Object> createBody(Long ticketId) {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("question_id", "55");
    body.add("id_chtime", String.valueOf(ticketId));
    body.add("email", "");
    return body;
  }

  private MultiValueMap<String, Object> createBody(TicketReservedDto dto) {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("_csrf", dto.getToken());
    body.add("value", dto.getValue());
    return body;
  }

}

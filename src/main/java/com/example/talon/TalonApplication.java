package com.example.talon;

import com.example.talon.client.CheckTicketClient;
import com.example.talon.dto.ResponseDates;
import com.example.talon.dto.SuccessFindTicketDto;
import com.example.talon.dto.TicketReservedDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javazoom.jl.player.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class TalonApplication implements CommandLineRunner {

  private final CheckTicketClient client;

  public static void main(String[] args) {
    SpringApplication.run(TalonApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    SuccessFindTicketDto successFindTicketDto = client.checkTicket();
    TicketReservedDto ticketReservedDto = client.reserveTicket(successFindTicketDto);
    client.bookTicket(ticketReservedDto);

  }




}

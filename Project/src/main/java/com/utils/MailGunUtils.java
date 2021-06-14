package com.utils;

import com.Cons;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class MailGunUtils {
   static final String MAILGUN_DOMAIN_NAME = System.getenv(Cons.KEY_MAILGUN_DOMAIN_NAME);
   static final String MAILGUN_API_KEY = System.getenv(Cons.KEY_MAILGUN_API_KEY);

   public static boolean sendHtmlMessage(String receiveEmail, String subject, String body) {
      try {
         HttpResponse<JsonNode> request = Unirest.post("https://api.mailgun.net/v3/" + MAILGUN_DOMAIN_NAME + "/messages")
                 .basicAuth("api", MAILGUN_API_KEY)
                 .queryString("from", "Excited User mailgun@" + MAILGUN_DOMAIN_NAME)
                 .queryString("to", receiveEmail)
                 .queryString("subject", subject)
                 .queryString("html", body)
                 .asJson();
         request.getBody();
         return true;
      } catch (UnirestException e) {
         System.err.println(e.getMessage());
         return false;
      }
   }
}
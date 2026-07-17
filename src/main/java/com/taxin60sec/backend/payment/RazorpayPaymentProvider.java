package com.taxin60sec.backend.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Component
public class RazorpayPaymentProvider implements PaymentService.PaymentProvider {
 private final String keyId,keySecret,webhookSecret; private final RestClient client; private final ObjectMapper json;
 public RazorpayPaymentProvider(@Value("${payment.razorpay.key-id:}")String keyId,@Value("${payment.razorpay.key-secret:}")String keySecret,@Value("${payment.razorpay.webhook-secret:}")String webhookSecret,ObjectMapper json){this.keyId=keyId;this.keySecret=keySecret;this.webhookSecret=webhookSecret;this.json=json;this.client=RestClient.builder().baseUrl("https://api.razorpay.com/v1").defaultHeaders(h->h.setBasicAuth(keyId,keySecret)).build();}
 public String name(){return "razorpay";} public PaymentService.Health health(){return new PaymentService.Health(!keyId.isBlank()&&!keySecret.isBlank(),"Configure payment.razorpay.key-id and key-secret to enable Razorpay.");}
 public PaymentService.PaymentOrder createOrder(PaymentService.PaymentOrderRequest request){if(!health().available())throw new PaymentService.PaymentException("Razorpay is not configured");if(request.amount()==null||request.amount().signum()<=0)throw new PaymentService.PaymentException("Payment amount must be positive");try{Map<String,Object> body=new LinkedHashMap<>();body.put("amount",request.amount().movePointRight(2).longValueExact());body.put("currency",Optional.ofNullable(request.currency()).orElse("INR"));body.put("receipt",request.referenceId());body.put("notes",request.metadata());JsonNode node=json.readTree(client.post().uri("/orders").contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(String.class));return new PaymentService.PaymentOrder(node.path("id").asText(),name(),request.referenceId(),request.amount(),node.path("currency").asText("INR"),node.path("status").asText(),Instant.now());}catch(Exception e){throw new PaymentService.PaymentException("Unable to create Razorpay order",e);}}
 public PaymentService.PaymentEvent verifyWebhook(PaymentService.PaymentWebhook webhook){if(webhook.signature()==null||webhookSecret.isBlank()||!constantTime(hmac(webhook.payload(),webhookSecret),webhook.signature()))throw new PaymentService.PaymentException("Invalid Razorpay webhook signature");try{JsonNode root=json.readTree(webhook.payload());String event=root.path("event").asText();JsonNode payment=root.path("payload").path("payment").path("entity");return new PaymentService.PaymentEvent(name(),payment.path("id").asText(),event,payment.path("id").asText(),Instant.now(),Map.of("status",payment.path("status").asText(),"orderId",payment.path("order_id").asText()));}catch(Exception e){throw new PaymentService.PaymentException("Invalid Razorpay webhook payload",e);}}
 public PaymentService.Refund refund(PaymentService.RefundRequest request){if(!health().available())throw new PaymentService.PaymentException("Razorpay is not configured");try{Map<String,Object> b=new LinkedHashMap<>();if(request.amount()!=null)b.put("amount",request.amount().movePointRight(2).longValueExact());b.put("notes",Map.of("reason",Optional.ofNullable(request.reason()).orElse("")));JsonNode n=json.readTree(client.post().uri("/payments/{id}/refund",request.paymentId()).contentType(MediaType.APPLICATION_JSON).body(b).retrieve().body(String.class));return new PaymentService.Refund(n.path("id").asText(),request.paymentId(),request.amount(),n.path("status").asText(),Instant.now());}catch(Exception e){throw new PaymentService.PaymentException("Unable to refund Razorpay payment",e);}}
 public PaymentService.Invoice invoice(String paymentId){return new PaymentService.Invoice("invoice-"+paymentId,paymentId,null,Instant.now());}
 private String hmac(String payload,String secret){try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new PaymentService.PaymentException("Unable to verify webhook",e);}}private boolean constantTime(String a,String b){return java.security.MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),b.getBytes(StandardCharsets.UTF_8));}
}

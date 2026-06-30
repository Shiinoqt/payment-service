package com.its.gestionepagamentirestclient.service;

import com.its.gestionepagamentirestclient.config.RabbitMQConfig;
import com.its.gestionepagamentirestclient.dto.PaymentReceiptCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReceiptListener {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String CALLER_HEADER      = "caller";
    private static final String METHOD_HEADER      = "method";
    private static final String URI_HEADER         = "uri";

    private final PaymentService paymentService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_RECEIPT_QUEUE)
    public void receiveReceiptCreated(PaymentReceiptCreatedEvent event, Message message) {
        restoreMdc(message);
        try {
            log.info("event=payment_receipt_created orderId={} fileName={}",
                    event.orderId(), event.receiptFileName());
            paymentService.attachReceipt(
                    event.orderId(),
                    event.receiptFileName()
            );
        } finally {
            MDC.clear();
        }
    }

    private void restoreMdc(Message message) {
        MessageProperties props = message.getMessageProperties();
        putIfPresent("correlationId", header(props, CORRELATION_HEADER));
        putIfPresent("caller",  defaultIfBlank(header(props, CALLER_HEADER), "rabbitmq"));
        putIfPresent("method",  defaultIfBlank(header(props, METHOD_HEADER), "AMQP"));
        putIfPresent("uri",     defaultIfBlank(header(props, URI_HEADER), props.getConsumerQueue()));
    }

    private String header(MessageProperties props, String name) {
        Object value = props.getHeaders().get(name);
        return value != null ? value.toString() : null;
    }

    private void putIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) MDC.put(key, value);
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
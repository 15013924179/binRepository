package com.bin.meishikecan.service;

public interface RabbitMQService {
    Boolean sendDirectMessage();

    Boolean sendTopicMessage1();

    Boolean sendTopicMessage2();

    Boolean sendFanoutMessage();
}

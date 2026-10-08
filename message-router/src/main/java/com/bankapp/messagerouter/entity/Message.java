package com.bankapp.messagerouter.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
public class Message implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private String sender;

    private String receiver;

    private LocalDateTime timestamp;

    @Column(nullable = false)
    private boolean processed;

    public Message() {
    }

    public Message(
            Long id,
            String content,
            String sender,
            String receiver,
            LocalDateTime timestamp,
            boolean processed) {

        this.id = id;
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.processed = processed;
    }
}

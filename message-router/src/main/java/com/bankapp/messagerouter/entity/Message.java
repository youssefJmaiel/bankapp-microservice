package com.bankapp.messagerouter.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
public class Message implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;
    @NotNull
    @JsonProperty("content")  // Ici, on renomme le champ en "text" dans le JSON
    private String content;
    @NotNull
    private String sender;
    @NotNull
    private String receiver;
    @NotNull
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private boolean processed;

    public Message() {}

    public Message(Long id, String content, String sender, String receiver, LocalDateTime timestamp, boolean processed) {
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.processed = processed;
    }
}





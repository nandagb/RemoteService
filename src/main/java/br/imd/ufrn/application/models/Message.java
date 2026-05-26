package br.imd.ufrn.application.models;

import java.time.LocalDateTime;

public class Message {
    private int senderId;
    private int receiverId;
    private int id;
    private String content;

    public Message() {

    }

    public void setSenderId(int id) {
        this.senderId = id;
    }

    public void setReceiverId(int id) {
        this.receiverId = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return this.senderId;
    }

    public int getReceiverId() {
        return this.receiverId;
    }

    public String getContent() {
        return this.content;
    }

    public int getId() {
        return this.id;
    }
}

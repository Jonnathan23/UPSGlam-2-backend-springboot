package com.main.spring.app.schema;

import com.google.cloud.Timestamp;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommenSchema {

    public String com_authorUid;
    public String com_text;
    public Timestamp com_timestamp;
    public String com_posUid;

    public CommenSchema(String com_authorUid, String com_text, String com_posUid) {
        this.com_authorUid = com_authorUid;
        this.com_text = com_text;
        this.com_timestamp = Timestamp.now();
        this.com_posUid = com_posUid;
    }
}

package com.main.spring.app.schema;

import com.google.cloud.Timestamp;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LikeSchema {
    public Timestamp lik_timestamp;
    public String lik_authorUid;
    public String lik_postUid;

    public LikeSchema(String lik_authorUid, String lik_postUid) {
        this.lik_timestamp = Timestamp.now();
        this.lik_authorUid = lik_authorUid;
        this.lik_postUid = lik_postUid;
    }

}

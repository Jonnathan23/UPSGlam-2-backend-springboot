package com.main.spring.app.schema;

import com.google.cloud.Timestamp;
import lombok.Data;

@Data
public class SubscriptionSchema {

    public Timestamp sub_timestamp;

    public SubscriptionSchema() {
        this.sub_timestamp = Timestamp.now();
    }
}


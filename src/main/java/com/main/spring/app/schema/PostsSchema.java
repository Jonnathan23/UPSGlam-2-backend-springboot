package com.main.spring.app.schema;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

@Data
@NoArgsConstructor

public class PostsSchema {

    public String pos_postId;
    public String pos_authorUid;
    public String pos_imageUrl;
    public String pos_caption;
    public Timestamp pos_timestamp;
    public int pos_likesCount = 0;
    public int pos_commentsCount = 0;

    public PostsSchema(String pos_authorUid, String pos_imageUrl, String pos_caption) {
        this.pos_authorUid = pos_authorUid;
        this.pos_imageUrl = pos_imageUrl;
        this.pos_caption = pos_caption;
        this.pos_timestamp = Timestamp.now();
    }

}
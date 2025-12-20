package ru.mtuci.coursemanagement.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequest {
    private String content;
    private Long authorId;
}
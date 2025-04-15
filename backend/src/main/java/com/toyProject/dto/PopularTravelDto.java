package com.toyProject.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PopularTravelDto {
    private Long id;
    private String title;
    private String imageUrl;
    private Long participantCount;

    @JsonCreator
    public PopularTravelDto(@JsonProperty("id") Long id, @JsonProperty("title") String title) {
        this.id = id;
        this.title = title;
    }
}

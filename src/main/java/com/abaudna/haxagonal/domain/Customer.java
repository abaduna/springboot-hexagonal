package com.abaudna.haxagonal.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Customer {
    private Long Id;
    private String name;
    private String country;
}

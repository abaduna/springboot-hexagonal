package com.abaudna.haxagonal.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Orders {
    private String id;
    private Long coustomerId;
    private String total;
}

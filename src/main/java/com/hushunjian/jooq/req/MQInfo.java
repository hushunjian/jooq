package com.hushunjian.jooq.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MQInfo {
    private List<QueueInfo> queues;

    private List<ExchangeInfo> exchanges;

    private List<BindingInfo> bindings;
}

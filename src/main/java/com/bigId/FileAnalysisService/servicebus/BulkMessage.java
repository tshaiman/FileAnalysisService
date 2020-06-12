package com.bigId.FileAnalysisService.servicebus;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BulkMessage extends Message {

    private List<String> lines;
    private int bulkOffset;
}

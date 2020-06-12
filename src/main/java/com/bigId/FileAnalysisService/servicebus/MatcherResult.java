package com.bigId.FileAnalysisService.servicebus;

import com.bigId.FileAnalysisService.model.Position;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class MatcherResult extends Message{
    private String name;
    private List<Position> positions;


}

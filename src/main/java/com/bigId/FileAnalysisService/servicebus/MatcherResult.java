package com.bigId.FileAnalysisService.servicebus;

import com.bigId.FileAnalysisService.contracts.Position;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MatcherResult  {
    private String name;
    private List<Position> positions;

}

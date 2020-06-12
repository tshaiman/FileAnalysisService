package com.bigId.FileAnalysisService.model;

import lombok.AllArgsConstructor;

import lombok.Getter;

@Getter
@AllArgsConstructor
public class Position {
    private int lineNumber;
    private int charOffset;
}

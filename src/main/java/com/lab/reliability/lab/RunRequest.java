package com.lab.reliability.lab;

public record RunRequest(
    String scenario,
    String mode,
    int threads,
    int durationSec,
    int warmupSec
) {}

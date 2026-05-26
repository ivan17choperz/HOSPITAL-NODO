package com.parcial_final.ms_triage.entity;

public enum TriageCategory {
    RED,    // Inmediato — amenaza vital inmediata
    ORANGE, // Muy urgente — situación grave
    YELLOW, // Urgente — estado estable con riesgo
    GREEN,  // Normal — urgencia menor
    BLUE    // No urgente — consulta diferible
}

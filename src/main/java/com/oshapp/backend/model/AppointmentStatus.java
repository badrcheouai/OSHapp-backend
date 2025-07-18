package com.oshapp.backend.model;
 
public enum AppointmentStatus {
    DEMANDE,           // Demande initiée par le salarié
    PROPOSE,           // Créneau proposé par l'infirmier/médecin
    CONFIRME,          // Rendez-vous confirmé par le salarié
    REPORTE,           // Rendez-vous reporté (avec motif)
    ANNULE,            // Rendez-vous annulé
    TERMINE            // Rendez-vous terminé
} 
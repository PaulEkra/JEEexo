package com.example.applicationgestinemployes.service;

import com.example.applicationgestinemployes.model.Employe;

import java.util.List;
import java.util.Map;

public interface EmployeService {
    List<Employe> getAllEmployes();
    Employe getEmploye(Long id);
    void addEmploye(Employe employe);
    void updateEmploye(Employe employe);
    boolean deleteEmploye(Long id);

    long getNumberOfEmployes();
    Map<String, Long> getEmployeDistributionByPoste();

    Employe getEmployeByCourriel(String courriel);


}

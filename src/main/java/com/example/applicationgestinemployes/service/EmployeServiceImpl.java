package com.example.applicationgestinemployes.service;

import com.example.applicationgestinemployes.model.Employe;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import jakarta.persistence.TypedQuery;

import javax.persistence.NoResultException;
import java.util.Map;
import java.util.stream.Collectors;

@RequestScoped
public class EmployeServiceImpl implements EmployeService {

    @PersistenceContext(unitName = "GestEmploye_dbConfig")
    private EntityManager em;

    @Override
    public List<Employe> getAllEmployes() {
        return em.createQuery("SELECT e FROM Employe e", Employe.class).getResultList();
    }

    @Override
    public Employe getEmploye(Long id) {
        return em.find(Employe.class, id);
    }

    @Transactional
    @Override
    public void addEmploye(Employe employe) {
        em.persist(employe);
    }

    @Transactional
    @Override
    public void updateEmploye(Employe employe) {
        em.merge(employe);
    }


    @Transactional
    @Override
    public boolean deleteEmploye(Long id) {
        Employe employe = em.find(Employe.class, id);
        if (employe != null) {
            em.remove(employe);
            return true;
        }
        return false;
    }

    public long getNumberOfEmployes() {
        return em.createQuery("SELECT COUNT(e) FROM Employe e", Long.class).getSingleResult();
    }

    public Map<String, Long> getEmployeDistributionByPoste() {
        TypedQuery<Object[]> query = em.createQuery("SELECT e.poste, COUNT(e) FROM Employe e GROUP BY e.poste", Object[].class);
        return query.getResultList().stream()
                .collect(Collectors.toMap(result -> (String) result[0], result -> (Long) result[1]));
    }

    public Employe getEmployeByCourriel(String courriel) {
        try {
            return em.createQuery("SELECT e FROM Employe e WHERE e.courriel = :courriel", Employe.class)
                    .setParameter("courriel", courriel)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // No employee found
        }
    }
}
package com.example.applicationgestinemployes.controller;

import com.example.applicationgestinemployes.model.Employe;
import com.example.applicationgestinemployes.model.Responsable;
import com.example.applicationgestinemployes.service.EmployeService;
import com.example.applicationgestinemployes.service.ResponsableService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class EmployeController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EmployeService employeService;

    @Inject
    private  ResponsableService responsableService;

    private transient List<Employe> employes;
    private Employe selectedEmploye = new Employe();

    private long totalEmployes;
    private Map<String, Long> employeDistributionByPoste;

    @PostConstruct
    public void init() {
        employes = employeService.getAllEmployes();
        totalEmployes = employeService.getNumberOfEmployes();
        employeDistributionByPoste = employeService.getEmployeDistributionByPoste();

        String employeId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (employeId != null) {
            selectedEmploye = employeService.getEmploye(Long.parseLong(employeId));
        }
    }

    public void addEmploye() {
        Responsable responsable = getLoggedInResponsable();
        if (responsable != null) {
            selectedEmploye.setResponsable(responsable);
            employeService.addEmploye(selectedEmploye);
            selectedEmploye = new Employe(); // Reset
            init(); // Recharger la liste des employés

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Employé enregistré."));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Employé non enregistré. Responsable non trouvé."));
        }
    }

    @Transactional
    public void updateEmploye() throws IOException {
        if (selectedEmploye != null && selectedEmploye.getIdEmploye() != null) {
            Employe existingEmploye = employeService.getEmploye(selectedEmploye.getIdEmploye());
            if (existingEmploye != null) {
                existingEmploye.setNom(selectedEmploye.getNom());
                existingEmploye.setAdresse(selectedEmploye.getAdresse());
                existingEmploye.setCourriel(selectedEmploye.getCourriel());
                existingEmploye.setPoste(selectedEmploye.getPoste());
                existingEmploye.setNumeroTelephone(selectedEmploye.getNumeroTelephone());
                existingEmploye.setSalaire(selectedEmploye.getSalaire());

                employeService.updateEmploye(existingEmploye);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Employé mis à jour avec succès."));
                FacesContext.getCurrentInstance().getExternalContext().redirect("listEmploye.xhtml?id=" + existingEmploye.getIdEmploye());
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "L'employé sélectionné n'existe pas."));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Aucun employé sélectionné."));
        }
    }

    public void deleteEmploye() {
        if (selectedEmploye != null && selectedEmploye.getIdEmploye() != null) {
            employeService.deleteEmploye(selectedEmploye.getIdEmploye());
            employes.remove(selectedEmploye);
            selectedEmploye = new Employe(); // Reset selected employe
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Employé supprimé avec succès."));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur", "Aucun employé sélectionné."));
        }
    }

    // Getters and Setters
    public List<Employe> getEmployes() {
        return employes;
    }

    public void setEmployes(List<Employe> employes) {
        this.employes = employes;
    }

    public Employe getSelectedEmploye() {
        return selectedEmploye;
    }

    public void setSelectedEmploye(Employe selectedEmploye) {
        this.selectedEmploye = selectedEmploye;
    }

    public long getTotalEmployes() {
        return totalEmployes;
    }

    public Map<String, Long> getEmployeDistributionByPoste() {
        return employeDistributionByPoste;
    }
    public Responsable getLoggedInResponsable() {
        String username = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("username");
        if (username != null) {
            return responsableService.findByCourriel(username);
        }
        return null;
    }
}

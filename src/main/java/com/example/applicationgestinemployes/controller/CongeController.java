package com.example.applicationgestinemployes.controller;

import com.example.applicationgestinemployes.model.Conge;
import com.example.applicationgestinemployes.model.Employe;
import com.example.applicationgestinemployes.model.Responsable;
import com.example.applicationgestinemployes.service.CongeService;
import com.example.applicationgestinemployes.service.EmployeService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@RequestScoped
public class CongeController implements Serializable {

    @Inject
    private CongeService congeService;

    private Conge selectedConge = new Conge();

    @Inject
    private EmployeService employeService;

    private Conge newConge = new Conge();
    private List<Conge> pendingConges;

    @PostConstruct
    public void init() {
        pendingConges = congeService.findAllPending();
    }

    public String createConge() {
        Employe employe = getLoggedInEmploye();
        if (employe == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Employé non trouvé."));
            return "failure"; // Rediriger vers une page d'erreur si nécessaire
        }

        Responsable responsable = employe.getResponsable();
        if (responsable == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Responsable non défini pour l'employé."));
            return "failure"; // Rediriger vers une page d'erreur si nécessaire
        }

        newConge.setEmploye(employe);
        newConge.setStatut("en attente");
        congeService.create(newConge);
        // Envoyer une notification par e-mail au responsable
        if (employe.getResponsable() != null) {
            String managerEmail = employe.getResponsable().getCourriel();
            String employeeName = employe.getNom();
            String leaveStartDate = newConge.getDateDebut().toString();
            String leaveEndDate = newConge.getDateFin().toString();
            congeService.sendLeaveRequestNotificationToManager(managerEmail, employeeName, leaveStartDate, leaveEndDate);
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Demande de congé soumise avec succès."));
        newConge = new Conge();

        return "success"; // Rediriger vers une page de succès si nécessaire
    }



    public void approuverConge(Long congeId) {
        congeService.approuverConge(congeId);
        pendingConges = congeService.findAllPending(); // Rafraîchir la liste
    }

    public void rejeterConge(Long congeId) {
        congeService.rejeterConge(congeId);
        pendingConges = congeService.findAllPending(); // Rafraîchir la liste
    }

    public Conge getSelectedConge() {
        return selectedConge;
    }

    public void setSelectedConge(Conge selectedConge) {
        this.selectedConge = selectedConge;
    }


    public Employe getLoggedInEmploye() {
        String username = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("username");
        if (username != null) {
            List<Employe> employes = employeService.getAllEmployes();
            for (Employe employe : employes) {
                if (employe.getCourriel().equals(username)) {
                    return employe;
                }
            }
        }
        return null;
    }

    public Conge getNewConge() {
        return newConge;
    }

    public void setNewConge(Conge newConge) {
        this.newConge = newConge;
    }

    public List<Conge> getPendingConges() {
        return pendingConges;
    }

    public void setPendingConges(List<Conge> pendingConges) {
        this.pendingConges = pendingConges;
    }

    public List<Conge> getHistoriqueCongesUtilisateurConnecte() {
        Employe employe = getLoggedInEmploye();
        if (employe != null) {
            List<Conge> tousConges = congeService.getAllConges(); // Remplacez par la méthode adéquate
            List<Conge> historiqueConges = new ArrayList<>();
            for (Conge conge : tousConges) {
                if (conge.getEmploye() != null && conge.getEmploye().getIdEmploye() != null && conge.getEmploye().getIdEmploye().equals(employe.getIdEmploye())) {
                    historiqueConges.add(conge);
                }
            }
            return historiqueConges;
        } else {
            List<Conge> tousConges = congeService.getAllConges();
            return new ArrayList<>(tousConges);
        }
    }




}

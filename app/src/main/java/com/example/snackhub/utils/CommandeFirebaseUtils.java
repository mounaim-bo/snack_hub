package com.example.snackhub.utils;

import android.util.Log;
import com.example.snackhub.models.Commande;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandeFirebaseUtils {
    private static final String TAG = "CommandeFirebaseUtils";
    private static final String COLLECTION_COMMANDES = "commandes";

    public interface CommandeCallback {
        void onSuccess(String commandeId);
        void onFailure(String error);
    }

    public static void sauvegarderCommande(String snackId, String snackName,
                                           String clientNom, String clientTelephone, String clientEmail,
                                           String modeCommande, List<Commande> articles,
                                           double prixTotal, String notes,
                                           CommandeCallback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Convertir les articles en format Firebase
        List<Map<String, Object>> articlesFirebase = new ArrayList<>();
        for (Commande commande : articles) {
            Map<String, Object> article = new HashMap<>();
            article.put("nomRepas", commande.getNomRepas());
            article.put("description", commande.getDescription());
            article.put("quantite", commande.getQuantite());
            article.put("prixUnitaire", commande.getPrixUnitaire());
            article.put("prixTotal", commande.getPrixTotal());
            article.put("imageResId", commande.getImageResId()); // Votre nom correct
            articlesFirebase.add(article);
        }

        // Créer l'objet commande pour Firebase
        Map<String, Object> commandeData = new HashMap<>();
        commandeData.put("snackId", snackId);
        commandeData.put("snackName", snackName);
        commandeData.put("clientNom", clientNom);
        commandeData.put("clientTelephone", clientTelephone);
        commandeData.put("clientEmail", clientEmail);
        commandeData.put("modeCommande", modeCommande);
        commandeData.put("articles", articlesFirebase);
        commandeData.put("prixTotal", prixTotal);
        commandeData.put("notes", notes);
        commandeData.put("statut", "En attente");
        commandeData.put("dateCommande", Timestamp.now());
        commandeData.put("dateModification", Timestamp.now());

        // Sauvegarder dans Firebase
        db.collection(COLLECTION_COMMANDES)
                .add(commandeData)
                .addOnSuccessListener(documentReference -> {
                    String commandeId = documentReference.getId();

                    // Mettre à jour l'ID dans le document
                    documentReference.update("id", commandeId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Commande sauvegardée avec ID: " + commandeId);
                                if (callback != null) {
                                    callback.onSuccess(commandeId);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Erreur lors de la mise à jour de l'ID", e);
                                if (callback != null) {
                                    callback.onFailure("Erreur lors de la sauvegarde: " + e.getMessage());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la sauvegarde de la commande", e);
                    if (callback != null) {
                        callback.onFailure("Erreur lors de la sauvegarde: " + e.getMessage());
                    }
                });
    }

    // Méthode pour récupérer les commandes d'un snack (pour le BackofficeActivity)
    public static void getCommandesSnack(String snackId, CommandeSnackCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(COLLECTION_COMMANDES)
                .whereEqualTo("snackId", snackId)
                // TEMPORAIRE: Retirer le orderBy pour éviter l'erreur d'index
                // .orderBy("dateCommande", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> commandes = new ArrayList<>();
                    queryDocumentSnapshots.forEach(document -> {
                        Map<String, Object> commandeData = document.getData();

                        // IMPORTANT: Ajouter l'ID du document si il n'existe pas
                        if (!commandeData.containsKey("id") || commandeData.get("id") == null) {
                            commandeData.put("id", document.getId());
                            Log.d(TAG, "ID ajouté pour document: " + document.getId());
                        }

                        Log.d(TAG, "Commande récupérée - ID: " + commandeData.get("id"));
                        commandes.add(commandeData);
                    });

                    Log.d(TAG, "Total commandes récupérées: " + commandes.size());
                    if (callback != null) {
                        callback.onSuccess(commandes);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la récupération des commandes", e);
                    if (callback != null) {
                        callback.onFailure("Erreur: " + e.getMessage());
                    }
                });
    }

    // Interface pour récupérer les commandes (modifiée)
    public interface CommandeSnackCallback {
        void onSuccess(List<Map<String, Object>> commandes);
        void onFailure(String error);
    }

    // Méthode pour mettre à jour le statut d'une commande (pour le BackofficeActivity)
    public static void updateStatutCommande(String commandeId, String nouveauStatut,
                                            CommandeCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> updates = new HashMap<>();
        updates.put("statut", nouveauStatut);
        updates.put("dateModification", com.google.firebase.Timestamp.now());

        db.collection(COLLECTION_COMMANDES)
                .document(commandeId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess(commandeId);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure("Erreur: " + e.getMessage());
                    }
                });
    }
}
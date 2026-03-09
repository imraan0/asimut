# PROJET ASIM'UT
**Collège-Lycée Asimov — ISIK Imran**  
**Étape 1 — Users et Abusers Stories**

---

## USERS STORIES

> Les Users Stories décrivent les besoins des utilisateurs du logiciel Asimut, avec le format suivant :  
> *En tant que [rôle], je veux [action] afin de [bénéfice]*

---

### Rôle : Secrétariat

| ID | Rôle | Je veux… | Afin de… | Priorité | Critères d'acceptation |
|----|------|----------|----------|----------|------------------------|
| US-01 | Secrétariat | Importer un fichier CSV d'élèves de 6e en début d'année | Gagner du temps et éviter les erreurs de saisie manuelle | 🔴 Haute | Le fichier CSV au format nom/prénom/identifiant/niveau est accepté. Les doublons sont signalés. Les élèves sont créés en base avec le niveau 6. |
| US-02 | Secrétariat | Saisir la moyenne générale d'un élève par semestre | Alimenter le suivi de scolarité | 🔴 Haute | La saisie est possible uniquement avant validation. Une fois validée par le proviseur, la modification est impossible pour le secrétariat. |
| US-03 | Secrétariat | Affecter automatiquement un enseignant référent à un élève (round-robin) | Répartir équitablement la charge entre enseignants | 🔴 Haute | L'algorithme round-robin distribue les élèves de façon équilibrée. Le secrétariat peut aussi affecter manuellement (attention à la casse de l'automatisation). |
| US-04 | Secrétariat | Valider le projet d'un élève (responsable de projet) | Permettre aux élèves de créer des projets conformes et s'investir dans la vie scolaire | 🟡 Moyenne | Une fois que l'élève aura soumis sa demande de projet, le secrétariat devra valider cette dernière pour que l'élève puisse commencer. |
| US-05 | Secrétariat | Enregistrer les informations d'un parent (nom, prénom, email) | Pouvoir les contacter par mail | 🟡 Moyenne | Un seul parent est saisi par élève. Les données respectent le RGPD (consentement, suppression possible). |

---

### Rôle : Proviseur

| ID | Rôle | Je veux… | Afin de… | Priorité | Critères d'acceptation |
|----|------|----------|----------|----------|------------------------|
| US-06 | Proviseur | Valider ou corriger une moyenne saisie par le secrétariat | Garantir l'exactitude des données officielles | 🔴 Haute | Le proviseur voit les moyennes en attente de validation. Il peut les approuver ou les corriger. Après validation, elles sont verrouillées. |
| US-07 | Proviseur | Consulter l'historique complet de scolarité d'un élève | Prendre des décisions d'orientation éclairées | 🔴 Haute | L'historique affiche toutes les classes fréquentées et les moyennes par semestre, avec graphique d'évolution. |

---

### Rôle : Enseignant référent

| ID | Rôle | Je veux… | Afin de… | Priorité | Critères d'acceptation |
|----|------|----------|----------|----------|------------------------|
| US-08 | Enseignant référent | Voir la liste des élèves dont je suis référent | Assurer leur suivi personnalisé | 🔴 Haute | L'enseignant voit ses élèves référents avec leurs informations de base (nom/prénom/classe). Les élèves peuvent être de classes différentes. |
| US-09 | Enseignant référent | Avoir un code couleur pour le suivi de recherche des stages | Assurer le suivi de recherche des stages et agir | 🔴 Haute | Plusieurs indicateurs visuels : 🔴 Rouge (aucune recherche OU 15 demandes / 0 accepté), 🟠 Orange (quelques recherches, quelques refus), 🔵 Bleu (entretien accordé), 🟢 Vert (stage validé). *⚠️ À affiner lors du développement.* |
| US-10 | Enseignant référent | Valider la convention de stage d'un élève | Officialiser le stage avant signature | 🔴 Haute | Le prof référent visualise le PDF de convention, peut ajouter des commentaires et valider. |
| US-11 | Enseignant référent | Consulter les moyennes par semestre de mes élèves référents | Anticiper les difficultés scolaires | 🟡 Moyenne | Graphique en barres ou lignes accessible depuis la fiche de chaque élève. |

---

### Rôle : Élève

| ID | Rôle | Je veux… | Afin de… | Priorité | Critères d'acceptation |
|----|------|----------|----------|----------|------------------------|
| US-12 | Élève | Consulter mes moyennes et mon historique de classe | Suivre ma progression scolaire | 🔴 Haute | L'élève voit ses moyennes par semestre et son parcours. Il ne peut pas modifier ces données. |
| US-13 | Élève | Voir le nom et les coordonnées de mon enseignant référent | Le contacter facilement | 🔴 Haute | Affiché sur le tableau de bord élève. |
| US-14 | Élève | Saisir mes recherches de stage (entreprise, contact, lettres, entretien) | Garder une trace organisée de mes démarches | 🔴 Haute | Formulaire de saisie avec : nom entreprise, contact, lettres envoyées/reçues, dates entretiens, résultats. |
| US-15 | Élève | Remplir ma convention de stage en ligne à partir d'un modèle PDF | Préparer mon stage sans paperasse manuelle | 🔴 Haute | Le PDF vierge est pré-rempli avec les données de l'élève. Il est soumis au référent pour validation. |
| US-16 | Élève | Garder la trace de participation aux projets de l'établissement | S'investir dans la vie scolaire | 🟡 Moyenne | L'élève sélectionne un projet dans une liste déroulante des projets créés et valide sa participation. |
| US-17 | Élève | Créer un nouveau projet et en être responsable | S'investir davantage dans la vie scolaire | 🟡 Moyenne | L'élève crée un projet (nom, objectif, etc.) dont il sera responsable. Le secrétariat doit valider le projet pour qu'il soit lancé. |

---

## ABUSERS STORIES

> Les Abusers Stories identifient les menaces potentielles sur l'application.  
> Chaque menace est associée à une contre-mesure technique.

| ID | Attaquant / Rôle mal intentionné | Menace / Action malveillante | Impact potentiel | Contre-mesure |
|----|----------------------------------|------------------------------|------------------|---------------|
| AS-01 | Attaquant externe / Script automatisé | Tenter de se connecter par force brute sur le formulaire de login | Accès non autorisé à des données d'élèves mineurs (RGPD critique) | Rate limiting, blocage après 5 tentatives, tokens JWT courts. |
| AS-02 | Élève mal intentionné | Modifier l'URL pour accéder à la fiche d'un autre élève (IDOR) | Violation de la vie privée, accès aux notes ou données de stage d'autrui | Vérifier l'identité du token JWT à chaque requête. Ne jamais se fier à l'ID passé en paramètre sans contrôle d'appartenance. |
| AS-03 | Secrétariat mal intentionné | Modifier une moyenne après validation du proviseur | Falsification des données officielles de scolarité | Verrou en base : champ `valide` booléen bloque toute modification. Log de chaque tentative. |
| AS-04 | Attaquant via API | Envoyer des requêtes SQL malveillantes dans les champs de formulaire (injection SQL) | Corruption ou exfiltration de la base de données | Utilisation exclusive d'un ORM (Sequelize) avec requêtes paramétrées. Jamais de SQL brut concaténé. |
| AS-05 | Élève ou tiers | Uploader un fichier CSV ou PDF malveillant contenant du code | Exécution de code côté serveur (RCE) ou XSS | Validation stricte du type MIME, taille maximale. Fichiers stockés hors du répertoire web. |
| AS-06 | Utilisateur authentifié | Élever ses privilèges en falsifiant le token JWT (changer son rôle en "proviseur") | Accès à des fonctions d'administration, modification de notes | Signature JWT côté serveur avec clé secrète forte. Vérification du rôle en base à chaque requête sensible. |
| AS-07 | Enseignant non référent | Accéder à la liste des élèves d'un collègue référent | Violation des règles RGPD sur l'accès aux données | Chaque requête `/professeurs/:id/eleves` vérifie que l'id correspond au token JWT. Middleware de contrôle d'accès. |
| AS-08 | Administrateur système | Accéder directement à la base de données pour lire les mots de passe | Compromission de tous les comptes | Mots de passe hachés avec bcrypt (coût >= 12). Jamais de mot de passe en clair en base ou dans les logs. |
| AS-09 | Tiers / Parent | Accéder aux données via un lien partagé sans authentification | Accès non autorisé aux données d'élèves mineurs (RGPD) | Toutes les routes API protégées par JWT. Durée de validité courte. Pas de données sensibles dans les URLs. |

---

## Récapitulatif des principes RGPD appliqués

- **Données minimales** : seul un parent par élève est collecté. Pas de données superflues.
- **Finalité** : les données sont utilisées uniquement pour le suivi de scolarité.
- **Durée de conservation** : à définir avec l'établissement (ex. : 5 ans après fin de scolarité).
- **Droit d'accès et de suppression** : l'application permettra la suppression d'un compte et de ses données associées.
- **Sécurité** : mots de passe hachés (bcrypt), communications en HTTPS, tokens JWT à durée limitée.
- **Hébergement** : AlwaysData (données hébergées en France/UE).
- **Élèves mineurs** : accès restreint, données parents protégées, pas de partage tiers.

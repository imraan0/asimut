# Asimut
Application de suivi de scolarité pour le Collège-Lycée Asimov  
Projet BTS SIO SLAM — ISIK Imran

---

## Stack technique

| Composant | Technologie |
|-----------|-------------|
| API REST | Node.js + Express + Sequelize |
| Base de données | PostgreSQL 16 |
| Client lourd | Java 17 (Maven) |
| Authentification | JWT (jsonwebtoken + bcrypt) |
| Hébergement | AlwaysData |
| Emails | Nodemailer (Gmail) |
| PDF | PDFKit |

---

## Fonctionnalités

### API REST
- Authentification JWT avec gestion des rôles (secrétariat, proviseur, professeur, élève)
- Gestion des élèves avec import CSV
- Gestion des moyennes par semestre avec verrou proviseur
- Recherches de stage avec statuts (refus, validé, en attente,..)
- Conventions de stage avec génération PDF
- Attestations de stage avec upload PDF signé
- Projets avec gestion des participations
- Options (max 2 par élève)
- Référents professeurs avec affectation automatique round-robin ou manuelle par secrétariat
- Publipostage parents par email

### Client lourd Java
- Interface Swing multi-rôles (secrétariat, proviseur, professeur, élève)
- Dashboard adapté selon le rôle connecté
- CRUD complet pour chaque module
- Graphique des moyennes par niveau de classe (vue proviseur)
- Téléchargement et upload de PDF
- Import CSV d'élèves
- JAR exécutable livrable

---

## Prérequis

### Pour lancer le client lourd
- [Java 17+](https://adoptium.net)

```bash
java -jar client-java/asimut-client.jar
```

### Pour lancer l'API en local
- Node.js 18+
- PostgreSQL 16

```bash
cd api
npm install
cp .env.example .env  # configurer les variables
npm run dev
```

### Pour visualiser les diagrammes UML
- Extension **PlantUML** sur IDE
- [Graphviz](https://graphviz.org/download/) — `brew install graphviz` (Mac)
- Ouvrir un fichier `.puml` et appuyer sur `Alt+D` / `Option+D`

---

## Variables d'environnement

Créer un fichier `.env` dans `api/` :

```env
PORT=3000
DB_HOST=localhost
DB_PORT=5432
DB_NAME=asimut
DB_USER=ton_user
DB_PASS=ton_password
JWT_SECRET=ton_secret
EMAIL_USER=ton_email@gmail.com
EMAIL_PASS=ton_app_password
```

---

## Comptes de test (production)

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Secrétariat | admin@asimov.fr | Admin1234! |
| Proviseur | proviseur@asimov.fr | Admin1234! |
| Professeur | prof@asimov.fr | Admin1234! |
| Élève | imran.isik@asimov.fr | Admin1234! |

---

## Structure du projet
asimut/  
├──   api/              → serveur Node.js + Express  
│     ├── controllers/  → logique métier  
│     ├── models/       → modèles Sequelize  
│     ├── routes/       → définition des routes  
│     ├── middlewares/  → auth JWT, upload  
│     └── uploads/      → fichiers uploadés  
├──   client-java/      → client lourd Java  
│     ├── asimut/       → code source Maven  
│     └── asimut-client.jar → JAR exécutable  
└──   docs/             → UML et documentation   

---

## Déploiement

L'API est déployée sur AlwaysData :  
**https://asimut.alwaysdata.net**

# Asimut 

Application de suivi de scolarité pour le Collège-Lycée Asimov  
Projet BTS SIO SLAM — ISIK Imran

---

## Stack technique
- API REST : Node.js + Express
- Base de données : PostgreSQL
- Client lourd : Java (Maven)

---

## Prérequis

### Pour visualiser les diagrammes UML
- [Java](https://adoptium.net) (version 17 ou 21)
- [Graphviz](https://graphviz.org/download/) — `brew install graphviz` (sur Mac)
- Extension **PlantUML** sur IDE
- Ouvrir un fichier `.puml` et appuyer sur `Alt+D` ou `option + D` pour prévisualiser

### Pour lancer l'API
cd api && npm install && npm run dev

### Pour lancer le client Java
cd client-java && mvn install

---

## Structure du projet
asimut/  
├── api/          → serveur Node.js  
├── client-java/  → client lourd Java  
├── client-web/   → client web (optionnel)  
└── docs/         → UML et documentation  
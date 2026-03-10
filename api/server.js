const express = require('express')
const dotenv = require('dotenv')

// Charge les variables d'environnement depuis .env
dotenv.config()

const app = express()
const PORT = process.env.PORT || 3000

// Middleware pour lire le JSON dans les requêtes
app.use(express.json())

// Route de test pour vérifier que le serveur tourne
app.get('/', (req, res) => {
    res.json({ message: 'API Asimut opérationnelle ' })
})

// Démarrage du serveur
app.listen(PORT, () => {
    console.log(`Serveur démarré sur le port ${PORT}`)
})
const dotenv   = require('dotenv')
dotenv.config()

const express  = require('express')
const { connectDB } = require('./config/database')

const app  = express()
const PORT = process.env.PORT || 3000

app.use(express.json())

// Connexion à la BDD au démarrage
connectDB()
require('./models')  // charge tous les modèles et leurs relations (test pour l'instant)

app.get('/', (req, res) => {
    res.json({ message: 'API Asim\'UT opérationnelle 🚀' })
})

app.listen(PORT, () => {
    console.log(`Serveur démarré sur le port ${PORT}`)
})
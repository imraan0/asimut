const dotenv   = require('dotenv')
dotenv.config()

const express  = require('express')
const { connectDB } = require('./config/database')
const auth     = require('./routes/auth')
const eleves = require('./routes/eleves')

const app  = express()
const PORT = process.env.PORT || 3000

app.use(express.json())
app.use('/auth', auth)
app.use('/eleves', eleves)

connectDB()
require('./models')

app.get('/', (req, res) => {
    res.json({ message: 'API Asimut opérationnelle 🚀' })
})

app.listen(PORT, () => {
    console.log(`Serveur démarré sur le port ${PORT}`)
})
const dotenv   = require('dotenv')
dotenv.config()

const express  = require('express')
const { connectDB } = require('./config/database')

const auth     = require('./routes/auth')
const eleves = require('./routes/eleves')
const moyenne = require('./routes/moyenne')
const stages = require('./routes/stages')
const projets = require('./routes/projets')
const options = require('./routes/options')
const professeurs = require('./routes/professeurs')
const conventions = require('./routes/convention')
const attestations = require('./routes/attestations')
const mails = require('./routes/mails')
const classeRoutes = require('./routes/classes');
const semestreRoutes = require('./routes/semestres');
const parents = require('./routes/parents');

const app  = express()
const PORT = process.env.PORT || 3000

app.use(express.json())
app.use('/parents', parents);
app.use('/classes', classeRoutes);
app.use('/semestres', semestreRoutes);
app.use('/auth', auth)
app.use('/eleves', eleves)
app.use('/moyennes', moyenne)
app.use('/stages', stages)
app.use('/projets', projets)
app.use('/options', options)
app.use('/professeurs', professeurs)
app.use('/conventions', conventions)
app.use('/attestations', attestations)
app.use('/mails', mails)

connectDB()
require('./models')

app.get('/', (req, res) => {
    res.json({ message: 'API Asimut opérationnelle 🚀' })
})

app.listen(PORT, () => {
    console.log(`Serveur démarré sur le port ${PORT}`)
})
const { Sequelize } = require('sequelize')

// Connexion à PostgreSQL via les variables d'environnement
const sequelize = new Sequelize(
    process.env.DB_NAME,
    process.env.DB_USER,
    process.env.DB_PASSWORD,
    {
        host:    process.env.DB_HOST,
        port:    process.env.DB_PORT,
        dialect: 'postgres',
        logging: false  // true si on veut voir les requêtes SQL générées (pour deboguer etc)
    }
)

// Fonction pour tester la connexion
const connectDB = async () => {
    try {
        await sequelize.authenticate()
        console.log('Connexion à PostgreSQL réussie ✅')
    } catch (error) {
        console.error('Erreur de connexion à PostgreSQL ❌', error)
        process.exit(1) // arrête le serveur si la BDD est inaccessible
    }
}

module.exports = { sequelize, connectDB }
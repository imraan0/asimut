const { Sequelize } = require('sequelize')

// Connexion à PostgreSQL via les variables d'environnement
const sequelize = new Sequelize(
    process.env.DB_NAME,
    process.env.DB_USER,
    process.env.DB_PASS,
    {
        host: process.env.DB_HOST,
        port: process.env.DB_PORT,
        dialect: 'postgres',
        define: {
            freezeTableName: true, //permet a Sequelize de ne pas mettre de s a la fin des noms de tables
            underscored: true, 
        },
        logging: false
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
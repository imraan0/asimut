const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Utilisateur = sequelize.define('utilisateur', {
    id: {
        type:          DataTypes.INTEGER,
        primaryKey:    true,
        autoIncrement: true
    },
    email: {
        type:      DataTypes.STRING,
        allowNull: false,
        unique:    true,
        validate: {
            isEmail: true  // vérifie que c'est bien un email valide
        }
    },
    mot_de_passe: {
        type:      DataTypes.STRING,
        allowNull: false
    },
    role: {
        type:      DataTypes.ENUM('eleve', 'professeur', 'secretariat', 'proviseur'),
        allowNull: false
    }
}, {
    timestamps: true,       // ajoute created_at et updated_at automatiquement à chaque enregistrement
    underscored: true       // convertit createdAt en created_at (style SQL, snakecase au lieu de camelCase)
})

module.exports = Utilisateur